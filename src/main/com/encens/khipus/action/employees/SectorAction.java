package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.PayrollGenerationType;
import com.encens.khipus.model.employees.Sector;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Actions for Sector
 *
 * @author
 */

@Name("sectorAction")
@Scope(ScopeType.CONVERSATION)
public class SectorAction extends GenericAction<Sector> {

    @Factory(value = "sector", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('SECTOR','VIEW')}")
    public Sector initSector() {
        return getInstance();
    }

    @Factory(value = "payrollGenerationTypeEnum")
    public PayrollGenerationType[] getPayrollGenerationTypeEnum() {
        return PayrollGenerationType.values();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}