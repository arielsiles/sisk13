package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.BranchOffice;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Role data model
 *
 * @author
 * @version 1.0
 */
@Name("branchOfficeDataModel")
@Scope(ScopeType.PAGE)
public class BranchOfficeDataModel extends QueryDataModel<Long, BranchOffice> {

    private static final String[] RESTRICTIONS =
            {"lower(branchOffice.name) like concat('%', concat(lower(#{branchOfficeDataModel.criteria.name}), '%'))"};

    @Override
    public String getEjbql() {
        return "select branchOffice from BranchOffice branchOffice";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
