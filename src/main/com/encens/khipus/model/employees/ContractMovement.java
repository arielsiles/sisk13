package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.Contract;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Lo que le paso a un contrato: un cambio de condicion o una baja.
 * <p/>
 * El contrato guarda solo su estado de hoy. Sin esta tabla, pasar de eventual a plazo fijo a
 * indefinido a laboral seria una sucesion de ediciones silenciosas: al mirar el contrato dentro
 * de un ano nadie podria decir desde cuando es laboral ni quien lo decidio.
 * <p/>
 * Guarda el antes y el despues de cada campo. Es redundante contra el estado actual del
 * contrato, y a proposito: asi la historia se lee sola, sin tener que reconstruirla aplicando
 * los movimientos en orden.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "ContractMovement.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "movimientocontrato",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "ContractMovement.findByContract",
                query = "select o from ContractMovement o"
                        + " where o.contract = :contract"
                        + " order by o.date desc, o.id desc")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "movimientocontrato")
public class ContractMovement implements BaseModel {

    @Id
    @Column(name = "idmovimientocontrato", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ContractMovement.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontrato", nullable = false)
    @NotNull
    private Contract contract;

    @Column(name = "tipo", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private ContractMovementType type;

    /** Desde cuando rige el cambio. Para una baja, el ultimo dia de trabajo. */
    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmodalidadanterior")
    private ContractMode previousContractMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmodalidadnueva")
    private ContractMode newContractMode;

    @Column(name = "duracionanterior", length = 15)
    @Enumerated(EnumType.STRING)
    private ContractDuration previousDuration;

    @Column(name = "duracionnueva", length = 15)
    @Enumerated(EnumType.STRING)
    private ContractDuration newDuration;

    @Column(name = "fechafinanterior")
    @Temporal(TemporalType.DATE)
    private Date previousEndDate;

    @Column(name = "fechafinnueva")
    @Temporal(TemporalType.DATE)
    private Date newEndDate;

    @Column(name = "motivo", nullable = false, length = 250)
    @NotNull
    @Length(max = 250)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario")
    private User user;

    @Column(name = "fecharegistro", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date registrationDate = new Date();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Transient
    public boolean isTermination() {
        return ContractMovementType.TERMINATION.equals(type);
    }

    @Transient
    public boolean isModeChanged() {
        if (null == previousContractMode || null == newContractMode) {
            return false;
        }
        return !previousContractMode.getId().equals(newContractMode.getId());
    }

    @Transient
    public boolean isDurationChanged() {
        return null != previousDuration && !previousDuration.equals(newDuration);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public ContractMovementType getType() {
        return type;
    }

    public void setType(ContractMovementType type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ContractMode getPreviousContractMode() {
        return previousContractMode;
    }

    public void setPreviousContractMode(ContractMode previousContractMode) {
        this.previousContractMode = previousContractMode;
    }

    public ContractMode getNewContractMode() {
        return newContractMode;
    }

    public void setNewContractMode(ContractMode newContractMode) {
        this.newContractMode = newContractMode;
    }

    public ContractDuration getPreviousDuration() {
        return previousDuration;
    }

    public void setPreviousDuration(ContractDuration previousDuration) {
        this.previousDuration = previousDuration;
    }

    public ContractDuration getNewDuration() {
        return newDuration;
    }

    public void setNewDuration(ContractDuration newDuration) {
        this.newDuration = newDuration;
    }

    public Date getPreviousEndDate() {
        return previousEndDate;
    }

    public void setPreviousEndDate(Date previousEndDate) {
        this.previousEndDate = previousEndDate;
    }

    public Date getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(Date newEndDate) {
        this.newEndDate = newEndDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
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
        return "ContractMovement{" + type + " " + date + "}";
    }
}
