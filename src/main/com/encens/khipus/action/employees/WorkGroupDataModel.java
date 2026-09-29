package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.WorkGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Name("workGroupDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WORKGROUP','VIEW')}")
public class WorkGroupDataModel extends QueryDataModel<Long, WorkGroup> {

    private static final String[] RESTRICTIONS = {
            "lower(workGroup.name) like concat('%', concat(lower(#{workGroupDataModel.criteria.name}), '%'))",
            "lower(workGroup.area) like concat('%', concat(lower(#{workGroupDataModel.criteria.area}), '%'))"};

    @Create
    public void init() {
        sortProperty = "workGroup.name";
    }

    @Override
    public String getEjbql() {
        return "select workGroup from WorkGroup workGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
