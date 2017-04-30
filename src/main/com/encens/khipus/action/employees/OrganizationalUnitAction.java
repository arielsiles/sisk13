package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.Job;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.math.BigDecimal;
import java.util.List;

/**
 * Charge action class
 *
 * @author Ariel Siles Encinas
 * @version 1.0
 */
@Name("organizationalUnitAction")
@Scope(ScopeType.CONVERSATION)
public class OrganizationalUnitAction extends GenericAction<OrganizationalUnit> {

    @Factory(value = "organizationalUnit", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('ORGANIZATIONALUNIT','VIEW')}")
    public OrganizationalUnit initOrganizationalUnit() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "name";
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('ORGANIZATIONALUNIT','CREATE')}")
    public String create() {
        return super.create();
    }

    public Double getAnnualCost(List<Job> jobs) {

        BigDecimal annualCost = BigDecimal.ZERO;

        for (Job job : jobs) {
            annualCost = BigDecimalUtil.subtract(annualCost, job.getSalary().getAmount());
        }
        return annualCost.doubleValue() * 12;
    }

    @SuppressWarnings({"NullableProblems"})
    public void refreshOrganizationalUnitRoot() {
        getInstance().setOrganizationalUnitRoot(null);
    }

    public void refreshBusinessUnit() {
        getInstance().setBusinessUnit(getInstance().getOrganizationalUnitRoot().getBusinessUnit());
    }

    public void assingOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        if (organizationalUnit != null) {
            try {
                organizationalUnit = getService().findById(OrganizationalUnit.class, organizationalUnit.getId());
            } catch (EntryNotFoundException e) {
                return;
            }
            getInstance().setOrganizationalUnitRoot(organizationalUnit);
            getInstance().setBusinessUnit(organizationalUnit.getBusinessUnit());
            getInstance().setSector(organizationalUnit.getSector());
        }
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearOrganizationalUnitRoot() {
        getInstance().setOrganizationalUnitRoot(null);
        getInstance().setBusinessUnit(null);
        getInstance().setSector(null);
    }

    public Object getOrganizationalUnitRootName() {
        return getInstance().getOrganizationalUnitRoot() != null ? getInstance().getOrganizationalUnitRoot().getFullName() : null;
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setCostCenter(costCenter);
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearCostCenter() {
        getInstance().setCostCenter(null);
    }

    public String getCostCenterFullName() {
        return getInstance().getCostCenter() != null ? getInstance().getCostCenter().getFullName() : null;
    }
}