package io.github.isuru.oasis.model;

/**
 * @author iweerarathna
 */
public enum AggregatorType {

    SUM(true),
    COUNT(true),

    AVG(true),
    MIN(false),
    MAX(false);

    AggregatorType(boolean multiAggregatable) {
        this.multiAggregatable = multiAggregatable;
    }

    // whether this aggregation type can handle multiple heterogeneous tables at once
    // for eg: for now OA_POINT, and OA_STATE
    private final boolean multiAggregatable;

    public static AggregatorType from(String text) {
        for (AggregatorType val : values()) {
            if (val.name().equalsIgnoreCase(text)) {
                return val;
            }
        }
        return null;
    }

    public boolean isMultiAggregatable() {
        return multiAggregatable;
    }}
