package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.SalaryMovementProducer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/29/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("salaryMovementProducerDataModel")
@Scope(ScopeType.PAGE)
public class SalaryMovementProducerDataModel extends QueryDataModel<Long, SalaryMovementProducer> {

    private Date startDate;
    private Date endDate;
    private String firstName;
    private String lastName;
    private String maidenName;

    private static final String[] RESTRICTIONS = {
            "salaryMovementProducer.date >= #{salaryMovementProducerDataModel.startDate}",
            "salaryMovementProducer.date <= #{salaryMovementProducerDataModel.endDate}",
            "salaryMovementProducer.typeMovementProducer = #{salaryMovementProducerDataModel.criteria.typeMovementProducer}",
            "upper(rawMaterialProducer.firstName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.firstName})), '%')",
            "upper(rawMaterialProducer.lastName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.lastName})), '%')",
            "upper(rawMaterialProducer.maidenName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.maidenName})), '%')"
    };

    /*private static final String[] RESTRICTIONS = {
            "salaryMovementProducer.date >= #{salaryMovementProducerDataModel.startDate}",
            "salaryMovementProducer.date <= #{salaryMovementProducerDataModel.endDate}",
            *//*"salaryMovementProducer.state = #{salaryMovementProducerDataModel.privateCriteria.state}",*//*
            "upper(rawMaterialProducer.firstName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.firstName})), '%')",
            "upper(rawMaterialProducer.lastName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.lastName})), '%')",
            "upper(rawMaterialProducer.maidenName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.maidenName})), '%')"
    };*/

    @Override
    public String getEjbql() {
        String query = " select salaryMovementProducer " +
                       " from SalaryMovementProducer salaryMovementProducer " +
                       " left join fetch salaryMovementProducer.rawMaterialProducer rawMaterialProducer";
        return query;
    }

    @Create
    public void defaultSort() {
        sortProperty = "salaryMovementProducer.date";
        this.sortAsc = false;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
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

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }
}
