package io.github.isuru.oasis.services.utils;

import org.junit.Assert;
import org.junit.Test;

public class CommonsUtils {

    @Test
    public void testAsDouble() {
        Assert.assertEquals(Double.NaN, Commons.asDouble(null), 0.01);
        Assert.assertEquals(3.0, Commons.asDouble(3L), 0.01);
        Assert.assertEquals(5.0, Commons.asDouble(5), 0.01);
        Assert.assertEquals(34.0, Commons.asDouble("34"), 0.01);
        Assert.assertEquals(Double.NaN, Commons.asDouble("abc"), 0.01);
    }

}
