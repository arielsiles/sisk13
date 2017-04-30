package com.encens.khipus.action.admin;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.service.admin.UserService;
import com.encens.khipus.util.Hash;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

/**
 * Manages the preferences of the user logged in
 *
 * @author
 * @version 1.0
 */
@Name("userSettingsAction")
@Restrict("#{identity.loggedIn}")
public class UserSettingsAction extends GenericAction<User> {

    @In
    @Out
    private User currentUser;

    @In
    private Company currentCompany;

    @In
    private UserService userService;

    private User temporalUser = new User();

    @Logger
    protected Log log;


    public String changePassword() throws Exception {
        try {
            //searching by id because the username can be changed in between
            userService.findByIdAndPassword(currentUser.getId(), Hash.instance().hash(temporalUser.getPreviousPassword()));
        } catch (EntryNotFoundException e) {
            facesMessages.addToControlFromResourceBundle("previousPassword", StatusMessage.Severity.ERROR,
                    "User.error.invalid.previousPassword");
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        currentUser.setPassword(Hash.instance().hash(temporalUser.getPassword()));
        userService.update(currentUser);
        addPasswordUpdatedMessage();
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    public User getTemporalUser() {
        return temporalUser;
    }

    public void setTemporalUser(User temporalUser) {
        this.temporalUser = temporalUser;
    }

    protected void addPasswordUpdatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "User.message.passwordChanged");
    }
}