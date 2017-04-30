package com.encens.khipus.action.admin;

import com.encens.khipus.model.admin.AdministrativeNotification;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.service.admin.AdministrativeNotificationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;

/**
 * Notification action
 * This action class is used in conjunction with a poll in every page in the system. The poll asks continuously
 * for new notification messages.
 *
 * @author
 * @version 2.18
 */
@Name("notificationAction")
@Scope(ScopeType.SESSION)
public class NotificationAction {

    @In
    AdministrativeNotificationService administrativeNotificationService;

    @In
    private User currentUser;

    private AdministrativeNotification administrativeNotification;

    public AdministrativeNotification getAdministrativeNotification() {
        return administrativeNotification;
    }

    /**
     * After display a notification and when the user closes the message dialog,
     * the notification must be marked as read and the poll should be set to enabled to
     * continue asking for new messages.
     */
    public void markAsRead() {
        if (administrativeNotification != null) {
            administrativeNotificationService.setRead(administrativeNotification.getId(), currentUser);
            administrativeNotification = null;
        }
    }

    /**
     * The poll is enabled and continues asking for notifications while there are nothing to show yet.
     * But when there is one or more messages to show, then the poll is disabled
     *
     * @return whether the poll is enabled or not
     */
    public boolean isPollEnabled() {
        Events.instance().raiseEvent("SessionUserLogAction.ignoreLastUserAction", currentUser.getId());
        administrativeNotification = administrativeNotificationService.getAdministrativeNotification(currentUser);
        return administrativeNotification == null;
    }

    public boolean logUserAction() {
        Events.instance().raiseEvent("SessionUserLogAction.userAction", currentUser.getId());
        return true;
    }
}
