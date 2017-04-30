package com.encens.khipus.service.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.admin.AdministrativeNotification;
import com.encens.khipus.model.admin.Role;
import com.encens.khipus.model.admin.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;

import javax.ejb.Remove;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is Application scoped
 *
 * @author
 * @version 2.18
 */
@Name("administrativeNotificationService")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Synchronized
public class AdministrativeNotificationServiceBean implements AdministrativeNotificationService {

    private List<AdministrativeNotification> administrativeNotificationList = new ArrayList<AdministrativeNotification>();

    @Logger
    private Log log;

    public List<AdministrativeNotification> getAdministrativeNotificationList() {
        return administrativeNotificationList;
    }

    /**
     * Returns an administrative notification for a given user.
     * The notification must be active, the user must be in at least one of the notification roles
     * and the notification must be unread by the user.
     * There are priority for the oldest notification, it means with the older publish date.
     *
     * @param currentUser the user checking for a new notification
     * @return administrative notification if available, null otherwise
     */
    public AdministrativeNotification getAdministrativeNotification(User currentUser) {
        List<AdministrativeNotification> filteredList = new ArrayList<AdministrativeNotification>();

        for (AdministrativeNotification administrativeNotification : administrativeNotificationList) {
            if (administrativeNotification.isActive()) { // The notification must be active
                for (Role role : currentUser.getRoles()) {
                    // The user must have the required role
                    // The notification must be unread by the user
                    if ((administrativeNotification.getRoleList().contains(null)
                            || administrativeNotification.getRoleList().contains(role)) && !administrativeNotification.getUserReadList().contains(currentUser)) {
                        filteredList.add(administrativeNotification);
                        break;
                    }
                }
            }
        }

        Collections.sort(filteredList, new Comparator<AdministrativeNotification>() {
            public int compare(AdministrativeNotification an1, AdministrativeNotification an2) {
                return an1.getPublishDate().compareTo(an2.getPublishDate());
            }
        });

        if (filteredList.size() > 0) {
            AdministrativeNotification original = filteredList.get(0);
            AdministrativeNotification copy = new AdministrativeNotification();
            copy.setId(original.getId());
            copy.setTitle(original.getTitle());
            copy.setPublishDate(original.getPublishDate());
            copy.setExpirationDate(original.getExpirationDate());
            copy.setDescription(original.getDescription());
            copy.setCreatedBy(original.getCreatedBy());
            copy.setAdministrativeEventType(original.getAdministrativeEventType());
            copy.setRoleList(original.getRoleList());
            copy.setUserReadList(original.getUserReadList());
            copy.setCompany(original.getCompany());

            return copy;
        } else {
            return null;
        }
    }

    /**
     * After display a notification and when the user closes the message dialog,
     * the notification must be marked as read
     *
     * @param id          the administrative notification id
     * @param currentUser the notificatión reader
     */
    public void setRead(Long id, User currentUser) {
        try {
            AdministrativeNotification administrativeNotification = findById(id);
            administrativeNotification.getUserReadList().add(currentUser);
        } catch (EntryNotFoundException ex) {
        }
    }

    public void create(AdministrativeNotification administrativeNotification) {
        administrativeNotification.setId(new Long(administrativeNotificationList.size() + 1));
        administrativeNotificationList.add(administrativeNotification);
    }

    public void update(AdministrativeNotification an) throws EntryNotFoundException {
        AdministrativeNotification administrativeNotification = findById(an.getId());
        an.getUserReadList().clear();
    }

    public void delete(AdministrativeNotification administrativeNotification) throws ConcurrencyException {
        if (!administrativeNotificationList.remove(administrativeNotification)) {
            throw new ConcurrencyException();
        }
    }

    public AdministrativeNotification findById(Long id) throws EntryNotFoundException {
        for (AdministrativeNotification administrativeNotification : administrativeNotificationList) {
            if (administrativeNotification.getId() == id) {
                return administrativeNotification;
            }
        }

        throw new EntryNotFoundException();
    }

    @Destroy
    @Remove
    public void destroy() {
    }
}
