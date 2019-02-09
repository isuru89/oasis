package io.github.isuru.oasis.services.services.injector;

import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.utils.Commons;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ResultFetcherManager {

    private static final Logger LOG = LoggerFactory.getLogger(ResultFetcherManager.class);

    private final ResultFetcher resultFetcher;

    @Autowired
    public ResultFetcherManager(Map<String, ResultFetcher> fetcherMap,
                             OasisConfigurations configurations) {
        String impl = Commons.firstNonNull(configurations.getFetcherImpl(), "local");
        String key = "fetcher" + StringUtils.capitalize(impl);
        resultFetcher = fetcherMap.get(key);
        LOG.info("Loaded fetcher impl: " + resultFetcher.getClass().getName());
    }

    ResultFetcher getResultFetcher() {
        return resultFetcher;
    }
}
