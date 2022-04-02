package com.encens.khipus.model.employees;

import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.*;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */

@NamedQueries({
        @NamedQuery(name = "DischargeDocument.findByJobContractAndPayroll",
                query = "select document from DischargeDocument document where document.jobContract =:jobContract and document.gestionPayroll =:gestionPayroll"),

        @NamedQuery(name = "DischargeDocument.findByEmployee",
                query = "select document from DischargeDocument document where document.jobContract.contract.employee =:employee and document.gestionPayroll =:gestionPayroll ")
})

@Entity
@PrimaryKeyJoinColumns(value = {
        @PrimaryKeyJoinColumn(name = "iddocumentodescargo", referencedColumnName = "iddocumentocontable")
})

@EntityListeners(UpperCaseStringListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "documentodescargo")
public class DischargeDocument extends AccountingDocument {
    @Column(name = "tipo", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private CollectionDocumentType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "estado", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private DischargeDocumentState state;

    @Column(name = "moneda", updatable = false)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "tipocambio", precision = 16, scale = 6, updatable = true)
    private BigDecimal exchangeRate;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "identidad", referencedColumnName = "cod_enti", nullable = true, insertable = true, updatable = true)
    private FinancesEntity financesEntity;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idcontratopuesto", nullable = false, insertable = true, updatable = false)
    private JobContract jobContract;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idgestionplanilla", nullable = false, insertable = true, updatable = false)
    private GestionPayroll gestionPayroll;

    public CollectionDocumentType getType() {
        return type;
    }

    public void setType(CollectionDocumentType type) {
        this.type = type;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public DischargeDocumentState getState() {
        return state;
    }

    public void setState(DischargeDocumentState state) {
        this.state = state;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public FinancesEntity getFinancesEntity() {
        return financesEntity;
    }

    public void setFinancesEntity(FinancesEntity financesEntity) {
        this.financesEntity = financesEntity;
    }

    public JobContract getJobContract() {
        return jobContract;
    }

    public void setJobContract(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    public GestionPayroll getGestionPayroll() {
        return gestionPayroll;
    }

    public void setGestionPayroll(GestionPayroll gestionPayroll) {
        this.gestionPayroll = gestionPayroll;
    }

    public boolean isApproved() {
        return null != state && DischargeDocumentState.APPROVED.equals(state);
    }

    public boolean isPending() {
        return null != state && DischargeDocumentState.PENDING.equals(state);
    }

    public boolean isNullified() {
        return null != state && DischargeDocumentState.NULLIFIED.equals(state);
    }
}
