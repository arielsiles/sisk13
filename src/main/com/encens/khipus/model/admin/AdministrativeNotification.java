package com.encens.khipus.model.admin;

import com.encens.khipus.model.BaseModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.18
 */
public class AdministrativeNotification implements BaseModel, Cloneable {
    private Long id;
    private String title;
    private Date publishDate;
    private Date expirationDate;
    private String description;

    private User createdBy;
    private AdministrativeEventType administrativeEventType;
    private List<Role> roleList;
    private List<User> userReadList;
    private Company company;

    public AdministrativeNotification() {
        roleList = new ArrayList<Role>();
        setUserReadList(new ArrayList<User>());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publishDate);
        calendar.set(Calendar.SECOND, 0);

        this.publishDate = calendar.getTime();
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expirationDate);
        calendar.set(Calendar.SECOND, 59);

        this.expirationDate = calendar.getTime();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public AdministrativeEventType getAdministrativeEventType() {
        return administrativeEventType;
    }

    public void setAdministrativeEventType(AdministrativeEventType administrativeEventType) {
        this.administrativeEventType = administrativeEventType;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    public List<User> getUserReadList() {
        return userReadList;
    }

    public void setUserReadList(List<User> userReadList) {
        this.userReadList = userReadList;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * The notification is active if the current date/time is between the publish date and the expiration date
     *
     * @return whether or not the notification is active
     */
    public boolean isActive() {
        Date currentDate = new Date();
        return currentDate.after(publishDate) && currentDate.before(expirationDate);
    }
}
