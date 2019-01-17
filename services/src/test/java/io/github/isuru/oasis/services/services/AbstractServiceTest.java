package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.Bootstrapping;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractServiceTest {

    @Autowired
    IOasisDao dao;

    @Autowired
    private Bootstrapping bootstrapping;

    void resetSchema() throws Exception {
        Iterable<Map<String, Object>> tableList = dao.executeRawQuery("SHOW TABLES", null);
        for (Map<String, Object> map : tableList) {
            dao.executeRawCommand("TRUNCATE TABLE " + map.get("table_name").toString(), null);
        }

        bootstrapping.initialize();
    }

    void truncateTables(String... tableNames) throws DbException {
        for (String tbl : tableNames) {
            dao.executeRawCommand("TRUNCATE TABLE " + tbl, null);
        }
    }

}
