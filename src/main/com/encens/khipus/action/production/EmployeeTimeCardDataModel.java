package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.EmployeeTimeCard;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Employee Time Card Data Model
 *
 * @author Ariel Siles Encinas
 * @version 2.5
 */
@Name("employeeTimeCardDataModel")
@Scope(ScopeType.PAGE)
public class EmployeeTimeCardDataModel extends QueryDataModel<Long, EmployeeTimeCard> {

    private static final String[] RESTRICTIONS = {""};

    @Override
    public String getEjbql() {
        return "select employeeTimeCard from EmployeeTimeCard employeeTimeCard";
    }

    /*@Create
    public void defaultSort() {
        sortProperty = "startTime";
    }*/

    /*@Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }*/


}