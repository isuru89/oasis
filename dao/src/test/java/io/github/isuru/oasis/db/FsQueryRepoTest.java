package io.github.isuru.oasis.db;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.github.isuru.oasis.model.db.DbProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class FsQueryRepoTest {

    private FileSystem fs;

    @BeforeEach
    public void beforeEach() {
        fs = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (fs != null) {
            fs.close();
        }
    }

    @Test
    public void testInit() throws Exception {
        Path dirRoot = fs.getPath("/db/scripts");
        Files.createDirectories(dirRoot);

        Path subDir1 = dirRoot.resolve("subdir-a");
        Files.createDirectories(subDir1);
        Path subDir2 = dirRoot.resolve("subdir-b");
        Files.createDirectories(subDir2);
        Path subDir11 = subDir1.resolve("subdir-a-a");
        Files.createDirectories(subDir11);

        Files.write(dirRoot.resolve("getItem.sql"), Arrays.asList("SELECT *", "FROM item;"));
        Files.write(dirRoot.resolve("getItem-h2.sql"), Arrays.asList("SELECT *", "FROM ITEM;"));
        Files.write(dirRoot.resolve("getItem2.txt"), Arrays.asList("SELECT *", "FROM item2;"));
        Files.write(dirRoot.resolve("getItem3.SQL"), Arrays.asList("SELECT *", "FROM item3;"));
        Files.write(subDir1.resolve("updateItem.sql"), Arrays.asList("UPDATE ", "SET item;"));
        Files.write(subDir1.resolve("updateItem2.SQL"), Arrays.asList("UPDATE ", "SET item2;"));
        Files.write(subDir1.resolve("updateItem3.txt"), Arrays.asList("UPDATE ", "SET item3;"));
        Files.write(subDir2.resolve("insertItem.sql"), Arrays.asList("INSERT ", "INTO item;"));
        Files.write(subDir11.resolve("deleteItem.sql"), Arrays.asList("DELETE ", "FROM item;"));

        FsQueryRepo queryRepo = new FsQueryRepo();
        Assertions.assertThrows(FileNotFoundException.class, () -> {
            File nonExistingDir = new File("./nonexistingdir");
            DbProperties properties = new DbProperties("test");
            properties.setQueryLocation(nonExistingDir.getAbsolutePath());

            queryRepo.init(properties);
        });

        queryRepo.startScan(dirRoot);
        queryRepo.captureDbName("jdbc:h2:localhost/oasis");

        Map<String, String> queries = queryRepo.getQueries();
        Assertions.assertNotNull(queries);
        Assertions.assertEquals(5, queries.size());

        String q1 = queryRepo.fetchQuery("getItem");
        Assertions.assertEquals("SELECT *\nFROM ITEM;", q1);

        String q2 = queryRepo.fetchQuery("getItem.sql");
        Assertions.assertNotEquals(q1, q2);

        Assertions.assertNull(queryRepo.fetchQuery("getItem2"));
        Assertions.assertNull(queryRepo.fetchQuery("getItem3"));

        // get from sub-path
        Assertions.assertEquals("UPDATE \nSET item;",
                queryRepo.fetchQuery("subdir-a/updateItem"));

        Assertions.assertEquals("DELETE \nFROM item;",
                queryRepo.fetchQuery("subdir-a/subdir-a-a/deleteItem"));
        Assertions.assertNull(queryRepo.fetchQuery("subdir-a/subdir-a-a/a"));

        queryRepo.close();

        {
            Assertions.assertEquals(0, queryRepo.getQueries().size());
        }
    }

    @Test
    void testCaptureJdbcUrl() {
        String url = "jdbc:h2:[file:][<path>]<databaseName>";
        FsQueryRepo repo = new FsQueryRepo();
        Assertions.assertEquals("h2", repo.captureDbName(url));
        Assertions.assertEquals("mysql", repo.captureDbName("jdbc:mysql://host1:33060/sakila"));
        Assertions.assertEquals("h2", repo.captureDbName("other:h2:[file:][<path>]<databaseName>"));
        Assertions.assertEquals("mysql", repo.captureDbName(" jdbc:mysql://host1/sakila"));
        Assertions.assertEquals("", repo.captureDbName(" jdbc::mysql://host1/sakila"));
        Assertions.assertEquals("", repo.captureDbName("//host1/sakila:jdbc:mysql"));

    }

}
