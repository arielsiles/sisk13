package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.HoraryBandState;

import javax.ejb.Local;
import java.util.Date;

/**
 * @author
 * @version 3.0
 */
@Local
public interface HoraryBandStateService extends GenericService {
    HoraryBandState findByDateAndHoraryBand(Date date, Long horaryBandId);
}
