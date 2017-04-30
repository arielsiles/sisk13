package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ChristmasPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.2
 */
@Name("christmasPayrollGenerationDataModel")
@Scope(ScopeType.PAGE)
public class ChristmasPayrollGenerationDataModel extends QueryDataModel<Long, ChristmasPayroll> {
    private static final String[] RESTRICTIONS =
            {
                    "christmasPayroll.generatedPayroll=#{accountingRecord.generatedPayroll}",
                    "lower(employee.idNumber) like concat(lower(#{accountingRecord.idNumber}),'%')",
                    "christmasPayroll.liquid >= #{accountingRecord.lesserAmount}",
                    "christmasPayroll.liquid <= #{accountingRecord.higherAmount}",
                    "christmasPayroll.costCenter=#{accountingRecord.costCenter}"};

    @Create
    public void init() {
        sortProperty = "christmasPayroll.employee.lastName";
    }

    @Override
    public String getEjbql() {
        return "SELECT christmasPayroll FROM ChristmasPayroll christmasPayroll " +
                " left join fetch christmasPayroll.employee employee" +
                " WHERE #{true}=#{not empty accountingRecord.generatedPayroll}" +
                " and ((#{accountingRecordAction.managed}=#{false} and christmasPayroll.hasAccountingRecord=#{false} and christmasPayroll.hasActivePayment=#{true}) or " +
                " (#{accountingRecordAction.managed}=#{true} and christmasPayroll.hasAccountingRecord=#{true} and christmasPayroll.hasActivePayment=#{true}))";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}

