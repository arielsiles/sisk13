package com.encens.khipus.model.admin;

import com.encens.khipus.model.BaseModel;

import java.util.Date;

/**
 * Class to hold information about logged in users to keep track information about user sessions
 * Note that this class does not persist in the database, its instances will live in the application context
 *
 * @author
 * @version 2.17
 */
public class SessionUserLog implements BaseModel {

    private Long userId;
    private String name;
    private Date lastLogin;
    private Date penultimateAction;
    private Date lastAction;
    private Date lastLogout;
    private String ipAddress;
    private int openSessions;

    public SessionUserLog(Long userId, String name, String ipAddress) {
        this.userId = userId;
        this.name = name;
        this.ipAddress = ipAddress;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastAction() {
        return lastAction;
    }

    public void setLastAction(Date lastAction) {
        this.penultimateAction = this.lastAction;
        this.lastAction = lastAction;
    }

    public Date getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(Date lastLogout) {
        this.lastLogout = lastLogout;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Call this method when the user logs in
     */
    public void addOpenSessions() {
        openSessions++;
    }

    /**
     * Call this function when the user logs off
     */
    public void removeOpenSessions() {
        openSessions--;
    }

    /**
     * Restores the penultimate action to ignore the last action
     */
    public void ignoreLastAction() {
        if (penultimateAction != null) {
            lastAction = penultimateAction;
            penultimateAction = null;
        }
    }

    /**
     * The user is online if there are more than zero open sessions
     *
     * @return whether or not the user is online
     */
    public boolean isOnline() {
        return openSessions > 0;
    }

    public Object getId() {
        return userId;
    }
}
