package com.encens.khipus.action.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.admin.AdministrativeEventType;
import com.encens.khipus.model.admin.AdministrativeNotification;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.service.admin.AdministrativeNotificationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.util.Date;

/**
 * Administrative Notification action
 *
 * @author
 * @version 2.18
 */
@Name("administrativeNotificationAction")
@Scope(ScopeType.CONVERSATION)
public class AdministrativeNotificationAction {
    public static final String OP_CREATE = "create";
    public static final String OP_UPDATE = "update";

    @In
    private AdministrativeNotificationService administrativeNotificationService;

    private AdministrativeNotification instance;

    @In
    private User currentUser;

    private String op;

    @In
    protected FacesMessages facesMessages;

    @Logger
    protected Log log;

    @Factory(value = "administrativeNotification")
    @Restrict("#{s:hasPermission('ADMINISTRATIVENOTIFICATION','VIEW')}")
    public AdministrativeNotification initAdministrativeNotification() {
        if (instance == null) {
            instance = new AdministrativeNotification();
            instance.setPublishDate(new Date());
            instance.setExpirationDate(new Date());
            instance.setCreatedBy(currentUser);
        }
        return instance;
    }

    @Factory(value = "administrativeEventType", scope = ScopeType.STATELESS)
    public AdministrativeEventType[] getAdministrativeEventType() {
        return AdministrativeEventType.values();
    }

    @Begin(ifOutcome = com.encens.khipus.framework.action.Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('ADMINISTRATIVENOTIFICATION','VIEW')}")
    public String select(AdministrativeNotification instance) {
        try {
            setOp(OP_UPDATE);
            //define the current instance
            this.instance = instance;
            //Ensure the instance exists in the database, find it
            administrativeNotificationService.findById(instance.getId());
            return com.encens.khipus.framework.action.Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
    }

    @End
    @Restrict("#{s:hasPermission('ADMINISTRATIVENOTIFICATION','CREATE')}")
    public String create() {
        administrativeNotificationService.create(instance);
        addCreatedMessage();
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @End
    @Restrict("#{s:hasPermission('ADMINISTRATIVENOTIFICATION','UPDATE')}")
    public String update() {
        try {
            administrativeNotificationService.update(instance);
            addUpdatedMessage();
            return com.encens.khipus.framework.action.Outcome.SUCCESS;
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return com.encens.khipus.framework.action.Outcome.FAIL;
        }
    }

    @End
    @Restrict("#{s:hasPermission('ADMINISTRATIVENOTIFICATION','DELETE')}")
    public String delete() {
        try {
            administrativeNotificationService.delete(instance);
            addDeletedMessage();
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
        }

        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @End(beforeRedirect = true)
    public String cancel() {
        return com.encens.khipus.framework.action.Outcome.CANCEL;
    }

    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.created", instance.getTitle());
    }

    protected void addUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.updated", instance.getTitle());
    }

    protected void addDeletedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.deleted", instance.getTitle());
    }

    protected void addDeleteConcurrencyMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.message.concurrency.delete", instance.getTitle());
    }

    protected void addNotFoundMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                "Common.error.notFound", instance.getTitle());
    }

    public String getOp() {
        if (op != null) {
            return op;
        } else {
            return OP_CREATE; //by default set the create operation
        }
    }

    public void setOp(String op) {
        this.op = op;
    }

    public boolean isManaged() {
        return OP_UPDATE.equals(getOp());
    }

    public void entryNotFoundLog() {
        log.debug("entity was removed by another user");
    }

}
