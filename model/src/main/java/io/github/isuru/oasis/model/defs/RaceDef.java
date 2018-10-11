package io.github.isuru.oasis.model.defs;

import java.util.Map;

public class RaceDef {

    private Long id;
    private String name;
    private String displayName;

    private String timespan;    // daily, weekly, monthly

    private String condition;
    private String event;
    private String aggregator;  // SUM, MAX, MIN, AVG, COUNT
    private String accumulator;

    private String groupingScope;   // global, teamScope, team

    private Integer top;
    private Integer bottom;

    private Map<Integer, Double> points;
    private Map<Integer, String> badges;


}
