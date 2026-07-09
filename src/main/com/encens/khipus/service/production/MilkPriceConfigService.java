package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.MilkPriceConfig;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface MilkPriceConfigService extends GenericService {

    /**
     * Config de precios vigente para el periodo: estado ENABLE y cuyo rango
     * [fechaini, fechafin] cubre [startDate, endDate]. Devuelve null si no hay.
     */
    MilkPriceConfig findVigente(Date startDate, Date endDate);

    /**
     * Configs ENABLE cuyo rango se solapa con [startDate, endDate] (para validar
     * que no se dupliquen vigencias). excludeId ignora el propio registro al editar.
     */
    List<MilkPriceConfig> findOverlapping(Date startDate, Date endDate, Long excludeId);
}
