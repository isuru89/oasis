package io.github.isuru.oasis.model.configs;

import io.github.isuru.oasis.model.utils.OasisUtils;

import java.io.*;
import java.util.Properties;

/**
 * @author iweerarathna
 */
public final class Configs implements Serializable {

    private final Properties props = new Properties();

    public boolean isLocal() {
        return OasisUtils.getEnvOr("OASIS_MODE", System.getProperty("oasis.mode", ""))
                .trim()
                .equalsIgnoreCase("local");
    }

    public void append(String key, String value) {
        props.put(key, value);
    }

    public void append(String key, Object value) {
        props.put(key, value);
    }

    public Configs initWithSysProps() {
        props.putAll(System.getProperties());
        return this;
    }

    public Configs init(InputStream inputStream) throws IOException {
        props.load(inputStream);
        return this;
    }

    public Object getObj(String key, Object defObj) {
        return props.getOrDefault(key, defObj);
    }

    public File getPath(String key, String defPath) throws FileNotFoundException {
        return getPath(key, defPath, true, true);
    }

    public File getPath(String key, String defPath, boolean validate, boolean autoCreate) throws FileNotFoundException {
        String pathStr = props.getProperty(key, defPath);
        File path = new File(pathStr);
        if (validate) {
            if (!path.exists()) {
                if (autoCreate) {
                    path.mkdirs();
                } else {
                    throw new FileNotFoundException("The path specified in configuration key '"
                            + key + "' does not exist!");
                }
            }
        }
        return path;
    }

    public int getInt(String key, int defVal) {
        Object val = props.get(key);
        if (val == null) {
            return defVal;
        } else {
            return Integer.parseInt(val.toString());
        }
    }

    public String getStr(String key, String def) {
        return props.getProperty(key, def);
    }

    public boolean getBool(String key, boolean def) {
        Object val = props.get(key);
        if (val == null) {
            return def;
        } else {
            return Boolean.parseBoolean(val.toString());
        }
    }

    public String getStrReq(String key) {
        if (!props.containsKey(key)) {
            throw new IllegalStateException("Requested configuration '" + key + "' does not exist!");
        } else {
            return props.getProperty(key);
        }
    }

    public boolean has(String key) {
        return props.containsKey(key);
    }

    public Properties getProps() {
        return props;
    }

    public static Configs create() {
        return new Configs();
    }

    public static Configs from(Properties properties) {
        Configs configs = Configs.create();
        configs.props.putAll(properties);
        return configs;
    }

//    public static Configs get() {
//        return Holder.INSTANCE;
//    }

    private Configs() {}

    private static class Holder {
        private static final Configs INSTANCE = new Configs();
    }
}
