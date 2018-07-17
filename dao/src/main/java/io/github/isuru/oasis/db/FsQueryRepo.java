package io.github.isuru.oasis.db;

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
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class FsQueryRepo implements IQueryRepo {

    private final Map<String, String> queries = new ConcurrentHashMap<>();

    @Override
    public void init(DbProperties dbProperties) throws Exception {
        // initialize all scripts
        File folder = new File(dbProperties.getQueryLocation());
        if (!folder.exists()) {
            throw new FileNotFoundException("The given script folder does not exist!");
        }
        ScriptScanner scriptScanner = new ScriptScanner();
        Path root = folder.getCanonicalFile().toPath();
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
    public String fetchQuery(String queryId) throws Exception {
        return queries.get(queryId);
    }

    @Override
    public void close() throws IOException {
        queries.clear();
    }

    private static class ScriptScanner extends SimpleFileVisitor<Path> {

        private final Set<Path> scriptFiles = new HashSet<>();

        private ScriptScanner() {
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile() && file.toString().endsWith(".sql")) {
                scriptFiles.add(file.normalize());
            }
            return FileVisitResult.CONTINUE;
        }

        private Set<Path> getScriptFiles() {
            return scriptFiles;
        }
    }
}
