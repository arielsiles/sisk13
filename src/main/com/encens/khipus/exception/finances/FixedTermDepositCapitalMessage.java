package com.encens.khipus.exception.finances;

import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.Name;

/**
 * Arma el mensaje de la {@link FixedTermDepositCapitalException} para mostrarlo en la
 * vista, nombrando el certificado y las dos fechas que no cuadran.
 * <p>
 * Lo invoca el <code>&lt;exception&gt;</code> de WEB-INF/pages.xml via
 * <code>#{fixedTermDepositCapitalMessage.getMessage(org.jboss.seam.caughtException)}</code>.
 * Mismo patron que {@link CompanyAccountNotConfiguredMessage}.
 *
 * @author
 * @version 1.0
 */
@Name("fixedTermDepositCapitalMessage")
public class FixedTermDepositCapitalMessage {

    public String getMessage(Exception e) {
        FixedTermDepositCapitalException exception = getFixedTermDepositCapitalException(e);
        if (null != exception) {
            return MessageUtils.getMessage("Account.error.dpfCapitalAfterOpening",
                    exception.getAccountCode(),
                    DateUtils.format(exception.getOpeningDate(), "dd/MM/yyyy"),
                    DateUtils.format(exception.getVoucherDate(), "dd/MM/yyyy"));
        }
        return null;
    }

    private FixedTermDepositCapitalException getFixedTermDepositCapitalException(Exception e) {
        for (Exception cause = e; cause != null; cause = org.jboss.seam.util.Exceptions.getCause(cause)) {
            if (cause instanceof FixedTermDepositCapitalException) {
                return (FixedTermDepositCapitalException) cause;
            }
        }
        return null;
    }
}
