package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.InvoicesForm;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Invoices form data model
 *
 * @author
 * @version 2.26
 */
@Name("invoicesFormDataModel")
@Scope(ScopeType.PAGE)
public class InvoicesFormDataModel extends QueryDataModel<Long, InvoicesForm> {

    @Logger
    private Log log;

    private static final String[] RESTRICTIONS = {
            "invoicesForm.payrollGenerationCycle = #{payrollGenerationCycleAction.instance}"
    };

    @Override
    public String getEjbql() {
        return "select invoicesForm from InvoicesForm invoicesForm";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
