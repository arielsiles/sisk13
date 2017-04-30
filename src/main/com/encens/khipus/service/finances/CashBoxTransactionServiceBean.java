package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CashBox;
import com.encens.khipus.model.finances.CashBoxRecord;
import com.encens.khipus.model.finances.CashBoxState;
import com.encens.khipus.model.finances.CashBoxTransaction;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * CashBoxTransaction service implementation class
 *
 * @author
 */

@Stateless
@Name("cashBoxTransactionService")
@AutoCreate
public class CashBoxTransactionServiceBean implements CashBoxTransactionService {

    @In("#{entityManager}")
    private EntityManager em;

    @TransactionAttribute(REQUIRES_NEW)
    public void openCashBox(CashBox cashBox, User currentUser) throws EntryDuplicatedException, ConcurrencyException {
        try {
            if (cashBox != null && findByCashBox(cashBox) == null) {
                CashBoxTransaction cashBoxTransaction = new CashBoxTransaction();
                cashBoxTransaction.setOpeningDate(new Date());
                cashBoxTransaction.setCashBox(cashBox);
                cashBoxTransaction.setCashBoxUser(currentUser);
                cashBoxTransaction.setTotalAmount(BigDecimal.ZERO);
                CashBoxRecord cashBoxRecord = new CashBoxRecord(cashBox);
                cashBox.setState(CashBoxState.OPEN);
                cashBox.setStateDate(new Date());
                if (!em.contains(cashBox)) {
                    em.merge(cashBox);
                }
                em.persist(cashBoxRecord);
                em.persist(cashBoxTransaction);
                em.flush();
            }
        } catch (EntityExistsException e) {
            throw new EntryDuplicatedException();
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void closeCashBox(CashBox cashBox) throws ConcurrencyException {
        try {
            if (cashBox != null) {
                CashBoxTransaction cashBoxTransaction = findByCashBox(cashBox);
                if (cashBoxTransaction != null) {
                    cashBoxTransaction.setClosingDate(new Date());
                    CashBoxRecord cashBoxRecord = new CashBoxRecord(cashBox);
                    cashBox.setState(CashBoxState.CLOSED);
                    cashBox.setStateDate(new Date());
                    if (!em.contains(cashBox)) {
                        em.merge(cashBoxTransaction);
                        em.merge(cashBox);
                    }
                    em.persist(cashBoxRecord);
                    em.flush();
                }
            }
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException(e);
        }
    }

    public CashBoxTransaction findByCashBox(CashBox cashBox) {
        try {
            return (CashBoxTransaction) em.createNamedQuery("CashBoxTransaction.findByCashBox")
                    .setParameter("cashBox", cashBox).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public CashBoxTransaction findByCashBoxUser(User user) {
        try {
            return (CashBoxTransaction) em.createNamedQuery("CashBoxTransaction.findByCashBoxUser")
                    .setParameter("user", user).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean cashBoxOpen(CashBox cashBox) {
        return CashBoxState.OPEN.equals(cashBox.getState());
    }

    public boolean cashBoxClosedToday(CashBox cashBox) {
        try {
            Date lastClosingDate = (Date) em.createNamedQuery("CashBoxTransaction.findMaxClosingDate")
                    .setParameter("cashBox", cashBox).getSingleResult();
            return isSameDay(lastClosingDate, new Date()) && !cashBoxOpen(cashBox);
        } catch (NoResultException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean isSameDay(Date d1, Date d2) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(d1);
        int year1 = calendar.get(GregorianCalendar.YEAR);
        int day_of_year1 = calendar.get(GregorianCalendar.DAY_OF_YEAR);
        calendar.setTime(d2);
        int year2 = calendar.get(GregorianCalendar.YEAR);
        int day_of_year2 = calendar.get(GregorianCalendar.DAY_OF_YEAR);
        return (year1 == year2 && day_of_year1 == day_of_year2);
    }
}
