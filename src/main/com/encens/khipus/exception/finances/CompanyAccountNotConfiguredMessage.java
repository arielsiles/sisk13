package com.encens.khipus.exception.finances;

import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.Name;

/**
 * Arma el mensaje de la <code>CompanyAccountNotConfiguredException</code> para
 * mostrarlo en la vista, nombrando la cuenta que falta configurar.
 * <p>
 * Lo invoca el <code>&lt;exception&gt;</code> de WEB-INF/pages.xml via
 * <code>#{companyAccountNotConfiguredMessage.getMessage(org.jboss.seam.caughtException)}</code>.
 * Mismo patron que {@link com.encens.khipus.exception.admin.BusinessUnitAccessMessage}.
 *
 * @author
 * @version 1.0
 */
@Name("companyAccountNotConfiguredMessage")
public class CompanyAccountNotConfiguredMessage {

    public String getMessage(Exception e) {
        CompanyAccountNotConfiguredException exception = getCompanyAccountNotConfiguredException(e);
        if (null != exception) {
            // La etiqueta ya incluye el nombre de columna entre parentesis
            // (ej. "Costo de producto PV (ctacostpv)"), asi que el mensaje
            // identifica el campo tal como se ve en la pantalla y tal como esta
            // en la base.
            String label = MessageUtils.getMessage(exception.getLabelKey());
            return MessageUtils.getMessage("CompanyConfiguration.account.notConfigured", label);
        }

        return null;
    }

    private CompanyAccountNotConfiguredException getCompanyAccountNotConfiguredException(Exception e) {
        for (Exception cause = e; cause != null; cause = org.jboss.seam.util.Exceptions.getCause(cause)) {
            if (cause instanceof CompanyAccountNotConfiguredException) {
                return (CompanyAccountNotConfiguredException) cause;
            }
        }

        return null;
    }
}
