package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.MarkState;
import com.encens.khipus.model.employees.MarkStateHoraryBandState;

import javax.ejb.Local;
import javax.persistence.EntityManager;

/**
 * @author
 * @version 3.0
 */
@Local
public interface MarkStateHoraryBandStateService extends GenericService {
    Long countByMarkStateAndNotMarkStateHoraryBandState(MarkState markState, MarkStateHoraryBandState markStateHoraryBandState);

    MarkStateHoraryBandState findByMarkStateAndHoraryBandState(MarkState markState, Long horaryBandStateId, EntityManager entityManager);
}
