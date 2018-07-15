package com.virtusa.gto.oasis.services;

import com.virtusa.gto.oasis.services.backend.FlinkServices;

public class OasisServer {

    public static void main(String[] args) throws Exception {
        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init();


    }

}
