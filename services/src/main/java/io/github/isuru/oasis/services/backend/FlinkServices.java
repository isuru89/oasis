package io.github.isuru.oasis.services.backend;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class FlinkServices {

    private FlinkClient flinkClient;

    public void init(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        flinkClient = retrofit.create(FlinkClient.class);
    }

    public FlinkClient getFlinkClient() {
        return flinkClient;
    }
}
