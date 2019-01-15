package io.github.isuru.oasis.services.services.backend;

import io.github.isuru.oasis.services.configs.OasisConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.PostConstruct;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class FlinkServices {

    private static final Logger LOG = LoggerFactory.getLogger(FlinkServices.class);

    private final OasisConfigurations configurations;

    private FlinkClient flinkClient;

    @Autowired
    public FlinkServices(OasisConfigurations configurations) {
        this.configurations = configurations;
    }

    @PostConstruct
    public void init() {
        init(configurations.getFlinkURL());
    }

    public void init(String baseUrl) {
        LOG.info("Checking Flink client status...");
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
