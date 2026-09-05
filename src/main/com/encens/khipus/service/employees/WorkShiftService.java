package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.WorkShift;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Local
public interface WorkShiftService extends GenericService {

    List<WorkShift> findActiveList();

    Long countByName(String name, Long excludedId);
}
