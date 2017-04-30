package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.Client;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * ClientDataModel
 *
 * @author
 * @version 2.0
 */
@Name("clientDataModel")
@Scope(ScopeType.PAGE)
public class ClientDataModel extends QueryDataModel<Long, Client> {

    private static final String[] RESTRICTIONS = {
            "lower(client.name) like concat('%', concat(lower(#{clientDataModel.criteria.name}), '%'))",
            "lower(client.ap) like concat('%', concat(lower(#{clientDataModel.criteria.ap}), '%'))",
            "lower(client.am) like concat('%', concat(lower(#{clientDataModel.criteria.am}), '%'))"
    };

    @Create
    public void init() {
        sortProperty = "client.name";
    }

    @Override
    public String getEjbql() {
        return "select client from Client client";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
