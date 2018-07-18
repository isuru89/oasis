package io.github.isuru.oasis.services;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.backend.FlinkServices;

public class OasisServer {

    public static void main(String[] args) throws Exception {
        DbProperties dbProperties = new DbProperties(OasisDbPool.DEFAULT);

        IOasisDao oasisDao = OasisDbFactory.create(dbProperties);

        DefaultOasisApiService apiService = new DefaultOasisApiService(oasisDao);

        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init();


    }

}
