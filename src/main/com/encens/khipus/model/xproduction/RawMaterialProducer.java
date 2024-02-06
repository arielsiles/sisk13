package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.production.CollectedRawMaterial;
import com.encens.khipus.model.production.DiscountReserve;
import com.encens.khipus.model.production.ProducerTax;
import com.encens.khipus.model.production.ProductiveZone;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NamedQueries ({
        @NamedQuery(name = "RawMaterialProducer.findByIdNumber", query = "select e from RawMaterialProducer e where e.idNumber =:idNumber"),
        @NamedQuery(name = "RawMaterialProducer.findAllByProductiveZone",
                query = "select rawMaterialProducer " +
                        "from RawMaterialProducer rawMaterialProducer " +
                        "where rawMaterialProducer.productiveZone = :productiveZone" +
                        " order by rawMaterialProducer.lastName"),
        @NamedQuery(name = "RawMaterialProducer.findReponsibleExceptThisByProductiveZone",
                query = "select rawMaterialProducer " +
                        "from RawMaterialProducer rawMaterialProducer " +
                        "where rawMaterialProducer.responsible = 1 and rawMaterialProducer <> :rawMaterialProducer " +
                        "and rawMaterialProducer.productiveZone = :productiveZone")
})

@Entity
@Table(name = "productormateriaprima")
@DiscriminatorValue("productormateriaprima")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "idproductormateriaprima", referencedColumnName = "idpersona")})
public class RawMaterialProducer extends Person {

    @Column(name = "licenciaimpuestos2011", length = 200, nullable = true)
    private String codeTaxLicence2011;

    @Column(name = "fechainiimpuesto2011", columnDefinition = "DATE", nullable = true)
    private Date startDateTaxLicence2011;

    @Column(name = "fechafinimpuesto2011", columnDefinition = "DATE",nullable = true)
    private Date expirationDateTaxLicence2011;

    @Column(name = "licenciaimpuestos", length = 200, nullable = true)
    private String codeTaxLicence;

    @Column(name = "fechaexpiralicenciaimpuesto", columnDefinition = "DATE", nullable = true)
    private Date expirationDateTaxLicence;

    @Column(name = "fechainicialicenciaimpuesto", columnDefinition = "DATE",nullable = true)
    private Date startDateTaxLicence;

    @Column(name = "esresponsable", nullable = true)
    @Type(type = "IntegerBoolean")
    private Boolean responsible;

    @Column(name = "activo", nullable = true)
    @Type(type = "IntegerBoolean")
    private Boolean active;

    @Column(name = "numerocuenta", length = 50, nullable = true)
    @Length(max = 50)
    private String accountNumber;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idzonaproductiva", nullable = true, updatable = true, insertable = true)
    private com.encens.khipus.model.production.ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @OneToMany(mappedBy = "rawMaterialProducer", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<com.encens.khipus.model.production.CollectedRawMaterial> collectedRawMaterialList = new ArrayList<com.encens.khipus.model.production.CollectedRawMaterial>(0);

    @OneToMany(mappedBy = "rawMaterialProducerTax", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<com.encens.khipus.model.production.ProducerTax> producerTaxes = new ArrayList<com.encens.khipus.model.production.ProducerTax>(0);

    @OneToMany(mappedBy = "materialProducer", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<DiscountReserve> discountReserves = new ArrayList<DiscountReserve>(0);

    public String getCodeTaxLicence() {
        return codeTaxLicence;
    }

    public void setCodeTaxLicence(String codeTaxLicence) {
        this.codeTaxLicence = codeTaxLicence;
    }

    public Date getExpirationDateTaxLicence() {
        return expirationDateTaxLicence;
    }

    public void setExpirationDateTaxLicence(Date expirationDateTaxLicence) {
        this.expirationDateTaxLicence = expirationDateTaxLicence;
    }

    public Boolean getResponsible() {
        return responsible;
    }

    public void setResponsible(Boolean responsible) {
        this.responsible = responsible;
    }

    public com.encens.khipus.model.production.ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public com.encens.khipus.model.admin.Company getCompany() {
        return company;
    }


    public void setCompany(com.encens.khipus.model.admin.Company company) {
        this.company = company;
    }

    public List<com.encens.khipus.model.production.CollectedRawMaterial> getCollectedRawMaterialList() {
        return collectedRawMaterialList;
    }

    public void setCollectedRawMaterialList(List<CollectedRawMaterial> collectedRawMaterialList) {
        this.collectedRawMaterialList = collectedRawMaterialList;
    }

    public String getFullNameOfProductiveZone() {
        return (productiveZone == null ? "" : productiveZone.getFullName());
    }

    public void setFullNameOfProductiveZone(String fullName) {

    }

    public Date getStartDateTaxLicence() {
        return startDateTaxLicence;
    }

    public void setStartDateTaxLicence(Date startDateTaxLicence) {
        this.startDateTaxLicence = startDateTaxLicence;
    }

    public String getCodeTaxLicence2011() {
        return codeTaxLicence2011;
    }

    public void setCodeTaxLicence2011(String codeTaxLicence2011) {
        this.codeTaxLicence2011 = codeTaxLicence2011;
    }

    public Date getExpirationDateTaxLicence2011() {
        return expirationDateTaxLicence2011;
    }

    public void setExpirationDateTaxLicence2011(Date expirationDateTaxLicence2011) {
        this.expirationDateTaxLicence2011 = expirationDateTaxLicence2011;
    }

    public Date getStartDateTaxLicence2011() {
        return startDateTaxLicence2011;
    }

    public void setStartDateTaxLicence2011(Date startDateTaxLicence2011) {
        this.startDateTaxLicence2011 = startDateTaxLicence2011;
    }

    public List<DiscountReserve> getDiscountReserves() {
        return discountReserves;
    }

    public void setDiscountReserves(List<DiscountReserve> discountReserves) {
        this.discountReserves = discountReserves;
    }

    public List<com.encens.khipus.model.production.ProducerTax> getProducerTaxes() {
        return producerTaxes;
    }

    public void setProducerTaxes(List<ProducerTax> producerTaxes) {
        this.producerTaxes = producerTaxes;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}


