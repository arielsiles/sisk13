package com.encens.khipus.model.dashboard;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.JobContract;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a widget of a dashboard
 *
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Widget.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "componentepanel",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "Widget.findByXmlId", query = "select w from Widget w where w.xmlId = :xmlId")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "COMPONENTEPANEL",
        uniqueConstraints = @UniqueConstraint(columnNames = {"XMLID", "IDCOMPANIA"}))
public class Widget implements BaseModel {

    @Id
    @Column(name = "IDCOMPONENTEPANEL", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Widget.tableGenerator")
    private Long id;

    @Length(max = 50)
    @Column(name = "XMLID", length = 50, nullable = false)
    @NotNull
    private String xmlId;

    @Length(max = 255)
    @Column(name = "TITULO", length = 255, nullable = false)
    private String title;

    @Length(max = 255)
    @Column(name = "NOMBRECOMPONENTE", length = 255, nullable = false, insertable = true, updatable = false)
    private String componentName;

    @Length(max = 255)
    @Column(name = "AREA", length = 255)
    private String area;

    @Length(max = 255)
    @Column(name = "MODULO", length = 255)
    private String module;

    @Length(max = 255)
    @Column(name = "FUNCION", length = 255)
    private String function;

    @Enumerated(EnumType.STRING)
    @Column(name = "VERIFICACION", length = 30, nullable = false)
    @NotNull
    private Verification verification;

    @Enumerated(EnumType.STRING)
    @Column(name = "UNIDAD", length = 30, nullable = false)
    @NotNull
    private Unit unit;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "IDCOMPONENTEPANEL", referencedColumnName = "IDCOMPONENTEPANEL", nullable = false)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("index asc")
    private List<com.encens.khipus.model.dashboard.Filter> filters = new ArrayList<com.encens.khipus.model.dashboard.Filter>();

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IDRESPONSABLENACIONAL")
    private JobContract nationalResponsible;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "IDCOMPONENTEPANEL", referencedColumnName = "IDCOMPONENTEPANEL", nullable = false)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<Responsible> regionalResponsibles = new ArrayList<Responsible>();

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getXmlId() {
        return xmlId;
    }

    public void setXmlId(String xmlId) {
        this.xmlId = xmlId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Verification getVerification() {
        return verification;
    }

    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public List<com.encens.khipus.model.dashboard.Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<com.encens.khipus.model.dashboard.Filter> filters) {
        this.filters = filters;
    }

    public JobContract getNationalResponsible() {
        return nationalResponsible;
    }

    public void setNationalResponsible(JobContract nationalResponsible) {
        this.nationalResponsible = nationalResponsible;
    }

    public List<Responsible> getRegionalResponsibles() {
        return regionalResponsibles;
    }

    public void setRegionalResponsibles(List<Responsible> regionalResponsibles) {
        this.regionalResponsibles = regionalResponsibles;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
