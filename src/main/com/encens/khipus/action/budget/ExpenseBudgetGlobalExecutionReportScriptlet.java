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
 * ExpenseBudgetGlobalExecutionReportScriptlet
 *
 * @author
 * @version 2.7
 */
public class ExpenseBudgetGlobalExecutionReportScriptlet extends JRDefaultScriptlet {
    private ExpenseBudgetGlobalExecutionReportAction expenseBudgetGlobalExecutionReportAction = (ExpenseBudgetGlobalExecutionReportAction) Component.getInstance("expenseBudgetGlobalExecutionReportAction");
    private ExpenseBudgetService expenseBudgetService;
    private Date firstDayOfMonth;
    private Date lastDayOfMonth;
    private Date firstDateOfYear;
    private Map<Month, Double> globalDistributions;

    public ExpenseBudgetGlobalExecutionReportScriptlet() {

        super();
        Component a;
        BudgetDistributionService budgetDistributionService = (BudgetDistributionService) Component.getInstance("budgetDistributionService");
        globalDistributions = budgetDistributionService.getGlobalBudgetDistributionByGestion(expenseBudgetGlobalExecutionReportAction.getGestion());
        expenseBudgetService = (ExpenseBudgetService) Component.getInstance("expenseBudgetService");

        firstDateOfYear = DateUtils.firstDayOfYear(expenseBudgetGlobalExecutionReportAction.getGestion().getYear());
        if (expenseBudgetGlobalExecutionReportAction.getMovementMonth() != null) {
            firstDayOfMonth = DateUtils.firstDayOfMonth(
                    expenseBudgetGlobalExecutionReportAction.getMovementMonth().getValue(),
                    expenseBudgetGlobalExecutionReportAction.getGestion().getYear());
            lastDayOfMonth = DateUtils.lastDayOfMonth(
                    expenseBudgetGlobalExecutionReportAction.getMovementMonth().getValue(),
                    expenseBudgetGlobalExecutionReportAction.getGestion().getYear());
        } else {
            firstDayOfMonth = DateUtils.lastDayOfYear(expenseBudgetGlobalExecutionReportAction.getGestion().getYear());
        }
    }

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        Double accumulatedExecution;
        Double monthlyExecution;
        Double monthlyBudget = 0.0;
        Double monthlyDeltaExecution;
        Double currentExecution = null;
        Double annualDeltaExecution;
        Long classifierId = Long.valueOf(String.valueOf(this.getFieldValue("classifier.id")));
        ExpenseBudget expenseBudget = null;

        //annualBudgetAmount
        BigDecimal annualBudgetBigDecimal = expenseBudgetService.getExpenseBudgetAmountByClassifier(classifierId, expenseBudgetGlobalExecutionReportAction.getGestion().getId());
        Double annualBudget = (annualBudgetBigDecimal != null) ? annualBudgetBigDecimal.doubleValue() : 0.0;
        this.setVariableValue("annualBudget", annualBudget);

        //accumulatedExpenseBudgetAmount
        BigDecimal accumulatedExecutionObj = expenseBudgetService.getAccumulatedExecutionByClassifier(classifierId, firstDateOfYear, firstDayOfMonth);

        accumulatedExecution = (accumulatedExecutionObj != null) ? accumulatedExecutionObj.doubleValue() : 0.0;
        this.setVariableValue("accumulatedExecution", accumulatedExecution);

        //accumulatedExpenseBudgetAmountForMonth
        BigDecimal monthlyExecutionObj = expenseBudgetService.getAccumulatedExecutionByClassifier(classifierId, firstDayOfMonth, lastDayOfMonth);
        monthlyExecution = (monthlyExecutionObj != null) ? monthlyExecutionObj.doubleValue() : 0.0;
        this.setVariableValue("monthlyExecution", monthlyExecution);

        //Monthly budget
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
            if (customDistributions.containsKey(expenseBudgetGlobalExecutionReportAction.getMovementMonth())) {
                buDistrib = customDistributions.get(expenseBudgetGlobalExecutionReportAction.getMovementMonth());
            }
        } else {
            if (globalDistributions.containsKey(expenseBudgetGlobalExecutionReportAction.getMovementMonth())) {
                buDistrib = globalDistributions.get(expenseBudgetGlobalExecutionReportAction.getMovementMonth());
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
