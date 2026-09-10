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

    /**
     * @return los contratos vigentes en la fecha que ese dia no pertenecian a ningun grupo. Es
     *         la lista de la asignacion masiva: ya viene filtrada para que no se pueda elegir a
     *         alguien que provocaria un solapamiento
     */
    List<Contract> findContractsWithoutGroup(Date date);

    /**
     * @return los contratos vigentes en la fecha, esten o no en un grupo. Es la lista de la
     *         reorganizacion: aca SI aparece quien ya pertenece a otro grupo, porque de eso se
     *         trata
     */
    List<Contract> findContractsForMove(Date date);

    /** Las pertenencias base vigentes en una fecha, para saber de que grupo viene cada uno. */
    List<WorkGroupMembership> findBaseMembershipsAt(Date date);

    /**
     * Cierra una pertenencia en la fecha indicada, o la borra si el corte cae en su fecha de
     * inicio o antes -no hay historia que preservar-.
     */
    void closeMembership(WorkGroupMembership membership, Date date) throws Exception;

    /**
     * Mueve contratos a un grupo desde una fecha, cerrando su pertenencia base anterior el dia
     * previo. Es la reorganizacion: de treinta clics a uno.
     *
     * @return cuantos se movieron
     */
    int moveMembers(List<Contract> contracts, WorkGroup target, Date from);
}
