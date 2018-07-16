package io.github.isuru.oasis.services;

import io.github.isuru.oasis.services.backend.FlinkServices;

public class OasisServer {

    public static void main(String[] args) throws Exception {
        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init();


    }

}
