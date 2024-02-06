package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.RawMaterialPayment;
import com.encens.khipus.model.xproduction.RawMaterialPaymentState;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Name("rawMaterialPaymentDataModel")
@Scope(ScopeType.PAGE)
public class RawMaterialPaymentDataModel extends QueryDataModel<Long, RawMaterialPayment> {

    private String firstName;
    private String lastName;
    private Date startDate;
    private Date endDate;
    private RawMaterialPaymentState state;

    private static final String[] RESTRICTIONS = {
            "upper(rawMaterialPayment.producer.firstName) like concat(concat('%',upper(#{rawMaterialPaymentDataModel.firstName})), '%')",
            "upper(rawMaterialPayment.producer.lastName) like concat(concat('%',upper(#{rawMaterialPaymentDataModel.lastName})), '%')",
            "rawMaterialPayment.date >= #{rawMaterialPaymentDataModel.startDate}",
            "rawMaterialPayment.date <= #{rawMaterialPaymentDataModel.endDate}",
            "rawMaterialPayment.state = #{rawMaterialPaymentDataModel.state}"
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public RawMaterialPaymentState getState() {
        return state;
    }

    public void setState(RawMaterialPaymentState state) {
        this.state = state;
    }
}
