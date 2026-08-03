package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.ExchangeKind;
import com.encens.khipus.model.finances.FinancesExchangeRate;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ABM de tipos de cambio de contabilidad (tabla <code>arcgtc</code>): clase de cambio
 * (D = dolar, U = UFV), fecha y valor.
 * <p/>
 * Ojo: no confundir con {@link ExchangeRateAction}, que administra la tabla
 * <code>tipocambio</code> del esquema de la aplicacion y la usan las planillas.
 * La que consume la contabilidad (provision de DPF, comprobantes en ME) es esta.
 * <p/>
 * La clave primaria es compuesta (clase de cambio + fecha) y ambas columnas son
 * inmutables, por eso la edicion solo permite corregir el valor: para cambiar clase
 * o fecha hay que borrar y volver a dar de alta.
 *
 * @author
 */
@Name("financesExchangeRateAction")
@Scope(ScopeType.CONVERSATION)
public class FinancesExchangeRateAction extends GenericAction<FinancesExchangeRate> {

    /** Clase de cambio elegida en el combo; se vuelca a la PK recien al crear. */
    private ExchangeKind exchangeKind;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @Factory(value = "financesExchangeRate", scope = ScopeType.STATELESS)
    public FinancesExchangeRate initFinancesExchangeRate() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('FINANCESEXCHANGERATE','VIEW')}")
    public String select(FinancesExchangeRate instance) {
        String outcome = super.select(instance);
        if (Outcome.SUCCESS.equals(outcome)) {
            /** El combo se muestra deshabilitado en edicion, pero igual necesita su valor. */
            this.exchangeKind = getInstance().getExchangeKind();
        }
        return outcome;
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('FINANCESEXCHANGERATE','CREATE')}")
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        getInstance().getId().setExchangeKind(exchangeKind.getId());
        getInstance().getId().setDate(DateUtils.removeTime(getInstance().getId().getDate()));
        return super.create();
    }

    /**
     * En la edicion la PK no se toca: solo se persiste el valor. No se delega en
     * <code>super.update()</code> porque la entidad no tiene campo <code>@Version</code>
     * y el manejo de version del generico no aplica.
     */
    @Override
    @End
    @Restrict("#{s:hasPermission('FINANCESEXCHANGERATE','UPDATE')}")
    public String update() {
        if (!validateRate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    @End
    @Restrict("#{s:hasPermission('FINANCESEXCHANGERATE','DELETE')}")
    public String delete() {
        return super.delete();
    }

    private boolean validate() {
        if (exchangeKind == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinancesExchangeRate.error.exchangeKindRequired");
            return false;
        }
        Date date = getInstance().getId().getDate();
        if (date == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinancesExchangeRate.error.dateRequired");
            return false;
        }
        if (!validateRate()) {
            return false;
        }
        FinancesExchangeRate duplicated =
                financesExchangeRateService.findByExchangeKindAndDate(exchangeKind.getId(), DateUtils.removeTime(date));
        if (duplicated != null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinancesExchangeRate.error.duplicated",
                    exchangeKind.getId(), DateUtils.format(date, "dd/MM/yyyy"));
            return false;
        }
        return true;
    }

    private boolean validateRate() {
        BigDecimal rate = getInstance().getRate();
        if (rate == null || rate.doubleValue() <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinancesExchangeRate.error.invalidRate");
            return false;
        }
        return true;
    }

    /** Fecha de la PK ya formateada, para los mensajes de la vista. */
    public String getFormattedDate() {
        Date date = getInstance().getId() != null ? getInstance().getId().getDate() : null;
        return date != null ? DateUtils.format(date, "dd/MM/yyyy") : "";
    }

    public ExchangeKind getExchangeKind() {
        return exchangeKind;
    }

    public void setExchangeKind(ExchangeKind exchangeKind) {
        this.exchangeKind = exchangeKind;
    }
}
