package com.encens.khipus.model.xproduction;


import com.encens.khipus.model.xproduction.CollectionForm;
import com.encens.khipus.model.xproduction.ProductiveZone;
import com.encens.khipus.util.Constants;
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
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "registroacopio",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "registroacopio")
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class CollectionRecord implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "idregistroacopio", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CollectionRecord_Generator")
    private Long id;

    @Column(name = "cantidadrecibida", columnDefinition = "DECIMAL(16,2)",nullable = false)
    private Double receivedAmount;

    @Column(name = "cantidadpesada", columnDefinition = "DECIMAL(16,2)",nullable = false)
    private Double weightedAmount;

    @Column(name = "cantidadrechazada",columnDefinition = "DECIMAL(16,2)", nullable = false)
    private Double rejectedAmount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idzonaproductiva", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.xproduction.ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idplanillaacopio", nullable = false, updatable = false, insertable = true)
    private CollectionForm collectionForm;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
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

    public com.encens.khipus.model.xproduction.ProductiveZone getProductiveZone() {
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
