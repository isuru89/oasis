package io.github.isuru.oasis.model.defs;

import java.util.List;

/**
 * @author iweerarathna
 */
public class KpisDef {

    private List<KpiDef> calculations;

    public List<KpiDef> getCalculations() {
        return calculations;
    }

    public void setCalculations(List<KpiDef> calculations) {
        this.calculations = calculations;
    }
}
