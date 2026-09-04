package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.contacts.Gender;
import com.encens.khipus.model.employees.WeeklyWorkload;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Local
public interface WeeklyWorkloadService extends GenericService {

    /**
     * Jornada vigente para un genero, o null si la empresa no configuro ninguna. Quien la
     * consume decide que hacer con el null: la jornada no se inventa por defecto porque
     * cada empresa reparte la semana a su manera.
     */
    WeeklyWorkload findByGender(Gender gender);

    List<WeeklyWorkload> findActiveList();

    /**
     * Cuantas jornadas activas hay ya para ese genero, sin contar la que se esta editando.
     * Solo puede haber una vigente por genero.
     */
    Long countActiveByGender(Gender gender, Long excludedId);
}
