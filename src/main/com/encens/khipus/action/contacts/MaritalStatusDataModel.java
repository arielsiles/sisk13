package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.MaritalStatus;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Marital status
 *
 * @author:
 */

@Name("maritalStatusDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('MARITALSTATUS','VIEW')}")
public class MaritalStatusDataModel extends QueryDataModel<Long, MaritalStatus> {

    private static final String[] RESTRICTIONS =
            {"lower(maritalStatus.name) like concat('%', concat(lower(#{maritalStatusDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "maritalStatus.name";
    }

    @Override
    public String getEjbql() {
        return "select maritalStatus from MaritalStatus maritalStatus";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
