package com.encens.khipus.service.finances;

import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CashBox;
import com.encens.khipus.model.finances.CashBoxRecord;
import com.encens.khipus.model.finances.UserCashBox;
import com.encens.khipus.model.finances.UserCashBoxState;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * UserCashBox service implementation class
 *
 * @author
 */

@Stateless
@Name("userCashBoxService")
@AutoCreate
public class UserCashBoxServiceBean implements UserCashBoxService {

    @In("#{entityManager}")
    private EntityManager em;

    @In(required = false)
    private User currentUser;

    public UserCashBox findByCashBox(CashBox cashBox) {
        try {
            Query query = em.createNamedQuery("UserCashBox.findByCashBox");
            query.setParameter("cashBox", cashBox);
            query.setParameter("state", UserCashBoxState.ACTIVE);
            return (UserCashBox) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public CashBox findByUser(User user) {
        try {
            Query query = em.createNamedQuery("UserCashBox.findByUser");
            query.setParameter("user", user);
            query.setParameter("state", UserCashBoxState.ACTIVE);
            return (CashBox) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public UserCashBox find(User user, CashBox cashBox) {
        try {
            Query query = em.createQuery("select u from UserCashBox u where u.user =:user and u.cashBox =:cashBox");
            query.setParameter("user", user);
            query.setParameter("cashBox", cashBox);
            return (UserCashBox) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(UserCashBox userCashBox, UserCashBoxState userCashBoxState) {
        userCashBox.setState(userCashBoxState);
        if (!em.contains(userCashBox)) {
            em.merge(userCashBox);
        }
    }

    public void createCashBoxRecord(CashBoxRecord cashBoxRecord) {
        em.persist(cashBoxRecord);
    }

    public boolean isCashierActive() {
        return findByUser(currentUser) != null;
    }

}
