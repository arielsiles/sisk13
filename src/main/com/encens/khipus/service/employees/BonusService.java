package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Bonus;

import javax.ejb.Local;

/**
 * @author
 * @version 2.26
 */
@Local
public interface BonusService extends GenericService {
    Bonus load(Bonus bonus) throws EntryNotFoundException;
}
