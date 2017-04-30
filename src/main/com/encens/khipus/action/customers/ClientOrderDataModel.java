package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.ClientOrder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Data model for ClientOrder
 *
 * @author: Ariel Siles Encinas
 */

@Name("clientOrderDataModel")
@Scope(ScopeType.PAGE)
//@Restrict("#{s:hasPermission('CREDIT','VIEW')}")
public class ClientOrderDataModel extends QueryDataModel<Long, ClientOrder> {

    private String documentNumber;
    private String lastName;
    private String maidenName;

    private String type;

    private ClientOrder clientOrder;

    @Override
    public String getEjbql() {
        return  "select clientOrder " +
                " from ClientOrder clientOrder";
    }

    public ClientOrder getClientOrder() {
        return clientOrder;
    }

    public void setClientOrder(ClientOrder clientOrder) {
        this.clientOrder = clientOrder;
    }

    /*@Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    } */

    public void clearClientOrder() {
        setClientOrder(null);
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
