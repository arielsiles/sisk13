package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.DiscountRule;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.JobCategory;
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

@Name("discountRuleForInactiveDataModel")
@Scope(ScopeType.PAGE)
public class DiscountRuleForInactiveDataModel extends QueryDataModel<Long, DiscountRule> {
    private Long activeDiscountRuleId;
    private BusinessUnit businessUnit;
    private Gestion gestion;
    private JobCategory jobCategory;

    private static final String[] RESTRICTIONS = {
                    "discountRule.id <> #{discountRuleForInactiveDataModel.activeDiscountRuleId}",
                    "businessUnit = #{discountRuleForInactiveDataModel.businessUnit}",
                    "jobCategory = #{discountRuleForInactiveDataModel.jobCategory}",
                    "gestion = #{discountRuleForInactiveDataModel.gestion}"};


    @Create
    public void init() {
        sortProperty = "discountRule.name";
    }

    @Override
    public String getEjbql() {
        String conditions = composeNullConditions();

        return "SELECT discountRule FROM DiscountRule discountRule " +
                " LEFT JOIN discountRule.gestion gestion " +
                " LEFT JOIN discountRule.businessUnit businessUnit " +
                " LEFT JOIN discountRule.jobCategory jobCategory " +
                (!conditions.isEmpty() ? " WHERE " + conditions : "");
    }


    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    private String composeNullConditions() {
        String conditions = "";

        if (gestion == null) {
            conditions = " gestion IS NULL";
        }

        if (businessUnit == null) {
            if (!conditions.isEmpty()) {
                conditions += " AND ";
            }
            conditions += " businessUnit IS NULL";
        }

        if (jobCategory == null) {
            if (!conditions.isEmpty()) {
                conditions += " AND ";
            }
            conditions += " jobCategory IS NULL";
        }
        return conditions;
    }

    public Long getActiveDiscountRuleId() {
        return activeDiscountRuleId;
    }

    public void setActiveDiscountRuleId(Long activeDiscountRuleId) {
        this.activeDiscountRuleId = activeDiscountRuleId;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }
}
