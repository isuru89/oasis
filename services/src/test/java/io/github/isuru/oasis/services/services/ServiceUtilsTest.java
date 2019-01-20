package io.github.isuru.oasis.services.services;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ServiceUtilsTest {

    @Test
    public void testToList() {
        {
            List<String> list = Arrays.asList("a", "b", "c");
            List<String> result = ServiceUtils.toList(new HashSet<>(list));
            Assertions.assertThat(result)
                    .hasSize(list.size())
                    .containsAll(list);
        }
        {
            List<String> list = Arrays.asList("a", "b");
            List<String> result = ServiceUtils.toList(list);
            Assertions.assertThat(result)
                    .hasSize(list.size())
                    .containsAll(list);
        }
        {
            List<String> result = ServiceUtils.toList(null);
            Assertions.assertThat(result).isNotNull().isEmpty();
        }

    }

    @Test
    public void testOrDefault() {
        Assert.assertEquals(1, ServiceUtils.orDefault(new Integer(1), 3));
        Assert.assertEquals(3, ServiceUtils.orDefault((Integer) null, 3));

        Assert.assertEquals(5, ServiceUtils.orDefault(new Long(5), 3));
        Assert.assertEquals(3, ServiceUtils.orDefault((Long) null, 3));
    }

    @SuppressWarnings("boxing")
    @Test
    public void testIsValid() {
        Assert.assertTrue(ServiceUtils.isValid(Long.valueOf(123L)));
        Assert.assertTrue(ServiceUtils.isValid(new Long(1)));
        Assert.assertTrue(ServiceUtils.isValid(new Long(Long.MAX_VALUE)));
        Assert.assertFalse(ServiceUtils.isValid(new Long(Long.MIN_VALUE)));
        Assert.assertFalse(ServiceUtils.isValid(new Long(0)));

        Assert.assertTrue(ServiceUtils.isValid(new Integer(345)));
        Assert.assertTrue(ServiceUtils.isValid(new Integer(1)));
        Assert.assertTrue(ServiceUtils.isValid(new Integer(Integer.MAX_VALUE)));
        Assert.assertFalse(ServiceUtils.isValid(new Integer(Integer.MIN_VALUE)));
        Assert.assertFalse(ServiceUtils.isValid(new Integer(0)));

        Assert.assertFalse(ServiceUtils.isValid((Integer) null));
        Assert.assertFalse(ServiceUtils.isValid((Long) null));
    }
}
