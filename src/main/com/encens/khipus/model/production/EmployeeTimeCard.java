package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.warehouse.SubGroup;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for Employee Time CArd
 *
 * @author Ariel Siles Encinas
 * @version 1.1.5
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "EmployeeTimeCard.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "employeeTimeCard",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

/*@NamedQueries(
        {
                @NamedQuery(name = "EmployeeTimeCard.findEmployeeTimeCardByProductionOrder", query = "select e from EmployeeTimeCard e where e.productionOrder=:productionOrder ")
        })*/
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "TARJETATIEMPOEMPLEADO")
public class EmployeeTimeCard implements BaseModel {

    @Id
    @Column(name = "IDTARJETATIEMPOEMPLEADO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "EmployeeTimeCard.tableGenerator")
    private Long id;

    @Column(name = "FECHA", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "HORAINICIO", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "HORAFIN", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "FINJORNADA", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDay;

    @Column(name = "COSTOPORHORA", nullable = true, columnDefinition = "DECIMAL(16,2)")
    private Double costPerHour;

    @Column(name = "DESCRIPCION", nullable = true)
    @Lob
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDEMPLEADO", nullable = false, updatable = false)
    private Employee employee;

    //@Column(name = "NO_CIA", insertable = false, updatable = false)
    @Column(name = "NO_CIA", nullable = false, insertable = true, updatable = true)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "COD_GRU", nullable = false, updatable = true, insertable = true, length = 3)
    @Length(max = 3)
    private String groupCode;

    @Column(name = "COD_SUB", nullable = false, updatable = true, insertable = true, length = 3)
    @Length(max = 3)
    private String subGroupCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_GRU", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "COD_SUB", nullable = false, updatable = false, insertable = false)
    })
    private SubGroup subGroup;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTIPOTAREAPROD", nullable = false, updatable = false)
    private ProductionTaskType productionTaskType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ProductionTaskType getProductionTaskType() {
        return productionTaskType;
    }

    public void setProductionTaskType(ProductionTaskType productionTaskType) {
        this.productionTaskType = productionTaskType;
    }

    public Double getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(Double costPerHour) {
        this.costPerHour = costPerHour;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getSubGroupCode() {
        return subGroupCode;
    }

    public void setSubGroupCode(String subGroupCode) {
        this.subGroupCode = subGroupCode;
    }

    public SubGroup getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(SubGroup subGroup) {
        this.subGroup = subGroup;
    }

    public Date getEndDay() {
        return endDay;
    }

    public void setEndDay(Date endDay) {
        this.endDay = endDay;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
