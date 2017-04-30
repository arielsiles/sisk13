package com.encens.khipus.action.finances;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.service.finances.UserCashBoxService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.util.Date;

/**
 * Actions for Cash box
 *
 * @author
 */

@Name("cashBoxAction")
@Scope(ScopeType.CONVERSATION)
public class CashBoxAction extends GenericAction<CashBox> {

    private User user;
    private UserCashBox oldUserCashBox;
    private UserCashBox userCashBox;
    private boolean isUserAssigned;
    private CashBoxRecord cashBoxRecord;

    @In(required = false)
    private User currentUser;

    @In
    private UserCashBoxService userCashBoxService;

    @Factory(value = "cashBox", scope = ScopeType.STATELESS)
    public CashBox initCashBox() {
        if (isManaged() && userCashBox == null) {
            userCashBox = userCashBoxService.findByCashBox(getInstance());
            if (userCashBox == null) {
                userCashBox = new UserCashBox();
            } else {
                isUserAssigned = true;
            }
        }
        if (cashBoxRecord == null) {
            cashBoxRecord = new CashBoxRecord(getInstance());
        }

        return getInstance();
    }

    @Factory("cashBoxState")
    public CashBoxState[] getCashBoxState() {
        return CashBoxState.values();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "description";
    }

    @Override
    @End
    public String create() {
        try {
            getInstance().setUser(currentUser);
            genericService.create(getInstance());

            addCreatedMessage();

            //upate cashBoxRecord instance, because at creation time it was created with empty values.
            cashBoxRecord = new CashBoxRecord(getInstance());
            return super.select(getInstance());
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
    }

    @Override
    @End
    public String update() {
        getInstance().setUser(currentUser);
        userCashBoxService.createCashBoxRecord(cashBoxRecord);
        return super.update();
    }

    public void changeState(CashBoxState state) {
        try {
            if (state.equals(CashBoxState.OPEN)) {
                userCashBox.setOpeningDate(new Date());
            } else if (state.equals(CashBoxState.CLOSED)) {
                userCashBox.setClosingDate(new Date());
            }
            getInstance().setStateDate(new Date());
        } catch (NullPointerException e) {
            //
        }
    }

    public Date getOpeningDate() {
        return isUserAssigned ? userCashBox.getOpeningDate() : null;
    }

    public Date getClosingDate() {
        return isUserAssigned ? userCashBox.getClosingDate() : null;
    }

    public String selectUserCashBox() {
        userCashBox = userCashBoxService.findByCashBox(getInstance());
        if (userCashBox == null) {
            userCashBox = new UserCashBox();
        }
        return com.encens.khipus.framework.action.Outcome.SUCCESS;
    }

    @End
    public String updateUserCashBox() {
        if (null == user) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "UserCashBox.error.noAssignedUser", getInstance().getDescription());
            return Outcome.REDISPLAY;
        }

        //create history register for current cashBox
        userCashBoxService.createCashBoxRecord(cashBoxRecord);

        //the selected user and user related to actual userCashBox are equal
        if (user.equals(userCashBox.getUser())) {
            return super.update();
        }

        //the selected user has a relation with another userCashBox
        if (userCashBoxService.findByUser(user) != null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "UserCashBox.error.assignedUser", user.getEmployee().getFullName());
            return Outcome.REDISPLAY;
        }

        if (userCashBox.getUser() == null || oldUserCashBox == null) {
            UserCashBox newUserCashBox = new UserCashBox(user, getInstance());
            newUserCashBox.setState(UserCashBoxState.ACTIVE);
            getInstance().getUserCashBoxList().add(newUserCashBox);
        }

        getInstance().setStateDate(new Date());
        return super.update();
    }

    public void setUser(User user) {
        this.user = user;
        try {
            getInstance().setBusinessUnit(user.getEmployee().getBusinessUnit());
            if (!user.getId().equals(userCashBox.getUser().getId())) {
                userCashBoxService.update(userCashBox, UserCashBoxState.INACTIVE);
                oldUserCashBox = userCashBoxService.find(user, userCashBox.getCashBox());
                if (oldUserCashBox != null) {
                    userCashBoxService.update(oldUserCashBox, UserCashBoxState.ACTIVE);
                }
            } else {
                if (oldUserCashBox != null) {
                    userCashBoxService.update(oldUserCashBox, UserCashBoxState.INACTIVE);
                }
                userCashBoxService.update(userCashBox, UserCashBoxState.ACTIVE);
            }
        } catch (NullPointerException e) {

        }
    }

    public User getUser() {
        if (userCashBox.getUser() != null && this.user == null) {
            this.user = userCashBox.getUser();
        }
        return user;
    }

}
