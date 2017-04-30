package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.SalaryMovementTypeDuplicatedByDefaultException;
import com.encens.khipus.exception.employees.SalaryMovementTypeDuplicatedNameException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.MovementType;
import com.encens.khipus.model.employees.SalaryMovementType;

import javax.ejb.Local;

/**
 * @author
 * @version 3.4
 */
@Local
public interface SalaryMovementTypeService extends GenericService {
    SalaryMovementType load(SalaryMovementType salaryMovementType) throws EntryNotFoundException;

    void createSalaryMovementType(SalaryMovementType salaryMovementType) throws EntryDuplicatedException, SalaryMovementTypeDuplicatedNameException, SalaryMovementTypeDuplicatedByDefaultException;

    void updateSalaryMovementType(SalaryMovementType salaryMovementType) throws ConcurrencyException, EntryDuplicatedException, SalaryMovementTypeDuplicatedNameException, SalaryMovementTypeDuplicatedByDefaultException;

    SalaryMovementType findDefaultByMovementType(MovementType movementType);
}
