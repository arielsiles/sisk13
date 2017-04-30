package com.encens.khipus.model.production;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NamedQueries({
        @NamedQuery(name = "CollectionForm.calculateWeightedAmountOnDateByMetaProduct",
                    query = "select sum(collectionRecord.weightedAmount) " +
                            "from CollectionRecord collectionRecord " +
                            "where collectionRecord.collectionForm.date = :date " +
                            "and collectionRecord.collectionForm.metaProduct = :metaProduct"),

        @NamedQuery(name = "CollectionForm.calculateWeightedAmountBetweebDateByMetaProduct",
                query = " select sum(collectionRecord.weightedAmount) " +
                        " from CollectionRecord collectionRecord " +
                        " where collectionRecord.collectionForm.date between :startDate and :endDate" +
                        " and collectionRecord.collectionForm.metaProduct = :metaProduct"),
        @NamedQuery(name = "CollectionForm.calculateWeightedAmountBetweebDateByMetaProductAndGAB",
                query = " select sum(collectionRecord.weightedAmount) " +
                        " from CollectionRecord collectionRecord " +
                        " where collectionRecord.collectionForm.date between :startDate and :endDate" +
                        " and collectionRecord.productiveZone = :productiveZone" +
                        " and collectionRecord.collectionForm.metaProduct = :metaProduct"),
        @NamedQuery(name = "CollectionForm.calculateUsedAmountOnDateByMetaProduct",
                query = "select sum(inputProductionVoucher.amount) " +
                        "from ProductionOrder productionOrder " +
                        "left join productionOrder.inputProductionVoucherList inputProductionVoucher " +
                        "where productionOrder.productionPlanning.date = :date " +
                        "and productionOrder.productionPlanning.state <> com.encens.khipus.model.production.ProductionPlanningState.PENDING " +
                        "and inputProductionVoucher.metaProduct = :metaProduct "),
        @NamedQuery(name = "CollectionForm.calculateWeightedAmountToDateByMetaProduct",
                    query = "select sum(collectionRecord.weightedAmount) " +
                            "from CollectionRecord collectionRecord " +
                            "where collectionRecord.collectionForm.date <= :date " +
                            "and collectionRecord.collectionForm.metaProduct = :metaProduct"),
        @NamedQuery(name = "CollectionForm.calculateUsedAmountToDateByMetaProduct",
                    query = "select sum(inputProductionVoucher.amount) " +
                            "from ProductionOrder productionOrder " +
                            "left join productionOrder.inputProductionVoucherList inputProductionVoucher " +
                            "where productionOrder.productionPlanning.date <= :date " +
                            "and productionOrder.productionPlanning.state <> com.encens.khipus.model.production.ProductionPlanningState.PENDING " +
                            "and inputProductionVoucher.metaProduct = :metaProduct"),
        @NamedQuery(name = "CollectionForm.calculateCollectedAmountOnDateByMetaProduct",
                    query = "select collectedRawMaterial.rawMaterialCollectionSession.productiveZone.id, sum(collectedRawMaterial.amount) " +
                            "from CollectedRawMaterial collectedRawMaterial " +
                            "where collectedRawMaterial.rawMaterialCollectionSession.date = :date " +
                            "and collectedRawMaterial.rawMaterialCollectionSession.metaProduct = :metaProduct " +
                            "group by collectedRawMaterial.rawMaterialCollectionSession.productiveZone")
})


@TableGenerator(name = "CollectionForm_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "PLANILLAACOPIO",
        allocationSize = 10)

@Entity
@Table(name = "PLANILLAACOPIO", uniqueConstraints = @UniqueConstraint(columnNames = {"IDCOMPANIA", "FECHA"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class CollectionForm implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "IDPLANILLAACOPIO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CollectionForm_Generator")
    private Long id;

    @Column(name = "FECHA",columnDefinition = "DATE", nullable = false)
    private Date date;

    @Column(name = "ESTADO", nullable = true, length = 3)
    @Enumerated(EnumType.STRING)
    private CollectionFormState state = CollectionFormState.PEN;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @OneToMany(mappedBy = "collectionForm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<CollectionRecord> collectionRecordList = new ArrayList<CollectionRecord>();

    @Column(name = "PORCENTAJEGRASA",nullable = true,columnDefinition = "DECIMAL(16,2)")
    private Double greasePercentage = 0.0;

    @Column(name = "TOTALPESADO",nullable = true,columnDefinition = "DECIMAL(16,2)")
    private Double totalWeighed = 0.0;

    @OneToOne
    @JoinColumn(name = "IDMETAPRODUCTOPRODUCCION", nullable = false, updatable = false, insertable = true)
    private MetaProduct metaProduct;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public com.encens.khipus.model.admin.Company getCompany() {
        return company;
    }

    public void setCompany(com.encens.khipus.model.admin.Company company) {
        this.company = company;
    }

    public List<CollectionRecord> getCollectionRecordList() {
        return collectionRecordList;
    }

    public void setCollectionRecordList(List<CollectionRecord> collectionRecordList) {
        this.collectionRecordList = collectionRecordList;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public CollectionFormState getState() {
        return state;
    }

    public void setState(CollectionFormState state) {
        this.state = state;
    }

    public Double getGreasePercentage() {
        return greasePercentage;
    }

    public void setGreasePercentage(Double greasePercentage) {
        this.greasePercentage = greasePercentage;
    }

    public Double getTotalWeighed() {
        return totalWeighed;
    }

    public void setTotalWeighed(Double totalWeighed) {
        this.totalWeighed = totalWeighed;
    }
}
