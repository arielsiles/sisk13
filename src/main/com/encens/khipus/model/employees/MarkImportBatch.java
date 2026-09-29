package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Lote de importacion de marcaciones desde el archivo del biometrico.
 * <p/>
 * Existe para que la carga sea reversible: cada marca importada queda apuntando a su lote, y
 * anular el lote borra exactamente lo que ese lote trajo, sin tocar el marcado que el
 * dispositivo escribio directo.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "MarkImportBatch.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "loteimportmarcado",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "MarkImportBatch.findAll",
                query = "select o from MarkImportBatch o order by o.uploadDate desc"),
        @NamedQuery(name = "MarkImportBatch.countMarksByBatch",
                query = "select count(m) from RH_Mark m where m.importBatchId = :importBatchId")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "loteimportmarcado")
public class MarkImportBatch implements BaseModel {

    @Id
    @Column(name = "idloteimportmarcado", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MarkImportBatch.tableGenerator")
    private Long id;

    @Column(name = "nombrearchivo", nullable = false, length = 250)
    @NotNull
    @Length(max = 250)
    private String fileName;

    @Column(name = "fechacarga", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date uploadDate = new Date();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario")
    private User user;

    /* Rango de fechas que abarcan las marcas del archivo, para ubicar el lote de un vistazo. */
    @Column(name = "fechadesde")
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Column(name = "fechahasta")
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Column(name = "filasleidas", nullable = false)
    private Integer readRows = 0;

    @Column(name = "marcasnuevas", nullable = false)
    private Integer newMarks = 0;

    @Column(name = "marcasduplicadas", nullable = false)
    private Integer duplicatedMarks = 0;

    @Column(name = "marcassinempleado", nullable = false)
    private Integer marksWithoutEmployee = 0;

    /* Codigos de marcacion del archivo que no cruzaron con ningun empleado, para reportarlos. */
    @Column(name = "codigossinempleado", length = 2000)
    @Length(max = 2000)
    private String codesWithoutEmployee;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MarkImportBatchState state = MarkImportBatchState.CONFIRMED;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Integer getReadRows() {
        return readRows;
    }

    public void setReadRows(Integer readRows) {
        this.readRows = readRows;
    }

    public Integer getNewMarks() {
        return newMarks;
    }

    public void setNewMarks(Integer newMarks) {
        this.newMarks = newMarks;
    }

    public Integer getDuplicatedMarks() {
        return duplicatedMarks;
    }

    public void setDuplicatedMarks(Integer duplicatedMarks) {
        this.duplicatedMarks = duplicatedMarks;
    }

    public Integer getMarksWithoutEmployee() {
        return marksWithoutEmployee;
    }

    public void setMarksWithoutEmployee(Integer marksWithoutEmployee) {
        this.marksWithoutEmployee = marksWithoutEmployee;
    }

    public String getCodesWithoutEmployee() {
        return codesWithoutEmployee;
    }

    public void setCodesWithoutEmployee(String codesWithoutEmployee) {
        this.codesWithoutEmployee = codesWithoutEmployee;
    }

    public MarkImportBatchState getState() {
        return state;
    }

    public void setState(MarkImportBatchState state) {
        this.state = state;
    }

    @Transient
    public boolean isAnnulled() {
        return MarkImportBatchState.ANNULLED.equals(state);
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

    @Override
    public String toString() {
        return "MarkImportBatch{id=" + id + ", fileName='" + fileName + "', state=" + state + "}";
    }
}
