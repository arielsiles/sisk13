package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Salutation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Salutation
 *
 * @author:
 */

@Name("salutationDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('SALUTATION','VIEW')}")
public class SalutationDataModel extends QueryDataModel<Long, Salutation> {

    private static final String[] RESTRICTIONS =
            {"lower(salutation.name) like concat('%', concat(#{salutationDataModel.criteria.name}, '%'))"};

    @Create
    public void init() {
        sortProperty = "salutation.name";
    }

    @Override
    public String getEjbql() {
        return "select salutation from Salutation salutation";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
