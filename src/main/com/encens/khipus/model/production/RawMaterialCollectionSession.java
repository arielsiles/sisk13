package com.encens.khipus.model.production;


import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@NamedQueries({
        @NamedQuery(name = "RawMaterialCollectionSession.findMinimumDateOfCollectionSessionByMetaProductBetweenDates",
                query = "select Min(rawMaterialCollectionSession.date) " +
                        "from RawMaterialCollectionSession rawMaterialCollectionSession " +
                        "where rawMaterialCollectionSession.productiveZone = :productiveZone " +
                        "and rawMaterialCollectionSession.metaProduct = :metaProduct " +
                        "and rawMaterialCollectionSession.date between :startDate and :endDate ")
})

@TableGenerator(name = "RawMaterialCollectionSession_Generator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "sesionacopio",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "sesionacopio")
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class RawMaterialCollectionSession implements com.encens.khipus.model.BaseModel {

    @Id
    @Column(name = "idsesionacopio", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RawMaterialCollectionSession_Generator")
    private Long id;

    @Column(name = "fecha",columnDefinition = "DATE" , nullable = false)
    private Date date;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductionCollectionState state = ProductionCollectionState.PENDING;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private com.encens.khipus.model.admin.Company company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idzonaproductiva", nullable = false, updatable = false, insertable = true)
    private ProductiveZone productiveZone;

    @OneToMany(mappedBy = "rawMaterialCollectionSession", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<CollectedRawMaterial> collectedRawMaterialList = new ArrayList<CollectedRawMaterial>();

    @OneToOne
    @JoinColumn(name = "idmetaproductoproduccion", nullable = false, updatable = false, insertable = true)
    private MetaProduct metaProduct;

    @Version
    @Column(name = "version", nullable = false)
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public List<CollectedRawMaterial> getCollectedRawMaterialList() {
        return collectedRawMaterialList;
    }

    public void setCollectedRawMaterialList(List<CollectedRawMaterial> collectedRawMaterialList) {
        this.collectedRawMaterialList = collectedRawMaterialList;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getFullNameOfProductiveZone() {
        return (productiveZone == null ? "" : productiveZone.getFullName());
    }

    public  void setFullNameOfProductiveZone(String fullName) {

    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public ProductionCollectionState getState() {
        return state;
    }

    public void setState(ProductionCollectionState state) {
        this.state = state;
    }
}
