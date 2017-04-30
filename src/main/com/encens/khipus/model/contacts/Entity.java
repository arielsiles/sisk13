package com.encens.khipus.model.contacts;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.customers.DocumentType;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity Super Class
 *
 * @author
 * @version $Id: Entity.java 2008-8-28 11:08:43 $
 */


@NamedQueries(
        {
                @NamedQuery(name = "Entity.findByIdNumber", query = "select e from Entity e where e.idNumber =:number"),
                @NamedQuery(name = "Entity.findByIdNumberAndLimitId", query = "select e from Entity e where e.idNumber =:number and e.id >=:limitIdNumber"),
                @NamedQuery(name = "Entity.findByCustomerNumber", query = "select c.entity from Customer c where c.number =:number")}
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Entity.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "entidad",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "entidad")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
//@IdClass(EntityPK.class) see EntityPK.java class comments.
@javax.persistence.Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
public abstract class Entity implements Serializable, BaseModel {

    @Id
    @Column(name = "identidad", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Entity.tableGenerator")
    private Long id;

    @Column(name = "noidentificacion", nullable = false, length = 100)
    @Length(max = 100)
    private String idNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDEXTTIPODOCUMENTO", referencedColumnName = "IDEXTTIPODOCUMENTO")
    private Extension extensionSite;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipodocumento", nullable = false)
    private DocumentType documentType;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "iddireccion", nullable = true)
    private Address address;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Extension getExtensionSite() {
        return extensionSite;
    }

    public void setExtensionSite(Extension extensionSite) {
        this.extensionSite = extensionSite;
    }

    /**
     * Returns the name of the Entity, if person, the fullName, if organization, the name
     *
     * @return an string with the formatted name.
     */
    public abstract String getName();

    //Todo: remove this, after fixing InvoiceCustomerAction.updateEntityType

    public void setName(String name) {
    }
}
