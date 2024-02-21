package io.github.oasis.core.configs;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OasisConfigTest {

    @Test
    void testConfigsWithoutEnvVariables() {
        ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
        var oc = new OasisConfigs.Builder().buildFromYamlResource("configs/sample.yaml", clsLoader);

        assertEquals("engine-name-test", oc.get("oasis.engine.id", "abc"));
        assertFalse(oc.hasProperty("oasis.engine.idnx"));
        assertEquals("abc", oc.get("oasis.engine.idnx", "abc"));
        assertFalse(oc.hasProperty("oasis.engine.id.name.ref"));
        assertEquals("abc", oc.get("oasis.engine.id.name.ref", "abc"));
    }

    @Test
    void testConfigsWithEnvVariablesDefaultPrefix() {
        ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
        var envVars = new HashMap<String, String>();
        envVars.put("OASIS_OASIS_ENGINE_ID", "overridden-value");
        envVars.put("OASIS_NX_IN_FILE", "value-2");
        var oc = new OasisConfigs.Builder().withCustomEnvOverrides(envVars)
                .buildFromYamlResource("configs/sample.yaml", clsLoader);

        assertEquals("overridden-value", oc.get("oasis.engine.id", "abc"));
        assertEquals("abc", oc.get("oasis.engine.idnx", "abc"));
        assertFalse(oc.hasProperty("oasis.engine.idnx"));
        assertTrue(oc.getBoolean("oasis.cache.printSql", false));
        assertEquals("value-2", oc.get("nx.in.file", "xxx"));
        assertTrue(oc.hasProperty("nx.in.file"));
        assertEquals(3, oc.getInt("oasis.engine.interval", 99));
        assertEquals("item 2", oc.get("item-array[1]", "xxx"));
        assertEquals("val1", oc.get("it-obj-array[1].obj2.key1", "xxx"));
        assertEquals("xxx", oc.get("it-obj-array[1].obj2.keynx", "xxx"));
        assertEquals(1709384324489L, oc.getLong("it-obj-array[0].obj1.longVal", 0L));
        assertEquals(3.147, oc.getDouble("oasis.engine.pi", 0.0));
    }

    @Test
    void testGetResolvedObject() {
        ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
        var envVars = new HashMap<String, String>();
        envVars.put("OASIS_OASIS_ENGINE_ID", "overridden-value");
        envVars.put("OASIS_OASIS_ENGINE_AUTORESTART", "false");
        envVars.put("OASIS_OASIS_ENGINE_INTERVAL", "100");
        envVars.put("OASIS_OASIS_ENGINE_PI", "4.124");
        var oc = new OasisConfigs.Builder().withCustomEnvOverrides(envVars)
                .buildFromYamlResource("configs/sample.yaml", clsLoader);

        var engineConf = oc.getObject("oasis.engine");
        assertEquals(false, engineConf.get("autoRestart"));
        assertEquals(4.124, engineConf.get("pi"));
        assertEquals(100, engineConf.get("interval"));
        assertEquals("overridden-value", engineConf.get("id"));
    }

    @Test
    void testCustomEnvPrefix() {
        ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
        var envVars = new HashMap<String, String>();
        envVars.put("OAS_OASIS_ENGINE_ID", "overridden-value");
        envVars.put("OAS_OASIS_ENGINE_AUTORESTART", "false");
        envVars.put("OAS_OASIS_ENGINE_INTERVAL", "100");
        envVars.put("OAS_OASIS_ENGINE_PI", "4.124");
        envVars.put("OAS_OASIS_NX_PROPERTY", "nx-value");
        var oc = new OasisConfigs.Builder()
                .withCustomEnvOverrides(envVars)
                .withEnvVariablePrefix("OAS_")
                .buildFromYamlResource("configs/sample.yaml", clsLoader);

        var engineConf = oc.getObject("oasis.engine");
        assertEquals(false, engineConf.get("autoRestart"));
        assertEquals(4.124, engineConf.get("pi"));
        assertEquals(100, engineConf.get("interval"));
        assertEquals("overridden-value", engineConf.get("id"));
        assertEquals("nx-value", oc.get("oasis.nx.property", ""));
    }
}
