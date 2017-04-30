package com.encens.khipus.action.admin;

import com.encens.khipus.model.admin.SessionUserLog;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is Application scoped to allow keep track of the user sessions along the entire application
 *
 * @author
 * @version 2.17
 */
@Name("userSessionAction")
@Scope(ScopeType.APPLICATION)
public class SessionUserLogAction {

    private Map<Long, SessionUserLog> sessionUserLogMap = new HashMap<Long, SessionUserLog>();

    public Map<Long, SessionUserLog> getSessionUserLogMap() {
        return sessionUserLogMap;
    }

    /**
     * Observes the event SessionUserLogAction.userLoggedIn when an user logs in
     *
     * @param userId    the session user's id
     * @param name      the session user's user name
     * @param ipAddress the session user's IP address
     */
    @Observer("SessionUserLogAction.userLoggedIn")
    public void userLoggedIn(Long userId, String name, String ipAddress) {
        if (!sessionUserLogMap.containsKey(userId)) {
            sessionUserLogMap.put(userId, new SessionUserLog(userId, name, ipAddress));
        }

        SessionUserLog sessionUserLog = sessionUserLogMap.get(userId);
        sessionUserLog.setLastLogin(new Date());
        sessionUserLog.setIpAddress(ipAddress);
        sessionUserLog.addOpenSessions();
    }

    /**
     * Observes the event SessionUserLogAction.userLoggedIn when an user logs off
     *
     * @param userId the session user's id
     */
    @Observer("SessionUserLogAction.userLoggedOut")
    public void userLoggedOut(Long userId) {
        if (sessionUserLogMap.containsKey(userId)) {
            SessionUserLog sessionUserLog = sessionUserLogMap.get(userId);

            sessionUserLog.setLastLogout(new Date());
            sessionUserLog.removeOpenSessions();
        }
    }

    /**
     * Observes the event SessionUserLogAction.userLoggedIn when an user performs an action
     *
     * @param userId the session user's id
     */
    @Observer("SessionUserLogAction.userAction")
    public void userAction(Long userId) {
        if (sessionUserLogMap.containsKey(userId)) {
            SessionUserLog sessionUserLog = sessionUserLogMap.get(userId);

            sessionUserLog.setLastAction(new Date());
        }
    }

    /**
     * Restores the last user action to its previous value
     *
     * @param userId
     */
    @Observer("SessionUserLogAction.ignoreLastUserAction")
    public void ignoreLastUserAction(Long userId) {
        if (sessionUserLogMap.containsKey(userId)) {
            SessionUserLog sessionUserLog = sessionUserLogMap.get(userId);

            sessionUserLog.ignoreLastAction();
        }
    }
}
