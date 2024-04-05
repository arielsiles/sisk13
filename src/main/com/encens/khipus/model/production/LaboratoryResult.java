package com.encens.khipus.model.production;

/* Mapear la tabla resultadolab, con la clase LaboratoryResult y sus atributos
* */

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.File;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

@TableGenerator(
        name = "LaboratoryResult.tableGenerator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "resultadolab",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE
)

@Entity
@Table(name = "resultadolab")
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class})
public class LaboratoryResult implements BaseModel {

    @Id
    @Column(name = "idresultadolab", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "LaboratoryResult.tableGenerator")
    private Long id;

    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "codigo", length = 100)
    private String code;

    @Column(name = "caracteristica", length = 255)
    private String description;

    @Column(name = "fechamuestra")
    private Date sampleDate;

    @Column(name = "fecharecepcion")
    private Date receptionDate;

    @Column(name = "fechainicio")
    private Date startDate;

    @Column(name = "fechafin")
    private Date fechafin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idinspeccionzona", nullable = false)
    private ZoneInspection zoneInspection;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idarchivo", referencedColumnName = "idarchivo")
    private File file;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version")
    private long version;

    // Getters and setters

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getSampleDate() {
        return sampleDate;
    }

    public void setSampleDate(Date sampleDate) {
        this.sampleDate = sampleDate;
    }

    public Date getReceptionDate() {
        return receptionDate;
    }

    public void setReceptionDate(Date receptionDate) {
        this.receptionDate = receptionDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFechafin() {
        return fechafin;
    }

    public void setFechafin(Date fechafin) {
        this.fechafin = fechafin;
    }

    public ZoneInspection getZoneInspection() {
        return zoneInspection;
    }

    public void setZoneInspection(ZoneInspection zoneInspection) {
        this.zoneInspection = zoneInspection;
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
