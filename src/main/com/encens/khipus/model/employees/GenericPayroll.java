package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.CostCenter;

import java.math.BigDecimal;


/**
 * GenericPayroll
 *
 * @author
 * @version 2.19
 */
public interface GenericPayroll extends BaseModel {

    Employee getEmployee();

    GeneratedPayroll getGeneratedPayroll();

    Boolean getHasAccountingRecord();

    Boolean getHasActivePayment();

    Boolean getActiveForTaxPayrollGeneration();

    BusinessUnit getBusinessUnit();

    CostCenter getCostCenter();

    Charge getCharge();

    JobCategory getJobCategory();

    BigDecimal getTardinessMinutesDiscount();

    BigDecimal getLoanDiscount();

    BigDecimal getAdvanceDiscount();

    BigDecimal getAfp();

    BigDecimal getRciva();

    BigDecimal getWinDiscount();

    BigDecimal getOtherDiscounts();

    BigDecimal getDiscountsOutOfRetention();
}
