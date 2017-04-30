package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.GeneralPayroll;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * ProfessorsPayrollGenerationDataModel
 *
 * @author
 * @version 2.2
 */
@Name("professorsPayrollGenerationDataModel")
@Scope(ScopeType.PAGE)
public class ProfessorsPayrollGenerationDataModel extends QueryDataModel<Long, GeneralPayroll> {
    private static final String[] RESTRICTIONS =
            {
                    "generalPayroll.generatedPayroll=#{accountingRecord.generatedPayroll}",
                    "lower(generalPayroll.employee.idNumber) like concat(lower(#{accountingRecord.idNumber}),'%')",
                    "generalPayroll.liquid >= #{accountingRecord.lesserAmount}",
                    "generalPayroll.liquid <= #{accountingRecord.higherAmount}",
                    "generalPayroll.costCenter=#{accountingRecord.costCenter}"
            };

    @Create
    public void init() {
//        sortProperty = "employee.lastName,employee.maidenName,employee.firstName";
        sortProperty = "generalPayroll.employee.lastName";
    }

    @Override
    public String getEjbql() {
        return "SELECT generalPayroll FROM GeneralPayroll generalPayroll " +
                " left join fetch generalPayroll.employee employee" +
                " WHERE #{true}=#{not empty accountingRecord.generatedPayroll}" +
                " and ((#{accountingRecordAction.managed}=#{false} and generalPayroll.hasAccountingRecord=#{false} and generalPayroll.hasActivePayment=#{true}) or " +
                " (#{accountingRecordAction.managed}=#{true} and generalPayroll.hasAccountingRecord=#{true} and generalPayroll.hasActivePayment=#{true}))";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
