package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.ProducerCollectionRestriction;
import com.encens.khipus.model.production.RawMaterialProducer;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Local
public interface ProducerCollectionRestrictionService extends GenericService {

    /**
     * Pre-carga (batch) las restricciones vigentes para el periodo, indexadas por
     * id de productor. Vigente = estado ENABLE y su rango [fechaini, fechafin]
     * cubre el periodo de la planilla. Patron @Claude OPT (evita N+1 en generacion).
     */
    Map<Long, ProducerCollectionRestriction> preloadRestrictions(Date startDate, Date endDate);

    /**
     * Restricciones ENABLE del mismo productor cuyo rango se solapa con
     * [startDate, endDate] (para validar que no se dupliquen vigencias).
     * excludeId permite ignorar el propio registro al editar.
     */
    List<ProducerCollectionRestriction> findOverlapping(RawMaterialProducer producer, Date startDate, Date endDate, Long excludeId);
}
