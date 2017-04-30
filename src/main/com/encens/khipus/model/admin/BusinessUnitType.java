package com.encens.khipus.model.admin;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: macmac
 * Date: 17-dic-2008
 * Time: 16:42:25
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "BusinessUnitType.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipounidadnegocio",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "BusinessUnitType.findAll", query = "select o from BusinessUnitType o "),
                @NamedQuery(name = "BusinessUnitType.countMainBusinessUnitType", query = "select count(o) from BusinessUnitType o where o.main=:mainValue"),
                @NamedQuery(name = "BusinessUnitType.isMainBusinessUnitType", query = "select o from BusinessUnitType o where o.id=:id")
        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "tipounidadnegocio", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class BusinessUnitType implements BaseModel {

    @Id
    @Column(name = "idtipounidadnegocio", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BusinessUnitType.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "principal", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean main;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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


    public Boolean getMain() {
        return main;
    }

    public void setMain(Boolean main) {
        this.main = main;
    }

    @Override
    public String toString() {
        return "BusinessUnitType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", main=" + main +
                ", company=" + company +
                ", version=" + version +
                '}';
    }
}
