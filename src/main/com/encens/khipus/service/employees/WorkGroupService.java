package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.model.finances.Contract;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Local
public interface WorkGroupService extends GenericService {

    List<WorkGroup> findActiveList();

    Long countByName(String name, Long excludedId);

    /**
     * El grupo al que pertenecia el contrato en una fecha, o null si en esa fecha no estaba en
     * ninguno. Se busca por fecha y no por "el grupo actual": mover a alguien de grupo no puede
     * reescribir como se evaluaron sus dias anteriores.
     */
    WorkGroupMembership findMembership(Contract contract, Date date);

    List<WorkGroupMembership> findMemberships(WorkGroup workGroup);

    /**
     * @return la pertenencia que se pisa con el periodo dado, o null si no hay ninguna. Una
     *         persona no puede estar en dos grupos el mismo dia.
     */
    WorkGroupMembership findOverlapping(WorkGroupMembership membership);
}
