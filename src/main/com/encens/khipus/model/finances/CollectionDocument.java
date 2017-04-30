package com.encens.khipus.model.finances;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;

import javax.persistence.*;

/**
 * Entity for CollectionDocument
 *
 * @author
 * @version 2.26
 */

@NamedQueries(
        {
                @NamedQuery(name = "CollectionDocument.findAll", query = "select o from CollectionDocument o "),
                @NamedQuery(name = "CollectionDocument.findByRotatoryFundCollection", query = "select o from CollectionDocument o " +
                        " where o.rotatoryFundCollection =:rotatoryFundCollection ")
        }
)

@Entity
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "iddocumentocobro", referencedColumnName = "iddocumentocontable")
})

@EntityListeners(UpperCaseStringListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "DOCUMENTOCOBRO")
public class CollectionDocument extends AccountingDocument {

    @Column(name = "TIPODOCUMENTOCOBRO", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private CollectionDocumentType collectionDocumentType;

    @OneToOne(mappedBy = "collectionDocument", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private RotatoryFundCollection rotatoryFundCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDENTIDAD", referencedColumnName = "COD_ENTI")
    private FinancesEntity financesEntity;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public CollectionDocumentType getCollectionDocumentType() {
        return collectionDocumentType;
    }

    public void setCollectionDocumentType(CollectionDocumentType collectionDocumentType) {
        this.collectionDocumentType = collectionDocumentType;
    }

    public RotatoryFundCollection getRotatoryFundCollection() {
        return rotatoryFundCollection;
    }

    public void setRotatoryFundCollection(RotatoryFundCollection rotatoryFundCollection) {
        this.rotatoryFundCollection = rotatoryFundCollection;
    }

    public FinancesEntity getFinancesEntity() {
        return financesEntity;
    }

    public void setFinancesEntity(FinancesEntity financesEntity) {
        this.financesEntity = financesEntity;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}