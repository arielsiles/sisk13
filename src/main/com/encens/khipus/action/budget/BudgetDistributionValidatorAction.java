package com.encens.khipus.action.budget;

import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.service.budget.BudgetDistributionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author
 * @version 1.0
 */
@Name("budgetDistributionValidatorAction")
@Scope(ScopeType.PAGE)
@AutoCreate
public class BudgetDistributionValidatorAction implements Serializable {
    @Logger
    protected Log log;
    @In
    protected Map<String, String> messages;
    @In
    protected FacesMessages facesMessages;
    @In
    private BudgetDistributionService budgetDistributionService;

    /*
    * Validate the <b>businessUnitList</b>, the <b>businessUnitDistributions</b> must be contain every item
    * of <b>businessUnitList</b>, if this conditions isn't success the method return an invalid(false) result.
    *
    * @return validation result.
    */
    public Boolean validateHasGlobalConfiguration(Gestion gestion, List<Object[]> businessUnitList) {
        Boolean valid = true;
        Set businessUnitDistributions = budgetDistributionService.getGlobalBudgetDistributionDetailsByGestion(gestion).keySet();
        for (Object[] businessUnitData : businessUnitList) {
            if (!businessUnitDistributions.contains(businessUnitData[0])) {
                valid = false;
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "BudgetDistribution.error.businessUnitDistributionNotFound",
                        messages.get("BudgetType.expense"),
                        businessUnitData[1]
                );
            }
        }
        return valid;
    }
}
