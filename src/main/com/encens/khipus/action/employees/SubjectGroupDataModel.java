package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.SubjectGroup;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for SubjectGroup
 *
 * @author
 */

@Name("subjectGroupDataModel")
@Scope(ScopeType.PAGE)
public class SubjectGroupDataModel extends QueryDataModel<Long, SubjectGroup> {
    private static final String[] RESTRICTIONS =
            {"lower(subjectGroup.name) like concat('%', concat(lower(#{subjectGroupDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "subjectGroup.name";
    }

    @Override
    public String getEjbql() {
        return "select subjectGroup from SubjectGroup subjectGroup";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}