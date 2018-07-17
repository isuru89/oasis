package io.github.isuru.oasis.db;

import io.github.isuru.oasis.db.jdbi.JdbiOasisDao;

/**
 * @author iweerarathna
 */
public class OasisDbFactory {

    public static IOasisDao create(DbProperties dbProperties) throws Exception {
        IQueryRepo repo = new FsQueryRepo();
        repo.init(dbProperties);

        JdbiOasisDao oasisDao = new JdbiOasisDao(repo);
        oasisDao.init(dbProperties);
        return oasisDao;
    }

}
