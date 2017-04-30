package com.encens.khipus.action.admin;

import com.encens.khipus.model.admin.AdministrativeNotification;
import com.encens.khipus.service.admin.AdministrativeNotificationService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.security.Restrict;

import java.util.ArrayList;
import java.util.List;

/**
 * Administrative Notification data model
 *
 * @author
 * @version 2.18
 */
@Name("administrativeNotificationDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('ADMINISTRATIVENOTIFICATION','VIEW')}")
public class AdministrativeNotificationDataModel {
    @In
    private AdministrativeNotificationService administrativeNotificationService;

    @DataModel
    private List<AdministrativeNotification> administrativeNotificationList;

    private String criteria;
    private List<String> sortPriority;

    @Create
    public void init() {
        sortPriority = new ArrayList<String>();
        sortPriority.add("active");
        sortPriority.add("publishDate");
    }

    @Factory("administrativeNotificationList")
    public void loadAdministrativeNotificationList() {
        administrativeNotificationList = administrativeNotificationService.getAdministrativeNotificationList();
    }

    /**
     * Search a notification using a criteria over the title.
     * The wildcard "%" is allowed in the criteria.
     */
    public void search() {
        loadAdministrativeNotificationList();

        if (criteria != null && !criteria.equals("")) {
            List<AdministrativeNotification> filteredList = new ArrayList<AdministrativeNotification>();
            for (AdministrativeNotification administrativeNotification : administrativeNotificationList) {

                String regex = "[\\w|\\W]*" + criteria.toLowerCase().replace("%", "[\\w|\\W]*") + "[\\w|\\W]*";
                if (administrativeNotification.getTitle().toLowerCase().matches(regex)) {
                    filteredList.add(administrativeNotification);
                }
            }
            administrativeNotificationList = filteredList;
        }
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public List<String> getSortPriority() {
        return sortPriority;
    }

    public void setSortPriority(List<String> sortPriority) {
        this.sortPriority = sortPriority;
    }
}
