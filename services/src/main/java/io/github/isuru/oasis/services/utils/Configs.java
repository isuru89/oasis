package io.github.isuru.oasis.services.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author iweerarathna
 */
public final class Configs {

    public static final String KEY_STORAGE_DIR = "oasis.storage.dir";
    public static final String KEY_EXEC_PARALLELISM = "oasis.exec.parallelism";

    private final Properties props = new Properties();

    public Configs initWithSysProps() {
        props.putAll(System.getProperties());
        return this;
    }

    public Configs init(InputStream inputStream) throws IOException {
        props.load(inputStream);
        return this;
    }

    public File getPath(String key, String defPath) throws FileNotFoundException {
        return getPath(key, defPath, true, true);
    }

    private File getPath(String key, String defPath, boolean validate, boolean autoCreate) throws FileNotFoundException {
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

    public static Configs get() {
        return Holder.INSTANCE;
    }

    private Configs() {}

    private static class Holder {
        private static final Configs INSTANCE = new Configs();
    }
}
