package com.encens.khipus.action.budget;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.budget.BudgetDistributionDetail;
import com.encens.khipus.model.budget.ExpenseBudget;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.service.budget.BudgetDistributionService;
import com.encens.khipus.service.budget.ExpenseBudgetService;
import com.encens.khipus.util.DateUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * This class implements the Scriptlet for the ExpenseBudgetExecutionReport
 *
 * @author
 * @version 2.5
 */
public class ExpenseBudgetExecutionReportScriptlet extends JRDefaultScriptlet {

    private ExpenseBudgetExecutionReportAction expenseBudgetExecutionReportAction = (ExpenseBudgetExecutionReportAction) Component.getInstance("expenseBudgetExecutionReportAction");
    private ExpenseBudgetService expenseBudgetService;
    private Date firstDayOfMonth;
    private Date lastDayOfMonth;
    private Date firstDateOfYear;
    private Map<Long, Map<Month, Double>> businessUnitDistributions;

    public ExpenseBudgetExecutionReportScriptlet() {
        super();

        BudgetDistributionService budgetDistributionService = (BudgetDistributionService) Component.getInstance("budgetDistributionService");
        businessUnitDistributions = budgetDistributionService.getGlobalBudgetDistributionDetailsByGestion(expenseBudgetExecutionReportAction.getGestion());
        expenseBudgetService = (ExpenseBudgetService) Component.getInstance("expenseBudgetService");

        firstDateOfYear = DateUtils.firstDayOfYear(expenseBudgetExecutionReportAction.getGestion().getYear());
        if (expenseBudgetExecutionReportAction.getMovementMonth() != null) {
            firstDayOfMonth = DateUtils.firstDayOfMonth(
                    expenseBudgetExecutionReportAction.getMovementMonth().getValue(),
                    expenseBudgetExecutionReportAction.getGestion().getYear());
            lastDayOfMonth = DateUtils.lastDayOfMonth(
                    expenseBudgetExecutionReportAction.getMovementMonth().getValue(),
                    expenseBudgetExecutionReportAction.getGestion().getYear());
        } else {
            firstDayOfMonth = DateUtils.lastDayOfYear(expenseBudgetExecutionReportAction.getGestion().getYear());
        }
    }

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        Double accumulatedExecution;
        Double monthlyExecution;
        Double monthlyBudget = 0.0;
        Double monthlyDeltaExecution;
        Double currentExecution = null;
        Double annualBudget = ((BigDecimal) this.getFieldValue("expenseBudget.amount")).doubleValue();
        Double annualDeltaExecution;
        String executorUnitCode = String.valueOf(this.getFieldValue("businessUnit.executorUnitCode"));
        String costCenterCode = String.valueOf(this.getFieldValue("costCenter.code"));
        ExpenseBudget expenseBudget = null;

        //accumulatedExpenseBudgetAmount
        BigDecimal accumulatedExecutionObj = expenseBudgetService.getAccumulatedExecutionBetween(executorUnitCode, costCenterCode, firstDateOfYear, firstDayOfMonth, (Long) this.getFieldValue("expenseBudget.id"));

        accumulatedExecution = (accumulatedExecutionObj != null) ? accumulatedExecutionObj.doubleValue() : 0.0;
        this.setVariableValue("accumulatedExecution", accumulatedExecution);

        //accumulatedExpenseBudgetAmountForMonth
        BigDecimal monthlyExecutionObj = expenseBudgetService.getAccumulatedExecutionBetween(executorUnitCode, costCenterCode, firstDayOfMonth, lastDayOfMonth, (Long) this.getFieldValue("expenseBudget.id"));
        monthlyExecution = (monthlyExecutionObj != null) ? monthlyExecutionObj.doubleValue() : 0.0;
        this.setVariableValue("monthlyExecution", monthlyExecution);

        //Monthly budget
        Map<Month, Double> buDistributions = businessUnitDistributions.get((Long) this.getFieldValue("businessUnit.id"));
        Map<Month, Double> customDistributions = new HashMap<Month, Double>();
        Double buDistrib = 0.0;
        try {
            expenseBudget = expenseBudgetService.findById(ExpenseBudget.class, this.getFieldValue("expenseBudget.id"));
        } catch (EntryNotFoundException e) {
            // expense not found
        }
        //load custom distribution if exists
        if (expenseBudget != null && expenseBudget.getBudgetDistribution() != null) {
            List<BudgetDistributionDetail> budgetDistributionDetailList = expenseBudget.getBudgetDistribution().getBudgetDistributionDetailList();
            for (BudgetDistributionDetail budgetDistributionDetail : budgetDistributionDetailList) {
                customDistributions.put(budgetDistributionDetail.getMonth(), budgetDistributionDetail.getPercentDistribution().doubleValue());
            }
            if (customDistributions.containsKey(expenseBudgetExecutionReportAction.getMovementMonth())) {
                buDistrib = customDistributions.get(expenseBudgetExecutionReportAction.getMovementMonth());
            }
        } else {
            if (buDistributions.containsKey(expenseBudgetExecutionReportAction.getMovementMonth())) {
                buDistrib = buDistributions.get(expenseBudgetExecutionReportAction.getMovementMonth());
            }
        }
        monthlyBudget = annualBudget * (buDistrib / 100);
        this.setVariableValue("monthlyBudget", monthlyBudget);


        //monthlyDeltaExecution
        monthlyDeltaExecution = monthlyBudget;
        if (monthlyExecution != null) {
            monthlyDeltaExecution = monthlyBudget - monthlyExecution;
        }
        this.setVariableValue("monthlyDeltaExecution", monthlyDeltaExecution);

        //Current execution = acumulated + monthly
        if (accumulatedExecution != null) {
            currentExecution = accumulatedExecution;
            if (monthlyExecution != null) {
                currentExecution = accumulatedExecution + monthlyExecution;
            }
            this.setVariableValue("currentExecution", currentExecution);
        }
        //AnnualDeltaExecution
        annualDeltaExecution = annualBudget;
        if (currentExecution != null) {
            annualDeltaExecution = annualBudget - currentExecution;
        }
        this.setVariableValue("annualDeltaExecution", annualDeltaExecution);

        //percent
        if (annualDeltaExecution != null) {
            Double percent = annualDeltaExecution * 100 / annualBudget;
            this.setVariableValue("percent", percent);
        }
    }

}
