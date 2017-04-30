package com.encens.khipus.action.production;


import com.encens.khipus.service.production.ProductionOrderCodeGeneratorService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.util.Calendar;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

@Name("productionOrderCodeGenerator")
@AutoCreate
@Synchronized
@Startup
@Scope(ScopeType.APPLICATION)
public class ProductionOrderCodeGenerator {

    private Calendar last;
    private int counter;
    private static final String format = "%ty%tm-%04d";

    @In private ProductionOrderCodeGeneratorService productionOrderCodeGeneratorService;

    @Create
    public void prepare() {
        last = Calendar.getInstance();
        counter = productionOrderCodeGeneratorService.findLasCounter(String.format("%ty%tm-", last.getTime(), last.getTime()));
    }

    public String generateCode() {
        refreshFormat();
        return createCode();
    }

    private String createCode() {
        counter = productionOrderCodeGeneratorService.getCounterCode();
        counter += 1;
        return String.format(format, last.getTime(), last.getTime(), counter);
    }

    private void refreshFormat() {
        Calendar current = Calendar.getInstance();
        if (formatHasToChange(current) == false)
            return;

        last = current;
        counter = 0;
    }

    private boolean formatHasToChange(Calendar current) {
        return current.get(YEAR) != last.get(YEAR) || current.get(MONTH) != last.get(MONTH);
    }
}
