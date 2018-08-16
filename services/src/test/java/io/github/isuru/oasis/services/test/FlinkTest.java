package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.services.backend.FlinkClient;
import io.github.isuru.oasis.services.backend.FlinkServices;
import io.github.isuru.oasis.services.backend.model.JarListInfo;
import io.github.isuru.oasis.services.backend.model.JarUploadResponse;
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
        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init("http://localhost:8084");

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


    }

}
