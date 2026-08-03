package com.encens.khipus.model.customers;

import com.encens.khipus.model.finances.FinancesCurrencyType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Linea de detalle de la provision mensual de intereses por pagar sobre DPF.
 * <p/>
 * Es el equivalente de una fila del Excel "ESTADO DE DEPOSITOS A PLAZO FIJO POR PAGAR":
 * tipo de cuenta, socio, fechas, tasa, capital, dias devengados y provision del mes.
 * No es una entidad: se calcula en memoria y solo alimenta la grilla de la pantalla y
 * la totalizacion del asiento.
 *
 * @author
 */
public class FixedTermDepositProvision implements Serializable {

    private static final long serialVersionUID = 1L;

    private Account account;

    /** Ultimo dia devengado del mes: min(fin de mes, vencimiento). Columna "Prov. Al". */
    private Date provisionUntilDate;

    /** Primer dia devengado del mes: max(inicio de mes, apertura). */
    private Date provisionFromDate;

    /** Dias devengados, inclusivo en ambos extremos. */
    private int days;

    /** Tasa anual aplicada (tipocuenta.inta). */
    private BigDecimal rate;

    /** Interes del mes, SIN redondear: el redondeo se hace recien sobre el total. */
    private BigDecimal provision;

    /** Cuenta de pasivo (CTACF_MN / CTACF_ME del tipo de cuenta) donde se acredita. */
    private String liabilityAccountCode;

    public FixedTermDepositProvision() {
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Date getProvisionUntilDate() {
        return provisionUntilDate;
    }

    public void setProvisionUntilDate(Date provisionUntilDate) {
        this.provisionUntilDate = provisionUntilDate;
    }

    public Date getProvisionFromDate() {
        return provisionFromDate;
    }

    public void setProvisionFromDate(Date provisionFromDate) {
        this.provisionFromDate = provisionFromDate;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getProvision() {
        return provision;
    }

    public void setProvision(BigDecimal provision) {
        this.provision = provision;
    }

    public String getLiabilityAccountCode() {
        return liabilityAccountCode;
    }

    public void setLiabilityAccountCode(String liabilityAccountCode) {
        this.liabilityAccountCode = liabilityAccountCode;
    }

    /* ---- Accesos de conveniencia para la grilla ---- */

    public String getAccountTypeName() {
        return account != null && account.getAccountType() != null ? account.getAccountType().getName() : "";
    }

    public String getFirstName() {
        return account != null && account.getPartner() != null ? account.getPartner().getFirstName() : "";
    }

    public String getLastName() {
        return account != null && account.getPartner() != null ? account.getPartner().getLastName() : "";
    }

    public Date getOpeningDate() {
        return account != null ? account.getOpeningDate() : null;
    }

    public Date getExpirationDate() {
        return account != null ? account.getExpirationDate() : null;
    }

    public String getCode() {
        return account != null ? account.getCode() : "";
    }

    public FinancesCurrencyType getCurrency() {
        return account != null ? account.getCurrency() : null;
    }

    public AccountState getAccountState() {
        return account != null ? account.getAccountState() : null;
    }

    public BigDecimal getCapital() {
        return account != null ? account.getCapital() : BigDecimal.ZERO;
    }
}
