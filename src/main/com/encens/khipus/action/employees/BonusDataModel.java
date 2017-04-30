package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Bonus;
import com.encens.khipus.model.employees.BonusType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Bonus data model
 *
 * @author
 * @version 2.26
 */
@Name("bonusDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('BONUS','VIEW')}")
public class BonusDataModel extends QueryDataModel<Long, Bonus> {

    @Logger
    private Log log;

    private String description;

    private static final String[] RESTRICTIONS = {
            "lower(bonus.name) like concat('%', concat(lower(#{bonusDataModel.criteria.name}), '%'))",
            "bonus.amount = #{bonusDataModel.criteria.amount}",
            "lower(bonus.description.value) like concat('%', concat(lower(#{bonusDataModel.description}), '%'))",
            "bonus.bonusType = #{bonusDataModel.criteria.bonusType}"
    };

    @Override
    public String getEjbql() {
        return "select bonus from Bonus bonus";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void clear() {
        super.clear();

        description = null;
    }

    @Factory(value = "bonusType", scope = ScopeType.STATELESS)
    public BonusType[] getBonusType() {
        return new BonusType[]{BonusType.SENIORITY_BONUS, BonusType.REGULAR_BONUS};
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
