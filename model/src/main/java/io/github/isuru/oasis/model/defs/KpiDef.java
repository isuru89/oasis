package io.github.isuru.oasis.model.defs;

/**
 * @author iweerarathna
 */
public class KpiDef extends BaseDef {

    private String event;
    private String field;
    private String expression;

    public String assignName() {
        if (getName() == null) {
            setName(event + "::" + field);
        }
        return getName();
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
