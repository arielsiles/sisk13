package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.service.employees.GeneratedPayrollService;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;

/**
 * GenericPayrollDataModel
 *
 * @author
 * @version 2.10
 */
@Name("genericPayrollDataModel")
public class GenericPayrollDataModel<ID extends Long, T extends BaseModel> extends QueryDataModel<ID, T> {

    @In
    private GeneratedPayrollService generatedPayrollService;
    @In
    private GenericService genericService;
    @In
    private FacesMessages facesMessages;
    public GeneratedPayroll generatedPayroll;
    private String idNumber;
    private String lastName;
    private String maidenName;
    private String firstName;

    public GeneratedPayroll getGeneratedPayroll() {
        if (generatedPayroll == null) {
            generatedPayroll = (GeneratedPayroll) Component.getInstance("generatedPayroll");
        }
        return generatedPayroll;
    }

    public void setGeneratedPayroll(GeneratedPayroll generatedPayroll) {
        this.generatedPayroll = generatedPayroll;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
