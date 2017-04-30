package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HoraryBandContract;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Data model for HoraryBandContract
 *
 * @author
 * @version 2.8
 */

@Name("horaryBandContractDataModel")
@Scope(ScopeType.SESSION)
@Restrict("#{s:hasPermission('HORARYBANDCONTRACT','VIEW')}")
public class HoraryBandContractDataModel extends QueryDataModel<Long, HoraryBandContract> {
    private Employee employee;
    private Long employeeId;
    private Cycle cycle;

    private static final String[] RESTRICTIONS = {
            "horaryBandContract.jobContract.contract.employee.id = #{horaryBandContractDataModel.employeeId}",
            "horaryBandContract.horaryBand.initHour >= #{horaryBandContractDataModel.initHour}",
            "horaryBandContract.horaryBand.endHour <= #{horaryBandContractDataModel.endHour}",
            "horaryBandContract.horaryBand.initDay = #{horaryBandContractDataModel.initDay}",
            "horaryBandContract.horaryBand.endDay = #{horaryBandContractDataModel.endDay}",
            "horaryBandContract.initDate >= #{horaryBandContractDataModel.criteria.initDate}",
            "horaryBandContract.endDate <= #{horaryBandContractDataModel.criteria.endDate}",
            "horaryBandContract.jobContract.contract.cycle = #{horaryBandContractDataModel.cycle}"
    };


    private Date initHour;

    private Date endHour;

    private String initDay;

    private String endDay;

    public Date getInitHour() {
        return initHour;
    }

    public void setInitHour(Date initHour) {
        this.initHour = initHour;
    }

    public Date getEndHour() {
        return endHour;
    }

    public void setEndHour(Date endHour) {
        this.endHour = endHour;
    }

    public String getInitDay() {
        return initDay;
    }

    public void setInitDay(String initDay) {
        this.initDay = initDay;
    }

    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee != null) {
            setEmployeeId(employee.getId());
        }
    }

    public void clearEmployee() {
        setEmployee(null);
        setEmployeeId(null);
    }

    public Cycle getCycle() {
        return cycle;
    }

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    @Create
    public void init() {
        sortProperty = "horaryBandContract.initDate";
    }

    @Override
    public String getEjbql() {
        return "select horaryBandContract from HoraryBandContract horaryBandContract";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Override
    public void clear() {
        setEmployee(null);
        setInitDay(null);
        setEndDay(null);
        setInitHour(null);
        setEndHour(null);
        setEmployeeId(null);
        setCycle(null);
        super.clear();
        update();
        search();
    }

}