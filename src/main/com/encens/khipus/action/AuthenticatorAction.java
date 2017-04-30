package com.encens.khipus.action;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.service.admin.UserBusinessUnitService;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.util.Hash;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.SessionUserUtil;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Authenticator action class
 *
 * @author
 * @version 1.0
 */
@Name("authenticator")
public class AuthenticatorAction implements Serializable {

    private static final long serialVersionUID = -3938569313203918047L;

    @Logger
    private Log log;

    @In
    private UserService userService;

    @In
    private UserBusinessUnitService userBusinessUnitService;

    @In
    private SessionUser sessionUser;

    @In
    private FacesContext facesContext;

    @Out(required = false)
    @In(required = false)
    private User currentUser;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Company currentCompany;

    private String companyLogin;//TODO override Credentials

    @In
    Credentials credentials;

    @In
    private AppIdentity identity;

    public boolean authenticate() {
        try {
            currentUser = userService.findByUsernameAndPasswordAndCompany(credentials.getUsername(),
                    Hash.instance().hash(credentials.getPassword()),
                    companyLogin);

            currentCompany = currentUser.getCompany();
            log.debug("The companyId: " + currentCompany.getId());

            if (!ValidatorUtil.isEmptyOrNull(currentUser.getRoles())) {
                identity.setPermissions(userService.getPermissions(currentUser));
            }

            sessionUser.setUserId(currentUser.getId());
            sessionUser.setBusinessUnitIds(SessionUserUtil.i.getSessionUserBusinessUnitIds(userBusinessUnitService.readBusinessUnits(currentUser)));

            raiseEvents();

            return true;
        } catch (EntryNotFoundException e) {
            return false;
        }
    }

    private void raiseEvents() {
        String ipAddress = ((HttpServletRequest) facesContext.getExternalContext().getRequest()).getRemoteAddr();
        Events.instance().raiseEvent("SessionUserLogAction.userLoggedIn",
                sessionUser.getUserId(),
                currentUser.getEmployee().getFullName(),
                ipAddress);
        log.debug("raised event: SessionUserLogAction.userLoggedIn");

        Events.instance().raiseEvent("SessionUserUpdaterAction.loggedIn",
                sessionUser.getUserId(),
                JSFUtil.getHttpSession());
        log.debug("raised event: SessionUserUpdaterAction.loggedIn");
    }

    public void logOut() {
        identity.logout();
    }

    public void checkIfLoogedIn() {
        log.debug("Method called to force the redirection to home if user currently is logged in");
    }

    public String getCompanyLogin() {
        return companyLogin;
    }

    public void setCompanyLogin(String companyLogin) {
        this.companyLogin = companyLogin;
    }
}
