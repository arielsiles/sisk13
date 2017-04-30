package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.CycleType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.Sector;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 24-06-2010
 * Time: 12:29:08 PM
 */
@Local
public interface CycleService extends GenericService {
    Cycle findActiveCycle(Gestion gestion, CycleType cycleType);

    boolean isThereActiveCycleForSector(Sector sector);

    boolean isActiveInDataBase(Cycle cycle);

    void unActiveCycleForSector(Sector sector);
}
