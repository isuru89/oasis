package io.github.isuru.oasis.services.backend;

import retrofit2.Retrofit;

public class FlinkServices {

    private FlinkClient flinkClient;

    public void init() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:9090")
                .build();

        flinkClient = retrofit.create(FlinkClient.class);
    }

}
