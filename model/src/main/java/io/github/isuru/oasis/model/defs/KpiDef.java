package io.github.isuru.oasis.model.defs;

/**
 * @author iweerarathna
 */
public class KpiDef {

    private Long id;
    private String event;
    private String field;
    private String expression;

    public String getName() {
        return event + "::" + field;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
