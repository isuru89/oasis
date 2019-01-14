package io.github.isuru.oasis.db;

import io.github.isuru.oasis.model.db.DbProperties;
import io.github.isuru.oasis.model.db.IQueryRepo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class FsQueryRepo implements IQueryRepo {

    private static final Pattern JDBC_PATTERN = Pattern.compile("^(\\w+):(\\w+):.+");
    private static final String SQL = ".sql";

    private final Map<String, String> queries = new ConcurrentHashMap<>();

    private String DB = "";

    @Override
    public void init(DbProperties dbProperties) throws Exception {
        // initialize all scripts
        File folder = new File(dbProperties.getQueryLocation());
        if (!folder.exists()) {
            throw new FileNotFoundException("The given script folder does not exist!");
        }

        startScan(folder.getCanonicalFile().toPath());

        captureDbName(dbProperties.getUrl());
    }

    String captureDbName(String url) {
        Matcher matcher = JDBC_PATTERN.matcher(url.trim());
        if (matcher.find()) {
            DB = matcher.group(2);
        } else {
            DB = "";
        }
        return DB;
    }

    void startScan(Path root) throws IOException {
        ScriptScanner scriptScanner = new ScriptScanner();
        Files.walkFileTree(root, scriptScanner);

        Set<Path> scriptFiles = scriptScanner.getScriptFiles();
        for (Path path : scriptFiles) {
            String content = Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .collect(Collectors.joining("\n"));
            String prefix = root.relativize(path).toString();
            queries.put(prefix, content);
        }
    }

    @Override
    public String fetchQuery(String queryId) {
        if (queryId.endsWith(SQL)) {
            return queries.get(queryId);
        } else {
            return queries.getOrDefault(queryId + "-" + DB + SQL, queries.get(queryId + SQL));
        }
    }

    Map<String, String> getQueries() {
        return queries;
    }

    @Override
    public void close() {
        queries.clear();
    }

    private static class ScriptScanner extends SimpleFileVisitor<Path> {

        private final Set<Path> scriptFiles = new HashSet<>();

        private ScriptScanner() {
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (attrs.isRegularFile() && file.toString().endsWith(SQL)) {
                scriptFiles.add(file.normalize());
            }
            return FileVisitResult.CONTINUE;
        }

        private Set<Path> getScriptFiles() {
            return scriptFiles;
        }
    }
}
