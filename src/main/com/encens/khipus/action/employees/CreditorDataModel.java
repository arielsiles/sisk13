package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Creditor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Creditor data model
 *
 * @author Ariel Siles Encinas
 * @version 1.0
 */
@Name("creditorDataModel")
@Scope(ScopeType.PAGE)
public class CreditorDataModel extends QueryDataModel<Long, Creditor> {

    private static final String[] RESTRICTIONS =
            {"lower(creditor.name) like concat(lower(#{creditorDataModel.criteria.name}), '%')"};

    @Override
    public String getEjbql() {
        return "select creditor from Creditor creditor";
    }

    @Create
    public void defaultSort() {
        sortProperty = "creditor.name";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}