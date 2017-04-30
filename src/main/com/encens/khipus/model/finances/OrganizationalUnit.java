package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Sector;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for OrganizationalUnit
 *
 * @author
 * @version 1.1.6
 */

@NamedQueries(
        {
                @NamedQuery(name = "OrganizationalUnit.findById", query = "select o from OrganizationalUnit o where o.id =:id"),
                @NamedQuery(name = "OrganizationalUnit.findByCareer", query = "select o from OrganizationalUnit o where o.career =:career"),
                @NamedQuery(name = "OrganizationalUnit.findByLevel", query = "select ou from OrganizationalUnit ou where ou.organizationalLevel.id=:levelId"),
                @NamedQuery(name = "OrganizationalUnit.findByRoot", query = "select ou from OrganizationalUnit ou where ou.organizationalUnitRoot=:organizationalUnitRoot"),
                @NamedQuery(name = "OrganizationalUnit.getOrganizationalUnitRootByBusinessUnit", query = "select ou from OrganizationalUnit ou where ou.organizationalUnitRoot is null and ou.businessUnit=:businessUnit"),
                @NamedQuery(name = "OrganizationalUnit.getOrganizationalUnitRootByBusinessUnitAndSector", query = "select ou from OrganizationalUnit ou where ou.organizationalUnitRoot is null and ou.businessUnit=:businessUnit and ou.sector=:sector"),
                @NamedQuery(name = "OrganizationalUnit.findByLevelAndRoot", query = "select ou from OrganizationalUnit ou where ou.organizationalLevel.id=:levelId and " +
                        "ou.organizationalUnitRoot=:organizationalUnitRoot"),
                @NamedQuery(name = "OrganizationalUnit.countByLevel", query = "select count(ou) from OrganizationalUnit ou where ou.organizationalLevel.id=:levelId"),
                @NamedQuery(name = "OrganizationalUnit.countByRoot", query = "select count(ou) from OrganizationalUnit ou where ou.organizationalUnitRoot=:organizationalUnitRoot"),
                @NamedQuery(name = "OrganizationalUnit.countByLevelAndRoot", query = "select count(ou) from OrganizationalUnit ou where ou.organizationalLevel.id=:levelId and " +
                        "ou.organizationalUnitRoot=:organizationalUnitRoot"),
                @NamedQuery(name = "OrganizationalUnit.findByBusinessUnitOrganizationLevel", query = "SELECT ou FROM OrganizationalUnit ou " +
                        " WHERE ou.businessUnit=:businessUnit AND ou.organizationalLevel =:organizationalLevel")
        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "OrganizationalUnit.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "unidadorganizacional",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "unidadorganizacional")
public class OrganizationalUnit implements BaseModel {

    @Id
    @Column(name = "idunidadorganizacional", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "OrganizationalUnit.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idnivelorganizacional")
    private OrganizationalLevel organizationalLevel;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidadorganizacionalraiz")
    private OrganizationalUnit organizationalUnitRoot;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idsector", nullable = false)
    private Sector sector;

    @OneToMany(mappedBy = "organizationalUnitRoot", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<OrganizationalUnit> organizationalUnitList = new ArrayList<OrganizationalUnit>(0);

    @Column(name = "nombre", length = 200)
    @Length(max = 200)
    private String name;

    @Column(name = "sigla", length = 200)
    @Length(max = 200)
    private String acronym;

    @Column(name = "descripcion", length = 200)
    private String description;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "numerocompania", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "codigocencos", referencedColumnName = "cod_cc", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "numerocompania", updatable = false, length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "codigocencos", length = 6)
    @Length(max = 6)
    private String costCenterCode;

    @Column(name = "planestudio", nullable = true, length = 10)
    private String career;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrganizationalLevel getOrganizationalLevel() {
        return organizationalLevel;
    }

    public void setOrganizationalLevel(OrganizationalLevel organizationalLevel) {
        this.organizationalLevel = organizationalLevel;
    }

    public OrganizationalUnit getOrganizationalUnitRoot() {
        return organizationalUnitRoot;
    }

    public void setOrganizationalUnitRoot(OrganizationalUnit organizationalUnitRoot) {
        this.organizationalUnitRoot = organizationalUnitRoot;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public List<OrganizationalUnit> getOrganizationalUnitList() {
        return organizationalUnitList;
    }

    public void setOrganizationalUnitList(List<OrganizationalUnit> organizationalUnitList) {
        this.organizationalUnitList = organizationalUnitList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getFullName() {
        return getOrganizationalLevel().getName() + " - " + getName();
    }

    @Override
    public String toString() {
        return "OrganizationalUnit{" +
                "id=" + id +
                ", organizationalLevel=" + organizationalLevel +
                ", businessUnit=" + businessUnit +
                ", organizationalUnitRoot=" + organizationalUnitRoot +
                ", sector=" + sector +
                ", name='" + name + '\'' +
                ", acronym='" + acronym + '\'' +
                ", description='" + description + '\'' +
                ", costCenter=" + costCenter +
                ", companyNumber='" + companyNumber + '\'' +
                ", costCenterCode='" + costCenterCode + '\'' +
                ", career='" + career + '\'' +
                ", company=" + company +
                ", version=" + version +
                '}';
    }
}