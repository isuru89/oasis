package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.services.exception.InputValidationException;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ValueMap {

    private final Map<String, Object> data;

    public ValueMap(Map<String, Object> data) {
        this.data = data;
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public String getStrReq(String key) throws InputValidationException {
        if (data.containsKey(key)) {
            return (String) data.get(key);
        } else {
            throw new InputValidationException("Input request does not contain mandatory parameter '" + key + "'!");
        }
    }

    public String getStr(String key, String def) throws InputValidationException {
        if (data.containsKey(key)) {
            return (String) data.get(key);
        } else {
            return def;
        }
    }

    public long getLongReq(String key) throws InputValidationException {
        if (data.containsKey(key)) {
            return Long.parseLong(data.get(key).toString());
        } else {
            throw new InputValidationException("Input request does not contain mandatory parameter '" + key + "'!");
        }
    }

    public float getFloatReq(String key) throws InputValidationException {
        if (data.containsKey(key)) {
            return Float.parseFloat(data.get(key).toString());
        } else {
            throw new InputValidationException("Input request does not contain mandatory parameter '" + key + "'!");
        }
    }

    public int getIntReq(String key) throws InputValidationException {
        if (data.containsKey(key)) {
            return Integer.parseInt(data.get(key).toString());
        } else {
            throw new InputValidationException("Input request does not contain mandatory parameter '" + key + "'!");
        }
    }

    public long getLong(String key, long defVal) {
        if (data.containsKey(key)) {
            Object val = data.get(key);
            return Long.parseLong(val.toString());
        } else {
            return defVal;
        }
    }

    public float getFloat(String key, float defVal) {
        if (data.containsKey(key)) {
            Object val = data.get(key);
            return Float.parseFloat(val.toString());
        } else {
            return defVal;
        }
    }

    public int getInt(String key, int defVal) {
        if (data.containsKey(key)) {
            Object val = data.get(key);
            return Integer.parseInt(val.toString());
        } else {
            return defVal;
        }
    }
}
