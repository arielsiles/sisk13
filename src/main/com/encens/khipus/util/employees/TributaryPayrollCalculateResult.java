package com.encens.khipus.util.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;

/**
 * @author
 * @version 3.5
 */
public class TributaryPayrollCalculateResult {
    private Long socialWelfareEntityId;
    private BusinessUnit businessUnit;
    private CostCenter costCenter;
    private JobCategory jobCategory;
    private BigDecimal amount;

    public TributaryPayrollCalculateResult() {
    }

    /**
     * Constructor for payable document registration
     */
    public TributaryPayrollCalculateResult(BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, BigDecimal amount) {
        this.businessUnit = businessUnit;
        this.costCenter = costCenter;
        this.jobCategory = jobCategory;
        this.amount = amount;
    }

    /**
     * Constructor for payable document registration
     */
    public TributaryPayrollCalculateResult(BusinessUnit businessUnit, CostCenter costCenter, BigDecimal patronalProffesionalRiskRetentionAFP, BigDecimal patronalProHomeRetentionAFP, BigDecimal patronalSolidaryRetentionAFP) {
        this.businessUnit = businessUnit;
        this.costCenter = costCenter;
        this.amount = BigDecimalUtil.sum(patronalProffesionalRiskRetentionAFP, patronalProHomeRetentionAFP, patronalSolidaryRetentionAFP);
    }

    /**
     * Constructor for payable document registration
     */
    public TributaryPayrollCalculateResult(BusinessUnit businessUnit, CostCenter costCenter, JobCategory jobCategory, BigDecimal patronalProffesionalRiskRetentionAFP, BigDecimal patronalProHomeRetentionAFP, BigDecimal patronalSolidaryRetentionAFP) {
        this.businessUnit = businessUnit;
        this.costCenter = costCenter;
        this.jobCategory = jobCategory;
        this.amount = BigDecimalUtil.sum(patronalProffesionalRiskRetentionAFP, patronalProHomeRetentionAFP, patronalSolidaryRetentionAFP);
    }

    /**
     * Constructor for general calculation
     */
    public TributaryPayrollCalculateResult(Long socialWelfareEntityId, BigDecimal retentionAFP, BigDecimal patronalProffesionalRiskRetentionAFP, BigDecimal patronalProHomeRetentionAFP, BigDecimal patronalSolidaryRetentionAFP) {
        this.socialWelfareEntityId = socialWelfareEntityId;
        this.amount = BigDecimalUtil.sum(retentionAFP, patronalProffesionalRiskRetentionAFP, patronalProHomeRetentionAFP, patronalSolidaryRetentionAFP);
    }

    /**
     * Constructor for general calculation
     */
    public TributaryPayrollCalculateResult(Long socialWelfareEntityId, BigDecimal amount) {
        this.socialWelfareEntityId = socialWelfareEntityId;
        this.amount = amount;
    }

    public Long getSocialWelfareEntityId() {
        return socialWelfareEntityId;
    }

    public void setSocialWelfareEntityId(Long socialWelfareEntityId) {
        this.socialWelfareEntityId = socialWelfareEntityId;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    @Override
    public String toString() {
        return "TributaryPayrollCalculateResult{" +
                "socialWelfareEntityId=" + socialWelfareEntityId +
                ", businessUnit=" + businessUnit +
                ", costCenter=" + costCenter +
                ", jobCategory=" + jobCategory +
                ", amount=" + amount +
                '}';
    }
}
