package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractServiceTest {

    @Autowired
    IOasisDao dao;

    void truncateTables(String... tableNames) throws DbException {
        for (String tbl : tableNames) {
            dao.executeRawCommand("TRUNCATE TABLE " + tbl, null);
        }
    }

}
