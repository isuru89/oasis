package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.OasisGameDef;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.ILifecycleService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.backend.FlinkClient;
import io.github.isuru.oasis.services.backend.FlinkServices;
import io.github.isuru.oasis.services.backend.model.FlinkJar;
import io.github.isuru.oasis.services.backend.model.JarListInfo;
import io.github.isuru.oasis.services.backend.model.JarRunResponse;
import io.github.isuru.oasis.services.backend.model.JarUploadResponse;
import io.github.isuru.oasis.services.backend.model.JobSaveRequest;
import io.github.isuru.oasis.services.model.FlinkSubmittedJob;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Constants;
import io.github.isuru.oasis.services.utils.Maps;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.commons.io.FileUtils;
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
public class LifeCycleService extends BaseService implements ILifecycleService  {

    private final FlinkServices services;

    LifeCycleService(IOasisDao dao, IOasisApiService apiService,
                     FlinkServices flinkServices) {
        super(dao, apiService);

        this.services = flinkServices;
    }

    @Override
    public boolean start(long gameId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");

        return startDef(gameId, true);
    }

    @Override
    public boolean stop(long defId) throws Exception {
        Checks.greaterThanZero(defId, "gameId' or 'challengeId");

        FlinkSubmittedJob job = getTheOnlyRecord("getJobOfDef",
                Maps.create("defId", defId),
                FlinkSubmittedJob.class);

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
                    .blockingSingle();

            Map<String, Object> map = Maps.create("jobId", job.getJobId());
            return getDao().executeCommand("stopJob", map) > 0;
        }
        return false;
    }

    @Override
    public boolean startChallenge(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        return startDef(challengeId, false);
    }

    private boolean startDef(long defId, boolean isGame) throws Exception {
        Configs configs = Configs.get();
        File storageDir = configs.getPath(Configs.KEY_STORAGE_DIR, Constants.DEF_WORKSPACE_DIR);

        FlinkSubmittedJob job = getTheOnlyRecord("getJobOfDef",
                Maps.create("defId", defId),
                FlinkSubmittedJob.class);
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
                configs.getInt(Configs.KEY_EXEC_PARALLELISM, Constants.DEF_PARALLELISM),
                true,
                savepointDir.getAbsolutePath()).blockingSingle();

        String jobid = jarRunResponse.getJobid();
        return getDao().executeCommand("submitJob",
                Maps.create()
                        .put("jobId", jobid)
                        .put("jarId", uploadedJar.getId())
                        .put("defId", defId)
                        .put("snapshotDir", savepointDir.getAbsolutePath())
                        .build()) > 0;
    }

    private File writeGameConfigFile(long defId, boolean isGame, File specificExecutionDir) throws IOException {
        File configFile = specificExecutionDir.toPath().resolve("run.properties").toFile();
        File templateFile = Configs.get().getPath("game.run.template.file",
                Constants.DEF_LOCATION_RUN_TEMPLATE, true, false);

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
        File ruleFile = specificExecutionDir.toPath().resolve("rules.yml").toFile();
        try (FileWriter writer = new FileWriter(ruleFile)) {
            writeGameRulesFile(defId, isGame, writer);
        }
    }

    public void writeGameRulesFile(long defId, boolean isGame, Writer writer) throws Exception {
        IGameDefService gameDefService = getApiService().getGameDefService();
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
            DefWrapper wrapper = getDao().getDefinitionDao().readDefinition(defId);
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
                .resolve(Constants.RUN_CONFIGS_SUB_DIR)
                .resolve(String.valueOf(defId))
                .normalize()
                .toFile();
        FileUtils.forceMkdir(executionDir);
        return executionDir;
    }
}
