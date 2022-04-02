package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * FinancesEntity
 *
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "FinancesEntity.countByAcronym", query = "select count(fe) from FinancesEntity fe where lower(fe.acronym)=lower(:acronym)"),
        @NamedQuery(name = "FinancesEntity.countByAcronymAndEntity", query = "select count(fe) from FinancesEntity fe where lower(fe.acronym)=lower(:acronym) and fe.id<>:financesEntityId")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FinancesEntity.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sf_entidades",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

/*@SequenceGenerator(name = "FinancesEntity.sequenceGenerator", sequenceName = Constants.FINANCES_SCHEMA + ".sf_entidad")*/
@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "sf_entidades", schema = Constants.FINANCES_SCHEMA)
public class FinancesEntity implements BaseModel {

    @Id
    @Column(name = "cod_enti", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FinancesEntity.tableGenerator")
    private Long id;

    /*@Id
    @javax.persistence.Column(name = "idgestion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Gestion.tableGenerator")
    private Long id;*/

    //todo This attribute must be use for readonly
    /*@Column(name = "COD_ENTI", insertable = false, updatable = false)
    private String code;*/

    @Column(name = "nit", length = 20)
    @Length(max = 20)
    private String nitNumber;

    @Column(name = "ci", length = 20)
    @Length(max = 20)
    private String idNumber;

    @Column(name = "razon_social", length = 100)
    @Length(max = 100)
    private String acronym;

    @Column(name = "direccion", length = 100)
    @Length(max = 100)
    private String mainAddress;

    @Column(name = "direccion1", length = 100)
    @Length(max = 100)
    private String secondaryAddress;

    @Column(name = "lugar", length = 100)
    @Length(max = 100)
    private String place;

    @Column(name = "telefono", length = 30)
    @Length(max = 30)
    private String phoneNumber;

    @Column(name = "fax", length = 30)
    @Length(max = 30)
    private String faxNumber;

    @Column(name = "casilla", length = 10)
    @Length(max = 10)
    private String postOfficeBox;

    @Column(name = "responsable", length = 100)
    @Length(max = 100)
    private String responsible;

    @Column(name = "obs", length = 240)
    @Length(max = 240)
    private String observations;

    @Column(name = "estado", length = 3)
    @Enumerated(EnumType.STRING)
    private FinancesEntityState state;

    @Column(name = "motivo_bloqueo", length = 100)
    @Length(max = 100)
    private String blockReason;

    @Version
    @Column(name = "version")
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /*public String getCode() {
        return code;
    }*/

    /*public void setCode(String code) {
        this.code = code;
    }*/

    public String getNitNumber() {
        return nitNumber;
    }

    public void setNitNumber(String nitNumber) {
        this.nitNumber = nitNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getFullName(){
        return getNitNumber() + " " + getAcronym();
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getMainAddress() {
        return mainAddress;
    }

    public void setMainAddress(String mainAddress) {
        this.mainAddress = mainAddress;
    }

    public String getSecondaryAddress() {
        return secondaryAddress;
    }

    public void setSecondaryAddress(String secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getPostOfficeBox() {
        return postOfficeBox;
    }

    public void setPostOfficeBox(String postOfficeBox) {
        this.postOfficeBox = postOfficeBox;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public FinancesEntityState getState() {
        return state;
    }

    public void setState(FinancesEntityState state) {
        this.state = state;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
