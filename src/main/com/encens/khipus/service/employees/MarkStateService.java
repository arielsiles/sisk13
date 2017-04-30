package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.MarkState;
import com.encens.khipus.model.employees.RHMark;

import javax.ejb.Local;

/**
 * @author
 * @version 0.3
 */
@Local
public interface MarkStateService extends GenericService {
    MarkState findByMark(RHMark mark);
}
