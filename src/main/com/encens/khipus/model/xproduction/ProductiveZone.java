package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.CollectionRecord;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 22-05-13
 * Time: 05:13 PM
 * To change this template use File | Settings | File Templates.
 */

@NamedQueries({
        @NamedQuery(name = "ProductiveZone.findAll", query = "select productiveZone from ProductiveZone productiveZone where productiveZone.group <> 'CISC' "), /** MODIDYID **/
        @NamedQuery(name = "ProductiveZone.findAllThatDoNotHavePayRollOnDate",
                query = " SELECT productiveZone " +
                        " FROM ProductiveZone productiveZone " +
                        " WHERE NOT EXISTS ( " +
                        "  SELECT rawMaterialPayRoll.productiveZone " +
                        "  FROM RawMaterialPayRoll rawMaterialPayRoll " +
                        "  WHERE rawMaterialPayRoll.startDate = :startDate " +
                        "  and rawMaterialPayRoll.endDate = :endDate " +
                        "  and rawMaterialPayRoll.productiveZone = productiveZone" +
                        " )  "
        )
})

@TableGenerator(name = "ProductiveZone.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "zonaproductiva",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table( schema = Constants.KHIPUS_SCHEMA,
        name = "zonaproductiva",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "idcompania"}))
@Filter(name = "companyFilter")
@EntityListeners(com.encens.khipus.model.CompanyListener.class)
public class ProductiveZone implements BaseModel {

    @Id
    @Column(name = "idzonaproductiva", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductiveZone.tableGenerator")
    private Long id;

    @Column(name = "numero", nullable = false, length = 20)
    private String number;

    @Column(name = "grupo", nullable = true, length = 20)
    private String group;

    @Column(name = "nombre", nullable = false, length = 200)
    private String name;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @OneToMany(mappedBy = "productiveZone", fetch = FetchType.LAZY)
    private List<CollectionRecord> collectionRecordList = new ArrayList<CollectionRecord>();

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getFullName() {
        //return "GAB-" + getNumber() + " " + getName() + "(" + getGroup() + ")";
        return getNumber() + "-" + getName();
    }

    public void setFullName(String fullName) {
    }

    public List<CollectionRecord> getCollectionRecordList() {
        return collectionRecordList;
    }

    public void setCollectionRecordList(List<CollectionRecord> collectionRecordList) {
        this.collectionRecordList = collectionRecordList;
    }
}
