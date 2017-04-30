package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.FiscalProfessorPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@Name("fiscalProfessorPayrollGenerationDataModel")
@Scope(ScopeType.PAGE)
public class FiscalProfessorPayrollGenerationDataModel extends QueryDataModel<Long, FiscalProfessorPayroll> {
    private static final String[] RESTRICTIONS =
            {
                    "fiscalProfessorPayroll.generatedPayroll=#{accountingRecord.generatedPayroll}",
                    "lower(employee.idNumber) like concat(lower(#{accountingRecord.idNumber}),'%')",
                    "fiscalProfessorPayroll.liquid >= #{accountingRecord.lesserAmount}",
                    "fiscalProfessorPayroll.liquid <= #{accountingRecord.higherAmount}",
                    "fiscalProfessorPayroll.costCenter=#{accountingRecord.costCenter}"};

    @Create
    public void init() {
        sortProperty = "fiscalProfessorPayroll.employee.lastName";
    }

    @Override
    public String getEjbql() {
        return "SELECT fiscalProfessorPayroll FROM FiscalProfessorPayroll fiscalProfessorPayroll " +
                " left join fetch fiscalProfessorPayroll.employee employee" +
                " WHERE #{true}=#{not empty accountingRecord.generatedPayroll}" +
                " and ((#{accountingRecordAction.managed}=#{false} and fiscalProfessorPayroll.hasAccountingRecord=#{false} and fiscalProfessorPayroll.hasActivePayment=#{true}) or " +
                " (#{accountingRecordAction.managed}=#{true} and fiscalProfessorPayroll.hasAccountingRecord=#{true} and fiscalProfessorPayroll.hasActivePayment=#{true}))";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}

