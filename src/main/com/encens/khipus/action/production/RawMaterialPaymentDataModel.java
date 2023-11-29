package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.RawMaterialPayment;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("rawMaterialPaymentDataModel")
@Scope(ScopeType.PAGE)
public class RawMaterialPaymentDataModel extends QueryDataModel<Long, RawMaterialPayment> {

    private String firstName;
    private String lastName;

    private static final String[] RESTRICTIONS = {
            "upper(rawMaterialPayment.producer.firstName) like concat(concat('%',upper(#{rawMaterialPaymentDataModel.firstName})), '%')",
            "upper(rawMaterialPayment.producer.lastName) like concat(concat('%',upper(#{rawMaterialPaymentDataModel.lastName})), '%')"
    };

    @Create
    public void init() {
        //sortProperty = "";
    }

    @Override
    public String getEjbql() {
        return "select rawMaterialPayment from RawMaterialPayment rawMaterialPayment";
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
