package com.encens.khipus.action.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.contacts.Gender;
import com.encens.khipus.model.employees.WeeklyWorkload;
import com.encens.khipus.service.employees.WeeklyWorkloadService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;

/**
 * CRUD de la jornada semanal por genero.
 *
 * @author
 * @version 6.1.0
 */
@Name("weeklyWorkloadAction")
@Scope(ScopeType.CONVERSATION)
public class WeeklyWorkloadAction extends GenericAction<WeeklyWorkload> {

    @In
    private WeeklyWorkloadService weeklyWorkloadService;

    @Factory(value = "weeklyWorkload")
    @Restrict("#{s:hasPermission('WEEKLYWORKLOAD','VIEW')}")
    public WeeklyWorkload initWeeklyWorkload() {
        return getInstance();
    }

    @Factory(value = "weeklyWorkloadGender")
    public Gender[] getGenderValues() {
        return Gender.values();
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('WEEKLYWORKLOAD','VIEW')}")
    public String select(WeeklyWorkload instance) {
        try {
            setOp(OP_UPDATE);
            setInstance(getService().findById(WeeklyWorkload.class, instance.getId()));
            return Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.FAIL;
        }
    }

    @Override
    @Restrict("#{s:hasPermission('WEEKLYWORKLOAD','CREATE')}")
    public String create() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.create();
    }

    @Override
    @Restrict("#{s:hasPermission('WEEKLYWORKLOAD','UPDATE')}")
    public String update() {
        if (!validate()) {
            return Outcome.REDISPLAY;
        }
        return super.update();
    }

    @Override
    @Restrict("#{s:hasPermission('WEEKLYWORKLOAD','DELETE')}")
    public String delete() {
        return super.delete();
    }

    /**
     * Solo puede haber una jornada vigente por genero: con dos activas no habria forma de
     * saber cual se aplica. Las anteriores se desactivan, no se borran, para no perder con
     * que numero se calcularon los meses ya cerrados.
     */
    private boolean validate() {
        boolean valid = true;
        WeeklyWorkload instance = getInstance();

        if (Boolean.TRUE.equals(instance.getActive())) {
            Long active = weeklyWorkloadService.countActiveByGender(instance.getGender(),
                    isManaged() ? instance.getId() : null);
            if (null != active && active > 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "WeeklyWorkload.error.activeExists");
                valid = false;
            }
        }

        if (null != instance.getWeeklyHours()
                && instance.getWeeklyHours().compareTo(BigDecimal.ZERO) <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WeeklyWorkload.error.weeklyHoursPositive");
            valid = false;
        }

        if (null != instance.getDailyHours()
                && instance.getDailyHours().compareTo(BigDecimal.ZERO) <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WeeklyWorkload.error.dailyHoursPositive");
            valid = false;
        }

        if (null != instance.getWeeklyHours() && null != instance.getDailyHours()
                && instance.getDailyHours().compareTo(instance.getWeeklyHours()) > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "WeeklyWorkload.error.dailyOverWeekly");
            valid = false;
        }

        return valid;
    }

    @Override
    protected GenericService getService() {
        return weeklyWorkloadService;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "description";
    }
}
