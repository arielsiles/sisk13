package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayroll;

import javax.ejb.Local;

/**
 * @author
 * @version 3.4
 */
@Local
public interface PayrollGenerationCycleMergeService extends GenericService {
    void merge(GeneratedPayroll generatedPayroll) throws ConcurrencyException, EntryDuplicatedException;
}
