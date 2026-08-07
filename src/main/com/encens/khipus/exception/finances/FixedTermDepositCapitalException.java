package com.encens.khipus.exception.finances;

import java.util.Date;

/**
 * Se lanza cuando un comprobante intenta acreditar capital a un Deposito a Plazo Fijo
 * en una fecha posterior a su apertura.
 * <p>
 * Un DPF no recibe dinero a mitad de plazo: si el socio quiere aumentar el capital, el
 * certificado se cierra y se abre uno nuevo. Cuando el aumento se cargaba como un
 * asiento suelto desde la pantalla de comprobantes, la contabilidad quedaba con un
 * capital y la ficha de la cuenta con otro, sin que nada lo advirtiera. Se detectaron
 * asi ME00156 (+8.000) y ME00155 (+5.300).
 * <p>
 * Es unchecked porque <code>saveVoucher</code> se invoca desde decenas de flujos que no
 * tienen forma de recuperarse: la respuesta siempre es la misma, hacer la operacion por
 * la via de la renovacion. La maneja el <code>&lt;exception&gt;</code> declarado en
 * WEB-INF/pages.xml, que arma el texto con {@link FixedTermDepositCapitalMessage}.
 *
 * @author
 * @version 1.0
 */
public class FixedTermDepositCapitalException extends RuntimeException {

    private final String accountCode;
    private final Date openingDate;
    private final Date voucherDate;

    public FixedTermDepositCapitalException(String accountCode, Date openingDate, Date voucherDate) {
        super("El comprobante acredita capital al DPF '" + accountCode + "' con fecha posterior a su apertura");
        this.accountCode = accountCode;
        this.openingDate = openingDate;
        this.voucherDate = voucherDate;
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
