package com.encens.khipus.model.production;


import org.hibernate.annotations.Filter;

import javax.persistence.*;


@NamedQueries({
        @NamedQuery(name = "CollectionRecord.findByDateAndProductiveZoneAndMetaProduct",
                    query = "select collectionRecord " +
                            "from CollectionRecord collectionRecord " +
                            "where collectionRecord.collectionForm.date = :date " +
                            "and collectionRecord.productiveZone = :productiveZone " +
                            "and collectionRecord.collectionForm.metaProduct = :metaProduct "),
        @NamedQuery(name = "CollectionRecord.calculateDeltaAmountByMetaProductAndProductiveZoneBetweenDates",
                    query = "select sum(collectionRecord.weightedAmount) - sum(collectionRecord.receivedAmount) " +
                            "from CollectionRecord collectionRecord " +
                            "where collectionRecord.productiveZone = :productiveZone " +
                            "and collectionRecord.collectionForm.metaProduct = :metaProduct " +
                            "and collectionRecord.collectionForm.date between :startDate and :endDate")
})

@TableGenerator(name = "CollectionRecord_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "REGISTROACOPIO",
        allocationSize = 10)

@Entity
@Table(name = "REGISTROACOPIO")
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class CollectionRecord implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "IDREGISTROACOPIO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CollectionRecord_Generator")
    private Long id;

    @Column(name = "CANTIDADRECIBIDA", columnDefinition = "DECIMAL(16,2)",nullable = false)
    private Double receivedAmount;

    @Column(name = "CANTIDADPESADA", columnDefinition = "DECIMAL(16,2)",nullable = false)
    private Double weightedAmount;

    @Column(name = "CANTIDADRECHAZADA",columnDefinition = "DECIMAL(16,2)", nullable = false)
    private Double rejectedAmount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "IDZONAPRODUCTIVA", nullable = false, updatable = false, insertable = true)
    private ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "IDPLANILLAACOPIO", nullable = false, updatable = false, insertable = true)
    private CollectionForm collectionForm;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(Double receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public Double getWeightedAmount() {
        return weightedAmount;
    }

    public void setWeightedAmount(Double weightedAmount) {
        this.weightedAmount = weightedAmount;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public CollectionForm getCollectionForm() {
        return collectionForm;
    }

    public void setCollectionForm(CollectionForm collectionForm) {
        this.collectionForm = collectionForm;
    }

    public com.encens.khipus.model.admin.Company getCompany() {
        return company;
    }

    public void setCompany(com.encens.khipus.model.admin.Company company) {
        this.company = company;
    }

    public Double getRejectedAmount() {
        return rejectedAmount;
    }

    public void setRejectedAmount(Double rejectedAmount) {
        this.rejectedAmount = rejectedAmount;
    }
}
