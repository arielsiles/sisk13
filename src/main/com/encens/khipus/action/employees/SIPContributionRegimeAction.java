package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.SIPContributionRegime;
import com.encens.khipus.service.employees.SIPContributionRegimeService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

/**
 * CRUD del catalogo de regimenes de aportes al Sistema Integral de Pensiones.
 *
 * @author
 * @version 6.0.129
 */
@Name("sipContributionRegimeAction")
@Scope(ScopeType.CONVERSATION)
public class SIPContributionRegimeAction extends GenericAction<SIPContributionRegime> {

    @In
    private SIPContributionRegimeService sipContributionRegimeService;

    @Factory(value = "sipContributionRegime")
    @Restrict("#{s:hasPermission('SIPCONTRIBUTIONREGIME','VIEW')}")
    public SIPContributionRegime initSIPContributionRegime() {
        return getInstance();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('SIPCONTRIBUTIONREGIME','VIEW')}")
    public String select(SIPContributionRegime instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(SIPContributionRegime.class, instance.getId()));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SIPCONTRIBUTIONREGIME','CREATE')}")
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        clearOtherDefaultRegime();
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('SIPCONTRIBUTIONREGIME','UPDATE')}")
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        clearOtherDefaultRegime();
        return super.update();
    }

    @Override
    @Restrict("#{s:hasPermission('SIPCONTRIBUTIONREGIME','DELETE')}")
    public String delete() {
        Long assignedContracts = sipContributionRegimeService.countContractsByRegime(getInstance());
        if (null != assignedContracts && assignedContracts > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "SIPContributionRegime.error.hasContracts", getInstance().getName(), assignedContracts);
            return Outcome.REDISPLAY;
        }
        if (getInstance().getByDefault()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "SIPContributionRegime.error.cannotDeleteDefault", getInstance().getName());
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    /**
     * El regimen por defecto es el que se aplica a todo contrato que no tenga uno asignado,
     * asi que tiene que existir exactamente uno y tiene que estar activo.
     */
    private boolean validate() {
        boolean valid = true;

        Long duplicated = isManaged()
                ? sipContributionRegimeService.countByNameButThis(getInstance().getName(), getInstance().getId())
                : sipContributionRegimeService.countByName(getInstance().getName());
        if (null != duplicated && duplicated > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "Common.message.duplicated", getInstance().getName());
            valid = false;
        }

        if (getInstance().getByDefault() && !getInstance().getActive()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "SIPContributionRegime.error.defaultMustBeActive");
            valid = false;
        }

        if (isManaged() && !getInstance().getByDefault()) {
            SIPContributionRegime currentDefault = sipContributionRegimeService.findDefault();
            if (null == currentDefault) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "SIPContributionRegime.error.defaultRequired");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Solo puede haber un regimen por defecto: al marcar uno se desmarca el anterior.
     */
    private void clearOtherDefaultRegime() {
        if (!getInstance().getByDefault()) {
            return;
        }
        SIPContributionRegime currentDefault = sipContributionRegimeService.findDefault();
        if (null != currentDefault && !currentDefault.getId().equals(getInstance().getId())) {
            currentDefault.setByDefault(Boolean.FALSE);
            try {
                sipContributionRegimeService.update(currentDefault);
            } catch (Exception e) {
                log.error("No se pudo desmarcar el regimen por defecto anterior", e);
            }
        }
    }

    @Override
    protected GenericService getService() {
        return sipContributionRegimeService;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
