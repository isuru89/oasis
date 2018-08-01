package io.github.isuru.oasis.model.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author iweerarathna
 */
public class OasisUtils {

    public static Map<String, Object> filterKeys(Properties properties, String keyPfx) {
        Map<String, Object> map = new HashMap<>();
        for (Object keyObj : properties.keySet()) {
            String key = String.valueOf(keyObj);
            if (key.startsWith(keyPfx)) {
                Object val = properties.get(key);
                String tmp = key.substring(keyPfx.length());
                map.put(tmp, val);
            }
        }
        return map;
    }

}
