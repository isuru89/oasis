package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.services.services.backend.FlinkClient;
import io.github.isuru.oasis.services.services.backend.FlinkServices;
import io.github.isuru.oasis.services.services.backend.model.FlinkJar;
import io.github.isuru.oasis.services.services.backend.model.JarListInfo;
import io.github.isuru.oasis.services.services.backend.model.JarRunResponse;
import io.github.isuru.oasis.services.services.backend.model.JarUploadResponse;
import io.github.isuru.oasis.services.services.backend.model.JobSaveRequest;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.SubmittedJob;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Constants;
import io.github.isuru.oasis.services.utils.Maps;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
@Service("remoteLifecycleService")
public class LifeCycleServiceImpl implements ILifecycleService  {

    @Autowired
    private IOasisDao dao;

    @Autowired
    private FlinkServices services;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private OasisConfigurations oasisConfigurations;


    @Override
    public boolean start(long gameId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");

        return startDef(gameId, true);
    }

    @Override
    public boolean stop(long defId) throws Exception {
        Checks.greaterThanZero(defId, "gameId' or 'challengeId");

        SubmittedJob job = ServiceUtils.getTheOnlyRecord(dao, "getJobOfDef",
                Maps.create("defId", defId),
                SubmittedJob.class);

        if (job != null) {
            File savepointDir = new File(job.getSnapshotDir());

            Map<String, Object> data = Maps.create()
                    .put("target-directory", savepointDir.getAbsolutePath())
                    .put("cancel-job", true)
                    .build();
            JobSaveRequest request = new JobSaveRequest();
            request.putAll(data);

            // first stop flink job
            services.getFlinkClient()
                    .jobSaveAndClose(job.getJobId(), request)
                    .blockingAwait();

            Map<String, Object> map = Maps.create("jobId", job.getJobId());
            return dao.executeCommand("stopJob", map) > 0;
        }
        return false;
    }

    @Override
    public boolean startChallenge(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        return startDef(challengeId, false);
    }

    @Override
    public boolean resumeGame(long gameId) throws Exception {
        return false;
    }

    @Override
    public boolean resumeChallenge(long challengeId) throws Exception {
        return false;
    }

    private boolean startDef(long defId, boolean isGame) throws Exception {
        //File storageDir = configs.getPath(ConfigKeys.KEY_STORAGE_DIR, Constants.DEF_WORKSPACE_DIR);
        File storageDir = oasisConfigurations.getStorageDir();

        SubmittedJob job = ServiceUtils.getTheOnlyRecord(dao, "getJobOfDef",
                Maps.create("defId", defId),
                SubmittedJob.class);
        if (job != null) {
            throw new IOException("A " + (isGame ? "game" : "challenge") + " is already running!");
        }

        FlinkClient flinkClient = services.getFlinkClient();

        JarListInfo jarListInfos = flinkClient.getJars().blockingFirst();
        List<FlinkJar> files = jarListInfos.getFiles();
        if (files == null || files.isEmpty()) {
            // no jars have been deployed yet... deploy it
            files = uploadGameJar(storageDir, flinkClient);
        } else if (files.size() > 1) {
            // there are more than one jar. throw an exception.
            throw new IOException("Invalid deployment! There are more than one game artifact in the engine already!");
        }

        // write game content to file
        File executionDir = getExecutionDir(defId, storageDir);
        writeGameRulesFile(defId, isGame, executionDir);
        File executionFile = writeGameConfigFile(defId, isGame, executionDir);

        // start the game
        //
        FlinkJar uploadedJar = files.get(0);
        String args = buildGameArgs(executionFile);
        File savepointDir = executionDir.toPath().resolve(Constants.SAVEPOINT_DIR).normalize().toFile();
        FileUtils.forceMkdir(savepointDir);

        JarRunResponse jarRunResponse = flinkClient.runJar(uploadedJar.getId(),
                args,
                oasisConfigurations.getFlinkParallelism(),
                true,
                savepointDir.getAbsolutePath()).blockingSingle();

        String jobId = jarRunResponse.getJobid();
        return insertJobRecord(defId, jobId, uploadedJar.getId(), savepointDir, isGame);
    }

    private boolean insertJobRecord(long defId, String jobId, String jarId, File savepointDir,
                                    boolean isGame) throws Exception {
        long finishAt = 0L;
        if (!isGame) {
            ChallengeDef challengeDef = gameDefService.readChallenge(defId);
            if (challengeDef == null) {
                throw new InputValidationException("There is no challenge definition id exist by id " + defId + "!");
            }
            finishAt = challengeDef.getExpireAfter();
        }

        return dao.executeCommand("submitJob",
                Maps.create()
                        .put("jobId", jobId)
                        .put("jarId", jarId)
                        .put("defId", defId)
                        .put("snapshotDir", savepointDir.getAbsolutePath())
                        .put("toBeFinishedAt", finishAt)
                        .build()) > 0;
    }

    private File writeGameConfigFile(long defId, boolean isGame, File specificExecutionDir) throws IOException {
        File configFile = specificExecutionDir.toPath().resolve("run.properties").toFile();
        File templateFile = oasisConfigurations.getGameRunTemplateLocation();

        //configs.getPath("game.run.template.file", Constants.DEF_LOCATION_RUN_TEMPLATE, true, false);

        String runConfigsTxt = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);

        // replace rabbitmq source queue name correctly
        String queueName = isGame ?
                String.format("game.o.src.game.%d", defId) :
                String.format("game.o.src.challenge.%d", defId);
        runConfigsTxt = runConfigsTxt.replace("${rabbit.queue.src}", queueName);

        // write config file
        FileUtils.write(configFile, runConfigsTxt, StandardCharsets.UTF_8, false);
        return configFile;
    }

    private void writeGameRulesFile(long defId, boolean isGame, File specificExecutionDir) throws Exception {
        File ruleFile = specificExecutionDir.toPath().resolve(Constants.GAME_RULES_FILE).toFile();
        try (FileWriter writer = new FileWriter(ruleFile)) {
            writeGameRulesFile(defId, isGame, writer);
        }
    }

    public void writeGameRulesFile(long defId, boolean isGame, Writer writer) throws Exception {
        OasisGameDef oasisGameDef = new OasisGameDef();

        if (isGame) {
            GameDef gameDef = gameDefService.readGame(defId);
            oasisGameDef.setGame(gameDef);

            oasisGameDef.setKpis(gameDefService.listKpiCalculations(defId));
            oasisGameDef.setPoints(gameDefService.listPointDefs(defId));
            oasisGameDef.setBadges(gameDefService.listBadgeDefs(defId));
            oasisGameDef.setMilestones(gameDefService.listMilestoneDefs(defId));

        } else {
            // write challenge configs to file
            DefWrapper wrapper = dao.getDefinitionDao().readDefinition(defId);
            Long gameId = wrapper.getGameId();

            oasisGameDef.setGame(gameDefService.readGame(gameId));
            oasisGameDef.setChallenge(gameDefService.readChallenge(defId));
        }

        Yaml yaml = new Yaml();
        yaml.dump(oasisGameDef, writer);
    }

    private String buildGameArgs(File executionFile) throws IOException {
        if (!executionFile.isFile()) {
            throw new IOException("The input must be a file for executing game!");
        }

        return String.format(Constants.RUN_ARGS_FORMAT, executionFile.getCanonicalPath());
    }

    private List<FlinkJar> uploadGameJar(File storageDir, FlinkClient flinkClient) throws IOException {
        // no jars have been deployed yet...
        // deploy jar
        File jar = storageDir.toPath()
                .resolve(Constants.ARTIFACTS_SUB_DIR)
                .resolve(Constants.GAME_JAR).toFile();
        if (!jar.exists()) {
            throw new IOException("Game artifact jar does not exist in " + jar.getAbsolutePath() + "!");
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/x-java-archive"), jar);
        MultipartBody.Part formData = MultipartBody.Part.createFormData("oasis-game",
                jar.getName(),
                body);
        JarUploadResponse jarUploadResponse = flinkClient.uploadJar(formData).blockingSingle();
        if (jarUploadResponse.isSuccess()) {
            return flinkClient.getJars().blockingFirst().getFiles();
        } else {
            throw new IOException("Cannot upload game artifact to the engine! Please try again.");
        }
    }

    private File getExecutionDir(long defId, File storageDir) throws IOException {
        File executionDir = storageDir.toPath()
                .resolve(Constants.ALL_EXECUTIONS_DIR)
                .resolve(String.valueOf(defId))
                .normalize()
                .toFile();
        FileUtils.forceMkdir(executionDir);
        return executionDir;
    }
}
