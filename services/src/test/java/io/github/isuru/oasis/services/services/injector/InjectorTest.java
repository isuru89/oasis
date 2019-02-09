package io.github.isuru.oasis.services.services.injector;

import io.github.isuru.oasis.services.model.TestResultFetcher;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InjectorTest {

    @Autowired
    private ResultFetcherManager manager;



    @Before
    public void before() {
        ResultFetcher resultFetcher = manager.getResultFetcher();
        Assertions.assertThat(resultFetcher)
                .isInstanceOf(TestResultFetcher.class);
    }

    @Test
    public void testInjector() {

    }

}
