package io.github.isuru.oasis.parser;

import io.github.isuru.oasis.model.FieldCalculator;
import org.mvel2.MVEL;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class FieldCalculationParser {

    @SuppressWarnings("unchecked")
    public static List<FieldCalculator> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> calcs = yaml.load(inputStream);
        List<Map<String, Object>> calculations = (List<Map<String, Object>>) calcs.get("calculations");

        List<FieldCalculator> calculators = new LinkedList<>();
        for (Map<String, Object> item: calculations) {
            FieldCalculator calculator = new FieldCalculator();
            calculator.setForEvent((String) item.get("event"));
            calculator.setFieldName((String) item.get("field"));
            calculator.setExpression(MVEL.compileExpression((String) item.get("expression")));

            calculators.add(calculator);
        }
        return calculators;
    }

}
