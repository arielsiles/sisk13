package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Subject;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Subject
 *
 * @author
 */

@Name("subjectDataModel")
@Scope(ScopeType.PAGE)
public class SubjectDataModel extends QueryDataModel<Long, Subject> {
    private static final String[] RESTRICTIONS =
            {"lower(subject.name) like concat('%', concat(lower(#{subjectDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "subject.name";
    }

    @Override
    public String getEjbql() {
        return "select subject from Subject subject";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}