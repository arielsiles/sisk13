package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Charge;

import javax.ejb.Local;

/**
 * ChargeService
 *
 * @author
 * @version 2.17
 */
@Local
public interface ChargeService extends GenericService {
    boolean validateName(Charge charge);
}
