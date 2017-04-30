package com.encens.khipus.service.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.admin.AdministrativeNotification;
import com.encens.khipus.model.admin.User;

import javax.ejb.Local;
import java.util.List;

/**
 * Administrative Notification Manager
 *
 * @author
 * @version 2.18
 */
@Local
public interface AdministrativeNotificationService {

    List<AdministrativeNotification> getAdministrativeNotificationList();

    AdministrativeNotification getAdministrativeNotification(User currentUser);

    void setRead(Long id, User currentUser);

    void create(AdministrativeNotification administrativeNotification);

    void update(AdministrativeNotification administrativeNotification) throws EntryNotFoundException;

    void delete(AdministrativeNotification administrativeNotification) throws ConcurrencyException;

    AdministrativeNotification findById(Long id) throws EntryNotFoundException;
}
