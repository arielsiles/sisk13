package com.encens.khipus.model.common;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.FileUtil;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * @author
 * @version 1.2.3
 */
@NamedQueries(
        {
                @NamedQuery(name = "File.findAll", query = "select o from File o order by o.id")
        })

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "File.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "archivo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)

@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "archivo")
public class File implements BaseModel {

    @Id
    @Column(name = "idarchivo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "File.tableGenerator")
    private Long id;


    @Column(name = "valor", nullable = false)
    @Basic(fetch = FetchType.LAZY)
    @Lob
    private byte[] value;

    @Column(name = "nombre", nullable = false)
    @Length(max = 255)
    private String name;

    @Column(name = "tamanio", nullable = false)
    private long size;

    @Column(name = "tipocontenido", length = 200)
    @Length(max = 200)
    private String contentType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value != null ? value.clone() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Boolean isEmpty() {
        return FileUtil.isEmpty(this);
    }

    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", value=" + value +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", company=" + company +
                '}';
    }
}