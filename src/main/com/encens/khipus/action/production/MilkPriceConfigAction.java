package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.MilkPriceConfig;
import com.encens.khipus.service.production.MilkPriceConfigService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

@Name("milkPriceConfigAction")
@Scope(ScopeType.CONVERSATION)
public class MilkPriceConfigAction extends GenericAction<MilkPriceConfig> {

    @In
    private MilkPriceConfigService milkPriceConfigService;

    @Factory(value = "milkPriceConfig", scope = ScopeType.STATELESS)
    public MilkPriceConfig initMilkPriceConfig() {
        // Los precios por defecto (5.00 / 4.50 / 4.00 / 4.00) y el estado ENABLE
        // vienen de los inicializadores de campo de la entidad.
        return getInstance();
    }

    @End
    @Override
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @End
    @Override
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    private boolean validate() {
        MilkPriceConfig instance = getInstance();
        if (instance.getStartDate() == null || instance.getEndDate() == null
                || instance.getStartDate().compareTo(instance.getEndDate()) > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MilkPriceConfig.error.invalidDates");
            return false;
        }
        if (!milkPriceConfigService.findOverlapping(
                instance.getStartDate(), instance.getEndDate(), instance.getId()).isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MilkPriceConfig.error.overlap");
            return false;
        }
        return true;
    }

    public Boolean getState() {
        return getInstance().getState() != null && getInstance().getState().equals("ENABLE");
    }

    public void setState(Boolean state) {
        getInstance().setState(state ? "ENABLE" : "DISABLE");
    }
}
