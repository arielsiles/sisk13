package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.ControlReport;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for ControlReport
 *
 * @author
 */

@Name("controlReportDataModel")
@Scope(ScopeType.CONVERSATION)
public class ControlReportDataModel extends QueryDataModel<Long, ControlReport> {


    private String idNumber;
    private String lastName;
    private String maidenName;
    private String firstName;

    private static final String[] RESTRICTIONS = {
            "controlReport.generatedPayroll = #{controlReportAction.generatedPayroll}",
            "controlReport.horaryBandContract.jobContract.contract.employee.idNumber like concat(#{controlReportDataModel.idNumber}, '%')",
            "lower(controlReport.horaryBandContract.jobContract.contract.employee.lastName) like concat('%', concat(lower(#{controlReportDataModel.lastName}), '%'))",
            "lower(controlReport.horaryBandContract.jobContract.contract.employee.maidenName) like concat('%', concat(lower(#{controlReportDataModel.maidenName}), '%'))",
            "lower(controlReport.horaryBandContract.jobContract.contract.employee.firstName) like concat('%', concat(lower(#{controlReportDataModel.firstName}), '%'))"};


    @Create
    public void init() {
        sortProperty = "controlReport.horaryBandContract.jobContract.contract.employee.idNumber";
    }

    @Override
    public String getEjbql() {
        return "select controlReport from ControlReport controlReport " +
                "left join fetch controlReport.horaryBandContract horaryBandContract " +
                "left join fetch horaryBandContract.jobContract jobContract" +
                " left join fetch jobContract.contract contract" +
                " left join fetch contract.employee employee";

    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
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