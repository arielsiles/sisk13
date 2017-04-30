package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.KindOfSalary;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * KindOfSalary data model
 *
 * @author
 * @version 1.0
 */
@Name("kindOfSalaryDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('KINDOFSALARY','VIEW')}")
public class KindOfSalaryDataModel extends QueryDataModel<Long, KindOfSalary> {

    private static final String[] RESTRICTIONS =
            {"lower(kindOfSalary.type) like concat('%', concat(lower(#{kindOfSalaryDataModel.criteria.type}), '%'))"};

    @Override
    public String getEjbql() {
        return "select kindOfSalary from KindOfSalary kindOfSalary";
    }

    @Create
    public void defaultSort() {
        sortProperty = "kindOfSalary.type";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}