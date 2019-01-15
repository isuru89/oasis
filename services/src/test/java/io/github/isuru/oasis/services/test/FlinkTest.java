package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.services.backend.FlinkClient;
import io.github.isuru.oasis.services.services.backend.FlinkServices;
import io.github.isuru.oasis.services.services.backend.model.JarListInfo;
import io.github.isuru.oasis.services.services.backend.model.JarUploadResponse;
import io.github.isuru.oasis.services.services.backend.model.JobsStatusResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author iweerarathna
 */
class FlinkTest {



    @Test
    void testFlinkApi() throws IOException {
        OasisConfigurations oasisConfigurations = new OasisConfigurations();
        oasisConfigurations.setFlinkURL("http://localhost:8084");
        FlinkServices flinkServices = new FlinkServices(oasisConfigurations);

        FlinkClient flinkClient = flinkServices.getFlinkClient();
        JarListInfo jarListInfo = flinkClient.getJars().blockingFirst();
        System.out.println(jarListInfo);
        Assertions.assertTrue(jarListInfo.getFiles().isEmpty());

        File file = new File("../game/target/game.jar").getCanonicalFile();
        RequestBody body = RequestBody.create(MediaType.parse("application/x-java-archive"), file);
        MultipartBody.Part formData = MultipartBody.Part.createFormData("oasis-game",
                file.getName(),
                body);
        JarUploadResponse jarUploadResponse = flinkClient.uploadJar(formData).blockingSingle();
        System.out.println(jarUploadResponse);
        Assertions.assertTrue(jarUploadResponse.isSuccess());

        JarListInfo jarLists = flinkClient.getJars().blockingSingle();
        System.out.println(jarLists);
        Assertions.assertEquals(1, jarLists.getFiles().size());

        String id = jarLists.getFiles().get(0).getId();
        flinkClient.deleteJar(id).blockingAwait();

        JarListInfo tmp = flinkClient.getJars().blockingSingle();
        System.out.println(tmp);
        Assertions.assertEquals(0, tmp.getFiles().size());

        JobsStatusResponse jobsStatusResponse = flinkClient.jobs().blockingSingle();
        System.out.println(jobsStatusResponse);
        Assertions.assertTrue(jobsStatusResponse.getJobs().isEmpty());
    }

}
