package com.encens.khipus.action.admin;

import com.encens.khipus.action.SessionUser;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.service.admin.UserBusinessUnitService;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.util.SessionUserUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 2.23
 */
@Name("sessionUserUpdaterAction")
@Scope(ScopeType.APPLICATION)
public class SessionUserUpdaterAction {
    private static final LogProvider log = Logging.getLogProvider(SessionUserUpdaterAction.class);

    @In
    private UserService userService;

    @In
    private UserBusinessUnitService userBusinessUnitService;

    private Map<Long, HttpSession> sessionMap = new HashMap<Long, HttpSession>();

    @Observer("SessionUserUpdaterAction.loggedIn")
    public void loggedIn(Long id, HttpSession session) {
        log.debug("Adding userId: " + id);
        sessionMap.put(id, session);
    }

    @Observer("SessionUserUpdaterAction.loggedOut")
    public void loggedOut(Long id) {
        log.debug("Removing userId: " + id);
        sessionMap.remove(id);
    }

    @Observer("SessionUserUpdaterAction.updateUserSessionBusinessUnit")
    public void updateUserSessionBusinessUnit(Long id) {
        HttpSession httpSession = sessionMap.get(id);

        if (null != httpSession) {
            SessionUser sessionUser = (SessionUser) httpSession.getAttribute("sessionUser");

            User user = getUser(id);
            if (null != user) {
                sessionUser.setBusinessUnitIds(SessionUserUtil.i.getSessionUserBusinessUnitIds(userBusinessUnitService.readBusinessUnits(user)));
            }
        }
    }

    private User getUser(Long id) {
        try {
            return userService.findById(User.class, id);
        } catch (EntryNotFoundException e) {
            return null;
        }
    }
}
