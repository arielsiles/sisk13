package com.encens.khipus.service.production;

import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.Group;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Ariel Siles Encias
 */
@Local
public interface EmployeeTimeCardService {
    BigDecimal costProductionOrder(ProductionOrder productionOrder);

    public Double getCostPerHour(Employee employee);

    public List<ProductionTaskType> getTaskTypeGroup(Group group);

    public List<ConfigGroup> getConfigGroupsProduction();

    public List<ProductionTaskType> getTaskType();

    public Date getLastMark(Employee employeeSelect);

    public EmployeeTimeCard getLastEmployeeTimeCard(Employee employeeSelect);

    public BigDecimal getCostProductionOrder(ProductionOrder productionOrder, Date dateOrder, Double totalVolumDay);

    public List<EmployeeTimeCard> getLastTimesCards();

    public List<EmployeeTimeCard> getLastTimesCardsEmployee(Employee employeeSelect);

    public Double getTotalVolumeOrder(ProductionOrder productionOrder);

    public Double getTotalVolumeSingle(SingleProduct single);
}
