package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.services.backend.FlinkClient;
import io.github.isuru.oasis.services.backend.FlinkServices;
import io.github.isuru.oasis.services.backend.model.JarListInfo;
import org.junit.jupiter.api.Test;

/**
 * @author iweerarathna
 */
class FlinkTest {

    @Test
    void testFlinkApi() {
        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init("http://localhost:8089");

        FlinkClient flinkClient = flinkServices.getFlinkClient();
        JarListInfo jarListInfo = flinkClient.getJars().blockingFirst();
        System.out.println(jarListInfo);
    }

}
