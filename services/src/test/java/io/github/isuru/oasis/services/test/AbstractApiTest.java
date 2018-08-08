package io.github.isuru.oasis.services.test;

import org.junit.jupiter.api.Assertions;

/**
 * @author iweerarathna
 */
public abstract class AbstractApiTest {


    public void assertFail(RunnableEx runnable, Class<? extends Exception> exceptionType) {
        try {
            runnable.run();
            Assertions.fail("The execution expected to fail, but success!");
        } catch (Throwable t) {
            if (exceptionType.isAssignableFrom(t.getClass())) {
                return;
            }
            Assertions.fail(String.format("Expected '%s' but got '%s'!", exceptionType.getName(),
                    t.getClass().getName()));
        }
    }

    @FunctionalInterface
    public static interface RunnableEx {
        void run() throws Exception;
    }

}
