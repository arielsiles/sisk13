package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 *
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "BranchOffice.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sucursal",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "sucursal")
public class BranchOffice implements BaseModel {

    @Id
    @Column(name = "idsucursal", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BranchOffice.tableGenerator")
    private Long id;

    @Column(name="nombre")
    private String name;

    @Column(name="descripcion")
    private String description;

    @Column(name="actividad")
    private String activity;

    @Column(name="nombre_empresa")
    private String companyName;

    @Column(name="nombre_sucursal")
    private String branchName;

    @Column(name="nombre_pos")
    private String posName;

    @Column(name="telefonos")
    private String phone;

    @Column(name="lugar")
    private String place;

    @Column(name="direccion")
    private String address;

    @Column(name="codsuc")
    private Integer officeCode;

    @Column(name="codpos")
    private Integer posCode;

    @Column(name="docsector")
    private Integer sectorDocumentCode; // 1:FACTURA COMPRA-VENTA

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

    public String getName() {
        return name;
    }

    public void setName(String key) {
        this.name = key;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOfficeCode() {
        return officeCode;
    }

    public void setOfficeCode(Integer officeCode) {
        this.officeCode = officeCode;
    }

    public Integer getPosCode() {
        return posCode;
    }

    public void setPosCode(Integer posCode) {
        this.posCode = posCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Integer getSectorDocumentCode() {
        return sectorDocumentCode;
    }

    public void setSectorDocumentCode(Integer sectorDocumentCode) {
        this.sectorDocumentCode = sectorDocumentCode;
    }
}
