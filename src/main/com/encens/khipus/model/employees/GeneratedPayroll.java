package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.ExchangeRate;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for GENERATED PAYROLL
 *
 * @author
 * @version 2.26
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "GeneratedPayroll.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "planillagenerada",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "GeneratedPayroll.load", query = "select o from GeneratedPayroll o " +
                        " left join fetch o.gestionPayroll gestionPayroll" +
                        " left join fetch gestionPayroll.payrollGenerationCycle payrollGenerationCycle" +
                        " where o.id=:generatedPayrollId"),
                @NamedQuery(name = "GeneratedPayroll.findAll", query = "select o from GeneratedPayroll o order by o.id asc"),
                @NamedQuery(name = "GeneratedPayroll.findGeneratedPayrollById", query = "select o from GeneratedPayroll" +
                        " o where o.id =:id"),
                @NamedQuery(name = "GeneratedPayroll.findGeneratedPayrollByName", query = "select o from GeneratedPayroll" +
                        " o where o.name like:name"),
                @NamedQuery(name = "GeneratedPayroll.findGeneratedPayrollByGestionPayroll", query = "select o from GeneratedPayroll" +
                        " o where o.gestionPayroll=:gestionPayroll and o.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GeneratedPayroll.countGeneratedPayrollByName", query = "select count(o) from GeneratedPayroll" +
                        " o where o.name like:name"),
                @NamedQuery(name = "GeneratedPayroll.countGeneratedPayrollByGestionPayroll", query = "select count(o) from GeneratedPayroll" +
                        " o where o.gestionPayroll=:gestionPayroll and o.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GeneratedPayroll.countOfficialGeneratedPayrollByBusinessUnitAndGestionAndMonthAndJobCategoryAndGestionPayrollTypeAndGeneratedPayrollType",
                        query = "select count(o) from GeneratedPayroll" +
                                " o where o.gestionPayroll.businessUnit=:businessUnit " +
                                "and o.gestionPayroll.gestion=:gestion and o.gestionPayroll.month=:month " +
                                "and o.gestionPayroll.jobCategory=:jobCategory and o.gestionPayroll.gestionPayrollType=:gestionPayrollType " +
                                " and o.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GeneratedPayroll.findGeneratedPayrollsByGestionPayroll", query = "select o from GeneratedPayroll" +
                        " o where o.gestionPayroll=:gestionPayroll"),
                @NamedQuery(name = "GeneratedPayroll.findGeneratedPayrollsByGestionPayrollAndGeneratedPayrollType",
                        query = "select generatedPayroll from GeneratedPayroll generatedPayroll " +
                                " where generatedPayroll.gestionPayroll=:gestionPayroll and generatedPayroll.generatedPayrollType=:generatedPayrollType "),
                @NamedQuery(name = "GeneratedPayroll.countGeneratedPayrolls", query = "select count(o.id) from GeneratedPayroll" +
                        " o where o.gestionPayroll=:gestionPayroll"),
                @NamedQuery(name = "GeneratedPayroll.findGeneratedPayrollsByGestion", query = "select o from GeneratedPayroll" +
                        " o where o.gestionPayroll.gestion=:gestion"),
                @NamedQuery(name = "GeneratedPayroll.findGeneratedPayrollsByGestionAndType", query = "select o from GeneratedPayroll" +
                        " o where o.gestionPayroll.gestion=:gestion and o.gestionPayroll.month=:month and o.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GeneratedPayroll.countGeneratedPayrollsByGestionAndType", query = "select count(o) from GeneratedPayroll" +
                        " o where o.gestionPayroll.gestion=:gestion and o.gestionPayroll.month=:month and o.generatedPayrollType=:generatedPayrollType"),
                @NamedQuery(name = "GeneratedPayroll.setTestToOutdatedGeneratedPayrollButCurrentByGestionPayroll",
                        query = "update GeneratedPayroll o set o.generatedPayrollType=:outdated where o.gestionPayroll =:gestionPayroll " +
                                " and o.id<>:generatedPayrollId and o.generatedPayrollType=:test"),
                @NamedQuery(name = "GeneratedPayroll.findByJobCategoryGestionMonthGeneratedPayrollTypeBusinessUnitId",
                        query = "select gp from GeneratedPayroll gp " +
                                " where gp.gestionPayroll.jobCategory =:jobCategory " +
                                " and gp.gestionPayroll.gestion =:gestion " +
                                " and gp.gestionPayroll.month =:month " +
                                " and gp.generatedPayrollType =:generatedPayrollType " +
                                " and gp.gestionPayroll.businessUnit.id=:businessUnitId")

        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "planillagenerada")
public class GeneratedPayroll implements BaseModel, Cloneable {

    @Id
    @Column(name = "idplanillagenerada", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GeneratedPayroll.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200, unique = true)
    @Length(max = 200)
    private String name;

    @Column(name = "fechageneracion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date generationDate = new Date();

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idtipocambio", nullable = false, updatable = false)
    private ExchangeRate exchangeRate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idgestionplanilla", nullable = false, updatable = false, insertable = true)
    private GestionPayroll gestionPayroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCICLOGENERACIONPLANILLA")
    private PayrollGenerationCycle payrollGenerationCycle;

    @Column(name = "idgestionplanilla", updatable = false, insertable = false)
    private Long gestionPayrollId;

    @OneToMany(mappedBy = "generatedPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<GeneralPayroll> generalPayrollList = new ArrayList<GeneralPayroll>(0);

    @OneToMany(mappedBy = "generatedPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<ControlReport> controlReportList = new ArrayList<ControlReport>(0);

    @OneToMany(mappedBy = "generatedPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<ManagersPayroll> managersPayrollList = new ArrayList<ManagersPayroll>(0);

    @OneToMany(mappedBy = "generatedPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<FiscalProfessorPayroll> fiscalProfessorPayrollList = new ArrayList<FiscalProfessorPayroll>();

    @OneToMany(mappedBy = "generatedPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<ChristmasPayroll> christmasPayrollList = new ArrayList<ChristmasPayroll>(0);

    @OneToMany(mappedBy = "generatedPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<CategoryTributaryPayroll> categoryTributaryPayrollList = new ArrayList<CategoryTributaryPayroll>(0);

    @OneToMany(mappedBy = "generatedPayroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<CategoryFiscalPayroll> categoryFiscalPayrollList = new ArrayList<CategoryFiscalPayroll>(0);

    @Column(name = "tipoplanillagen", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private GeneratedPayrollType generatedPayrollType;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ManagersPayroll> getManagersPayrollList() {
        return managersPayrollList;
    }

    public void setManagersPayrollList(List<ManagersPayroll> managersPayrollList) {
        this.managersPayrollList = managersPayrollList;
    }

    public List<FiscalProfessorPayroll> getFiscalProfessorPayrollList() {
        return fiscalProfessorPayrollList;
    }

    public void setFiscalProfessorPayrollList(List<FiscalProfessorPayroll> fiscalProfessorPayrollList) {
        this.fiscalProfessorPayrollList = fiscalProfessorPayrollList;
    }

    public List<ChristmasPayroll> getChristmasPayrollList() {
        return christmasPayrollList;
    }

    public void setChristmasPayrollList(List<ChristmasPayroll> christmasPayrollList) {
        this.christmasPayrollList = christmasPayrollList;
    }

    public List<CategoryTributaryPayroll> getCategoryTributaryPayrollList() {
        return categoryTributaryPayrollList;
    }

    public void setCategoryTributaryPayrollList(List<CategoryTributaryPayroll> categoryTributaryPayrollList) {
        this.categoryTributaryPayrollList = categoryTributaryPayrollList;
    }

    public List<CategoryFiscalPayroll> getCategoryFiscalPayrollList() {
        return categoryFiscalPayrollList;
    }

    public void setCategoryFiscalPayrollList(List<CategoryFiscalPayroll> categoryFiscalPayrollList) {
        this.categoryFiscalPayrollList = categoryFiscalPayrollList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GeneralPayroll> getGeneralPayrollList() {
        return generalPayrollList;
    }

    public void setGeneralPayrollList(List<GeneralPayroll> generalPayrollList) {
        this.generalPayrollList = generalPayrollList;
    }

    public GeneratedPayrollType getGeneratedPayrollType() {
        return generatedPayrollType;
    }

    public void setGeneratedPayrollType(GeneratedPayrollType generatedPayrollType) {
        this.generatedPayrollType = generatedPayrollType;
    }

    public Boolean getOfficialPayrollType() {
        return GeneratedPayrollType.OFFICIAL.equals(getGeneratedPayrollType());
    }

    public Boolean getTestPayrollType() {
        return GeneratedPayrollType.TEST.equals(getGeneratedPayrollType());
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public GestionPayroll getGestionPayroll() {
        return gestionPayroll;
    }

    public void setGestionPayroll(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    public Long getGestionPayrollId() {
        return gestionPayrollId;
    }

    public void setGestionPayrollId(Long gestionPayrollId) {
        this.gestionPayrollId = gestionPayrollId;
    }

    public List<ControlReport> getControlReportList() {
        return controlReportList;
    }

    public void setControlReportList(List<ControlReport> controlReportList) {
        this.controlReportList = controlReportList;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Object clone() throws CloneNotSupportedException {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException ex) {
            System.out.println(ex.toString());
        }
        return obj;
    }

    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override
    public String toString() {
        return "GeneratedPayroll{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", generationDate=" + generationDate +
                '}';
    }
}