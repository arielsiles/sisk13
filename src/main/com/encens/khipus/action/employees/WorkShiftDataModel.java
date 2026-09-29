package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.WorkShift;
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
@Name("workShiftDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WORKSHIFT','VIEW')}")
public class WorkShiftDataModel extends QueryDataModel<Long, WorkShift> {

    private static final String[] RESTRICTIONS = {
            "lower(workShift.name) like concat('%', concat(lower(#{workShiftDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "workShift.name";
    }

    @Override
    public String getEjbql() {
        return "select workShift from WorkShift workShift";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
