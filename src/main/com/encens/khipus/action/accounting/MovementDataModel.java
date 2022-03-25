package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.Movement;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.25
 */
@Name("movementDataModel")
@Scope(ScopeType.PAGE)
public class MovementDataModel extends QueryDataModel<Long, Movement> {

    private Date startDate;
    private Date endDate;
    private Integer number;
    private String name;

    private static final String[] RESTRICTIONS = {
            "movement.number = #{movementDataModel.number}",
            "lower(movement.name) like concat('%', concat(lower(#{movementDataModel.name}), '%'))",
            "movement.date >= #{movementDataModel.startDate}",
            "movement.date <= #{movementDataModel.endDate}"
    };

    @Create
    public void init() {
        sortProperty = "movement.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select movement from Movement movement";
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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
