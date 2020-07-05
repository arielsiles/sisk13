package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "Dosage.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "dosificacion",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "dosificacion")
public class Dosage implements BaseModel {

    @Id
    @Column(name = "IDDOSIFICACION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Dosage.tableGenerator")
    private Long id;

    @Column(name = "NROAUTORIZACION", nullable = false)
    private Long authorizationNumber;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "FECHAVENCIMIENTO")
    private Date expirationDate;

    @Column(name="LLAVE")
    private String key;

    @Column(name="ESTADO")
    private String state;

    @Column(name = "activo")
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean active;

    @Column(name = "NUMEROACTUAL", nullable = false)
    private Long currentNumber;

    @Column(name="NITEMPRESA")
    private String companyNit;

    @Column(name="ETIQUETAEMPRESA")
    private String companyLabel;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "FECHAINICIO")
    private Date startDate;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "FECHACONTROL")
    private Date controlDate;

    @Column(name="ETIQUETALEY")
    private String lawLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDSUCURSAL", referencedColumnName = "IDSUCURSAL", nullable = false)
    private BranchOffice branchOffice;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorizationNumber() {
        return authorizationNumber;
    }

    public void setAuthorizationNumber(Long authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(Long currentNumber) {
        this.currentNumber = currentNumber;
    }

    public String getCompanyNit() {
        return companyNit;
    }

    public void setCompanyNit(String companyNit) {
        this.companyNit = companyNit;
    }

    public String getCompanyLabel() {
        return companyLabel;
    }

    public void setCompanyLabel(String companyLabel) {
        this.companyLabel = companyLabel;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getControlDate() {
        return controlDate;
    }

    public void setControlDate(Date controlDate) {
        this.controlDate = controlDate;
    }

    public String getLawLabel() {
        return lawLabel;
    }

    public void setLawLabel(String lawLabel) {
        this.lawLabel = lawLabel;
    }

    public BranchOffice getBranchOffice() {
        return branchOffice;
    }

    public void setBranchOffice(BranchOffice branchOffice) {
        this.branchOffice = branchOffice;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
}
