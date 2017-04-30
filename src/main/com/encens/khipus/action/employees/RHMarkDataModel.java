package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.RHMark;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for RegisterMarkAction
 *
 * @author
 */

@Name("rHMarkDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('RHMARK','VIEW')}")
public class RHMarkDataModel extends QueryDataModel<Long, RHMark> {

    private RHMark criteria;

    private static final String[] RESTRICTIONS = {
            "rHMark.marRefCard like concat(#{rHMarkDataModel.criteria.marRefCard}, '%')",
            "rHMark.marDate = #{rHMarkDataModel.criteria.marDate}"};

    @Create
    public void init() {
        sortProperty = "rHMark.marRefCard";
        criteria = new RHMark();
        criteria.setMarDate(null);
    }

    @Override
    public String getEjbql() {
        return "select rHMark from RegisterMarkAction rHMark";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void setCriteria(RHMark criteria) {
        this.criteria = criteria;
    }

    @Override
    public RHMark getCriteria() {
        return this.criteria;
    }
}