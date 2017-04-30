package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.Credit;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Credit
 *
 * @author:
 */

@Name("creditDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CREDIT','VIEW')}")
public class CreditDataModel extends QueryDataModel<Long, Credit> {

    private String firstName;
    private String lastName;

    private static final String[] RESTRICTIONS = {
            "lower(credit.partner.firstName) like concat('%', concat(lower(#{creditDataModel.firstName}), '%'))",
            "lower(credit.partner.lastName) like concat('%', concat(lower(#{creditDataModel.lastName}), '%'))",
            "lower(credit.code) like concat('%', concat(lower(#{creditDataModel.criteria.code}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "credit.creationDate ";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select credit from Credit credit ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
