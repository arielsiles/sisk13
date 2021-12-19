package com.encens.khipus.action.admin;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.BranchOffice;
import com.encens.khipus.model.customers.Dosage;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
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
@Name("dosageDataModel")
@Scope(ScopeType.PAGE)
public class DosageDataModel extends QueryDataModel<Long, Dosage> {

    private static final String[] RESTRICTIONS =
            {"lower(dosage.authorizationNumber) like concat('%', concat(lower(#{dosageDataModel.criteria.authorizationNumber}), '%'))"};

    @Override
    public String getEjbql() {
        return "select dosage from Dosage dosage";
    }

    @Create
    public void init() {
        sortProperty = "dosage.id";
        sortAsc = false;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
