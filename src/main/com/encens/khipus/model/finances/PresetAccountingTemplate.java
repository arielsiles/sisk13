package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.customers.PriceItem;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@TableGenerator(schema = Constants.KHIPUS_SCHEMA,name = "PresetAccountingTemplate.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "plantillacontablepredefinida",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "plantillacontablepredefinida",uniqueConstraints = @UniqueConstraint(columnNames = {"idplantillacontablepredefinida"}))
public class PresetAccountingTemplate implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PresetAccountingTemplate.tableGenerator")
    @Column(name = "idplantillacontablepredefinida", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @OneToMany(mappedBy = "presetAccountingTemplate", fetch = FetchType.LAZY)
    private List<TypePresetAccountingTemplate> typePresetAccountingTemplateList = new ArrayList<TypePresetAccountingTemplate>(0);

    public List<TypePresetAccountingTemplate> getTypePresetAccountingTemplateList() {
        return typePresetAccountingTemplateList;
    }

    public void setTypePresetAccountingTemplateList(List<TypePresetAccountingTemplate> typePresetAccountingTemplateList) {
        this.typePresetAccountingTemplateList = typePresetAccountingTemplateList;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Override
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
}
