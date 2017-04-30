package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.AFPRate;
import com.encens.khipus.model.employees.AFPRateType;

import javax.ejb.Local;

/**
 * @author
 * @version 2.26
 */
@Local
public interface AFPRateService extends GenericService {
    AFPRate findActive(AFPRateType afpRateType) throws EntryNotFoundException;
}
