package io.github.isuru.oasis.game.parser;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.FieldCalculator;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.KpisDef;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class KpiParser {

    public static List<FieldCalculator> parse(List<KpiDef> calculations) {
        List<FieldCalculator> calculators = new LinkedList<>();
        for (KpiDef item: calculations) {
            FieldCalculator calculator = new FieldCalculator();
            calculator.setId(item.getId());
            calculator.setPriority(Integer.parseInt(item.getId().toString()));
            calculator.setForEvent(item.getEvent());
            calculator.setFieldName(item.getField());
            calculator.setExpression(Utils.compileExpression(item.getExpression()));

            calculators.add(calculator);
        }
        return calculators;
    }

    public static List<FieldCalculator> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        KpisDef calcs = yaml.loadAs(inputStream, KpisDef.class);
        List<KpiDef> calculations = calcs.getCalculations();

        return parse(calculations);
    }

}
