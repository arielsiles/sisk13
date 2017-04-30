package com.encens.khipus.action;

import com.encens.khipus.util.KhipusCacheManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class that holds logged in user information in the session.
 *
 * @author
 * @version 1.0
 */

@Name("sessionUser")
@Scope(ScopeType.SESSION)
@AutoCreate
public class SessionUser implements Serializable, HttpSessionBindingListener {
    @Logger
    private Log log;
    private Long userId;
    private Integer rowsPerPage = 10; //default
    private Integer maxFilesQuantity = 10; //default
    private TimeZone timeZone = TimeZone.getTimeZone("GMT-4"); //default
    private Locale locale = new Locale("es");
    private List<Long> businessUnitIds = new ArrayList<Long>();

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public Integer getMaxFilesQuantity() {
        return maxFilesQuantity;
    }

    public void setMaxFilesQuantity(Integer maxFilesQuantity) {
        this.maxFilesQuantity = maxFilesQuantity;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public List<Long> getBusinessUnitIds() {
        return businessUnitIds;
    }

    public void setBusinessUnitIds(List<Long> businessUnitIds) {
        this.businessUnitIds = businessUnitIds;
    }

    public void valueBound(HttpSessionBindingEvent httpSessionBindingEvent) {
    }

    /**
     * method call before of that SessionUser is destroy
     *
     * @param httpSessionBindingEvent
     */
    public void valueUnbound(HttpSessionBindingEvent httpSessionBindingEvent) {
        //delete session temp folder
        log.debug("Executing deleteSessionsFolder...");
        String sessionId = httpSessionBindingEvent.getSession().getId();
        boolean success = KhipusCacheManager.deleteSessionFolder(sessionId);
        log.debug("result in delete folder:" + success);

        Events.instance().raiseEvent("SessionUserLogAction.userLoggedOut", userId);
        log.debug("raised event: SessionUserLogAction.userLoggedOut");

        Events.instance().raiseEvent("SessionUserUpdaterAction.loggedOut", userId);
        log.debug("raised event: SessionUserUpdaterAction.loggedOut");
    }

    /**
     * Observes the events in the JSF life cycle to register an action in the
     * {@Link com.encens.khipus.action.admin.SessionUserLogAction}
     *
     * @param event the phase in the JSF life cycle
     */
    @Observer("org.jboss.seam.beforePhase")
    public void registerAction(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW && userId != null) {
            Events.instance().raiseEvent("SessionUserLogAction.userAction", userId);
            log.debug("raised event: SessionUserLogAction.userAction");
        }
    }
}
