package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.JobCategory;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.ExchangeRate;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.Calendar;
import java.util.List;

/**
 * Service implementation of GestionPayrollScheduleService
 *
 * @author
 * @version 2.26
 */
@Stateless
@Name("gestionPayrollScheduleService")
@AutoCreate
public class GestionPayrollScheduleServiceBean extends GenericServiceBean implements GestionPayrollScheduleService {

    /**
     * This method saves all the changes made to gestionPayrollToEditList
     *
     * @param gestionPayrollToEditList   the list of instances to save or update
     * @param gestionPayrollToDeleteList the list of gestion payrolls that the user have specified to be deleted
     * @param activeBusinessUnit         the current business unit
     * @param jobCategory                the current jobCategory
     * @param gestion                    the current gestion    @throws com.encens.khipus.exception.ConcurrencyException
     *                                   in case the entity instance have been modified by other user
     * @throws com.encens.khipus.exception.EntryDuplicatedException
     *          in case there is a persistence expection
     * @throws com.encens.khipus.exception.ConcurrencyException
     *          in case the entity has been changed by another user
     */
    public void saveAll(List<GestionPayroll> gestionPayrollToEditList,
                        List<GestionPayroll> gestionPayrollToDeleteList,
                        BusinessUnit activeBusinessUnit, JobCategory jobCategory, Gestion gestion)
            throws ConcurrencyException, EntryDuplicatedException {
        for (GestionPayroll gestionPayroll : gestionPayrollToEditList) {
            /*in case already created*/
            if (null != gestionPayroll.getId()) {
                try {
                    if (gestionPayrollToDeleteList.contains(gestionPayroll)) {
                        getEntityManager().remove(gestionPayroll);
                    } else {
                        if (!getEntityManager().contains(gestionPayroll)) {
                            getEntityManager().merge(gestionPayroll);
                        }
                    }
                } catch (OptimisticLockException e) {
                    try {
                        super.findById(gestionPayroll.getClass(), gestionPayroll.getId(), true);
                    } catch (EntryNotFoundException e1) {
                        gestionPayroll = new GestionPayroll();
                        gestionPayroll.setExchangeRate(new ExchangeRate());
                        return;
                    }
                    throw new ConcurrencyException(e);
                } catch (PersistenceException ee) {
                    throw new EntryDuplicatedException(ee);
                }
            } else {
                /*if the properties are validated so create the instance*/
                if (null != gestionPayroll.getGestionName()) {
                    if (gestionPayroll.getExchangeRate().getDate() == null) {
                        gestionPayroll.getExchangeRate().setDate((Calendar.getInstance().getTime()));
                    }
                    if (gestionPayroll.getExchangeRate().getSale() == null) {
                        gestionPayroll.getExchangeRate().setSale(gestionPayroll.getExchangeRate().getRate());
                    }
                    if (gestionPayroll.getExchangeRate().getPurchase() == null) {
                        gestionPayroll.getExchangeRate().setPurchase(gestionPayroll.getExchangeRate().getRate());
                    }
                    gestionPayroll.setGestion(gestion);
                    gestionPayroll.setMonth(Month.getMonth(gestionPayrollToEditList.indexOf(gestionPayroll) + 1));
                    gestionPayroll.setBusinessUnit(activeBusinessUnit);
                    gestionPayroll.setJobCategory(jobCategory);
                    try {
                        getEntityManager().persist(gestionPayroll);
                    } catch (PersistenceException e) {
                        log.debug("Persistence error..", e);
                        throw new EntryDuplicatedException();
                    }
                }
            }
        }
        getEntityManager().flush();
    }

}
