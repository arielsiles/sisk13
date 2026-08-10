package com.encens.khipus.model.customers;

import java.util.Date;

/**
 * Una operacion sobre un DPF que no corresponde hacer desde un comprobante suelto.
 * <p/>
 * Es el resultado de
 * {@link com.encens.khipus.service.accouting.VoucherAccoutingService#findFixedTermDepositConflict}
 * y existe justamente para NO usar una excepcion: la validacion tiene que poder consultarse
 * antes de abrir la transaccion, sin riesgo de marcarla para rollback.
 *
 * @author
 */
public class FixedTermDepositConflict {

    public enum Type {
        /** Abono a un certificado ya abierto: un DPF no recibe dinero a mitad de plazo. */
        CAPITAL_INCREASE,
        /** Debito que deja saldo: el retiro parcial no existe, un DPF se cierra entero. */
        PARTIAL_WITHDRAWAL,
        /** Debito que cierra el certificado: corresponde hacerlo desde "Cerrar DPF". */
        TOTAL_WITHDRAWAL
    }

    private final Type type;
    private final String accountCode;
    /** Solo en CAPITAL_INCREASE. */
    private final Date openingDate;
    /** Solo en CAPITAL_INCREASE. */
    private final Date voucherDate;

    private FixedTermDepositConflict(Type type, String accountCode, Date openingDate, Date voucherDate) {
        this.type = type;
        this.accountCode = accountCode;
        this.openingDate = openingDate;
        this.voucherDate = voucherDate;
    }

    public static FixedTermDepositConflict capitalIncrease(String accountCode, Date openingDate, Date voucherDate) {
        return new FixedTermDepositConflict(Type.CAPITAL_INCREASE, accountCode, openingDate, voucherDate);
    }

    public static FixedTermDepositConflict withdrawal(String accountCode, boolean total) {
        return new FixedTermDepositConflict(
                total ? Type.TOTAL_WITHDRAWAL : Type.PARTIAL_WITHDRAWAL, accountCode, null, null);
    }

    public Type getType() {
        return type;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }
}
