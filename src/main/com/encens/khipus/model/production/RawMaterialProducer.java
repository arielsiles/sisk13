package com.encens.khipus.model.production;

import com.encens.khipus.model.contacts.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NamedQueries ({
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
@Table(name = "PRODUCTORMATERIAPRIMA")
@DiscriminatorValue("PRODUCTORMATERIAPRIMA")
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "IDPRODUCTORMATERIAPRIMA", referencedColumnName = "IDPERSONA")})
public class RawMaterialProducer extends Person {

    @Column(name = "LICENCIAIMPUESTOS2011", length = 200, nullable = true)
    private String codeTaxLicence2011;

    @Column(name = "FECHAINIIMPUESTO2011", columnDefinition = "DATE", nullable = true)
    private Date startDateTaxLicence2011;

    @Column(name = "FECHAFINIMPUESTO2011", columnDefinition = "DATE",nullable = true)
    private Date expirationDateTaxLicence2011;

    @Column(name = "LICENCIAIMPUESTOS", length = 200, nullable = true)
    private String codeTaxLicence;

    @Column(name = "FECHAEXPIRALICENCIAIMPUESTO", columnDefinition = "DATE", nullable = true)
    private Date expirationDateTaxLicence;

    @Column(name = "FECHAINICIALICENCIAIMPUESTO", columnDefinition = "DATE",nullable = true)
    private Date startDateTaxLicence;

    @Column(name = "ESRESPONSABLE", nullable = false)
    @Type(type = "IntegerBoolean")
    private Boolean responsible;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDZONAPRODUCTIVA", nullable = true, updatable = true, insertable = true)
    private ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @OneToMany(mappedBy = "rawMaterialProducer", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<CollectedRawMaterial> collectedRawMaterialList = new ArrayList<CollectedRawMaterial>(0);

    @OneToMany(mappedBy = "rawMaterialProducerTax", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<ProducerTax> producerTaxes = new ArrayList<ProducerTax>(0);

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

    public ProductiveZone getProductiveZone() {
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

    public List<CollectedRawMaterial> getCollectedRawMaterialList() {
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

    public List<ProducerTax> getProducerTaxes() {
        return producerTaxes;
    }

    public void setProducerTaxes(List<ProducerTax> producerTaxes) {
        this.producerTaxes = producerTaxes;
    }
}


