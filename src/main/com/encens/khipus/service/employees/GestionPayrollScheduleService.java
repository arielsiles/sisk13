package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.JobCategory;

import javax.ejb.Local;
import java.util.List;

/**
 * Encens S.R.L.
 * This class implements the GestionPayrollSchedule service local interface
 *
 * @author
 * @version 2.26
 */
@Local
public interface GestionPayrollScheduleService extends GenericService {

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
    void saveAll(List<GestionPayroll> gestionPayrollToEditList, List<GestionPayroll> gestionPayrollToDeleteList, BusinessUnit activeBusinessUnit,
                 JobCategory jobCategory, Gestion gestion)
            throws ConcurrencyException, EntryDuplicatedException;
}
