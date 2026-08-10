package com.encens.khipus.model.finances;

/**
 * Referencia colgada a una cuenta contable: una columna que guarda un codigo que
 * NO existe en el plan de cuentas `arcgms` (por la clave real no_cia + cuenta).
 * <p>
 * Es un POJO, no una entidad: se arma con SQL nativo a proposito, para NO pasar
 * por el mapeo de {@link CashAccount} (cuyo proxy es justamente el que explota al
 * renderizar). Asi la pantalla de reparacion nunca se rompe.
 *
 * @author
 * @version 1.0
 */
public class DanglingAccount {

    public static final String ORIGIN_CONFIG = "CONFIG";
    public static final String ORIGIN_BANK = "BANK";

    /** ORIGIN_CONFIG (tabla configuracion) u ORIGIN_BANK (tabla ck_ctas_bco). */
    private String origin;

    /** Compania del registro. */
    private String companyNumber;

    /** Columna que guarda el codigo colgado (en config: la columna; en banco: "cuenta"). */
    private String column;

    /** Codigo de cuenta que no existe en arcgms. */
    private String code;

    /** Solo banco: cta_bco (identificador de la cuenta bancaria). */
    private String bankAccountNumber;

    /** Solo banco: descripcion. */
    private String description;

    /** Solo banco: estado (VIG / NOVIG / ...). */
    private String state;

    /** Marca del checkbox en la pantalla de reparacion. */
    private boolean selected;

    public DanglingAccount() {
    }

    /**
     * Clave estable para el rowKey de la tabla y para ubicar la fila al anular.
     */
    public String getKey() {
        if (ORIGIN_BANK.equals(origin)) {
            return "BANK|" + companyNumber + "|" + bankAccountNumber;
        }
        return "CONFIG|" + companyNumber + "|" + column;
    }

    public boolean isBank() {
        return ORIGIN_BANK.equals(origin);
    }

    public boolean isConfig() {
        return ORIGIN_CONFIG.equals(origin);
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
