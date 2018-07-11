package io.github.isuru.oasis.process;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.utils.Utils;
import io.github.isuru.oasis.model.FieldCalculator;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class FieldInjector<E extends Event> implements MapFunction<E, E> {

    private final List<FieldCalculator> fieldCalculatorList;

    public FieldInjector(List<FieldCalculator> fieldCalculatorList) {
        this.fieldCalculatorList = Utils.isNonEmpty(fieldCalculatorList) ?
                fieldCalculatorList : new LinkedList<>();
    }

    @Override
    public E map(E value) {
        if (Utils.isNonEmpty(fieldCalculatorList)) {
            for (FieldCalculator fc : fieldCalculatorList) {
                if (fc.getForEvent().equals(value.getEventType())) {
                    Object evaluated = evaluate(fc, value.getAllFieldValues());
                    value.setFieldValue(fc.getFieldName(), evaluated);
                }
            }
        }
        return value;
    }

    private static Object evaluate(FieldCalculator fieldCalculator, Map<String, Object> vars) {
        return Utils.executeExpression(fieldCalculator.getExpression(), vars);
    }
}
