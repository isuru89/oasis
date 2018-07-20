package io.github.isuru.oasis.model.defs;

/**
 * @author iweerarathna
 */
public class KpiDef {

    private Long id;
    private String name;
    private String event;
    private String field;
    private String expression;

    public String assignName() {
        if (name == null) {
            name = event + "::" + field;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
