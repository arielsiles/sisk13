package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.WorkGroup;
import com.encens.khipus.model.employees.WorkGroupMembership;
import com.encens.khipus.model.employees.WorkGroupMembershipType;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("workGroupService")
@AutoCreate
public class WorkGroupServiceBean extends GenericServiceBean implements WorkGroupService {

    @SuppressWarnings({"unchecked"})
    public List<WorkGroup> findActiveList() {
        List<WorkGroup> resultList = getEntityManager()
                .createNamedQuery("WorkGroup.findActive")
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        return null == resultList ? new ArrayList<WorkGroup>() : resultList;
    }

    public Long countByName(String name, Long excludedId) {
        if (null == excludedId) {
            return (Long) getEntityManager().createNamedQuery("WorkGroup.countByName")
                    .setParameter("name", name).getSingleResult();
        }
        return (Long) getEntityManager().createNamedQuery("WorkGroup.countByNameButThis")
                .setParameter("name", name)
                .setParameter("id", excludedId).getSingleResult();
    }

    /**
     * El prestamo gana sobre la pertenencia base: si ese dia la persona estaba prestada a otro
     * grupo, es contra el cronograma de ESE grupo que hay que evaluarla.
     */
    public WorkGroupMembership findMembership(Contract contract, Date date) {
        if (null == contract || null == date) {
            return null;
        }
        WorkGroupMembership loan = findMembership(contract, date, WorkGroupMembershipType.LOAN);
        return null != loan ? loan : findMembership(contract, date, WorkGroupMembershipType.BASE);
    }

    @SuppressWarnings({"unchecked"})
    private WorkGroupMembership findMembership(Contract contract, Date date,
                                               WorkGroupMembershipType type) {
        List<WorkGroupMembership> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findByContractAndDate")
                .setParameter("contract", contract)
                .setParameter("date", date)
                .setParameter("type", type)
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<WorkGroupMembership> findMemberships(WorkGroup workGroup) {
        if (null == workGroup || null == workGroup.getId()) {
            return new ArrayList<WorkGroupMembership>();
        }
        List<WorkGroupMembership> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findByGroup")
                .setParameter("workGroup", workGroup)
                .getResultList();
        return null == resultList ? new ArrayList<WorkGroupMembership>() : resultList;
    }

    @SuppressWarnings({"unchecked"})
    public WorkGroupMembership findOverlapping(WorkGroupMembership membership) {
        List<WorkGroupMembership> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findOverlapping")
                .setParameter("contract", membership.getContract())
                .setParameter("id", null == membership.getId() ? -1L : membership.getId())
                .setParameter("startDate", membership.getStartDate())
                .setParameter("endDate", membership.getEndDate())
                .setParameter("type", membership.getType())
                .getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<Contract> findContractsWithoutGroup(Date date) {
        List<Contract> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findContractsWithoutGroup")
                .setParameter("active", Boolean.TRUE)
                .setParameter("date", date)
                .getResultList();
        return null == resultList ? new ArrayList<Contract>() : resultList;
    }

    /**
     * Contratos vigentes en la fecha, esten o no en un grupo. Es la lista de la reorganizacion:
     * a diferencia del alta, aca SI aparece quien ya pertenece a otro grupo, porque de eso se
     * trata: moverlo. Su grupo actual se muestra al lado para que la decision sea informada.
     */
    @SuppressWarnings({"unchecked"})
    public List<Contract> findContractsForMove(Date date) {
        List<Contract> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findContractsForMove")
                .setParameter("active", Boolean.TRUE)
                .setParameter("date", date)
                .getResultList();
        return null == resultList ? new ArrayList<Contract>() : resultList;
    }

    /**
     * Cierra una pertenencia en la fecha indicada. Si el corte cae en su fecha de inicio o
     * antes, la BORRA: cerrarla dejaria una vigencia que termina antes de empezar, que estorba
     * y no se puede sacar. Cerrar es para historia real; deshacer un alta recien hecha no.
     */
    public void closeMembership(WorkGroupMembership membership, Date date) throws Exception {
        Date cut = startOfDay(date);
        if (!membership.getStartDate().before(cut)) {
            delete(membership);
            return;
        }
        membership.setEndDate(dayBefore(cut));
        update(membership);
    }

    /**
     * Mueve contratos a un grupo desde una fecha, cerrando la pertenencia base anterior el dia
     * previo. Cada uno se procesa por separado y se sigue con el resto si alguno falla: que una
     * fila con un problema puntual no tire abajo las otras cuarenta.
     *
     * @return cuantos se movieron
     */
    public int moveMembers(List<Contract> contracts, WorkGroup target, Date from) {
        int moved = 0;
        Date start = startOfDay(from);
        for (Contract contract : contracts) {
            try {
                WorkGroupMembership current = findMembership(contract, start, WorkGroupMembershipType.BASE);
                if (null != current) {
                    if (current.getWorkGroup().getId().equals(target.getId())) {
                        continue; /* ya esta en el grupo destino */
                    }
                    closeMembership(current, start);
                }
                WorkGroupMembership membership = new WorkGroupMembership();
                membership.setWorkGroup(target);
                membership.setContract(contract);
                membership.setStartDate(start);
                membership.setType(WorkGroupMembershipType.BASE);
                create(membership);
                moved++;
            } catch (Exception e) {
                log.error("No se pudo mover el contrato al grupo", e);
            }
        }
        return moved;
    }

    @SuppressWarnings({"unchecked"})
    public List<WorkGroupMembership> findBaseMembershipsAt(Date date) {
        List<WorkGroupMembership> resultList = getEntityManager()
                .createNamedQuery("WorkGroupMembership.findBaseAtDate")
                .setParameter("type", WorkGroupMembershipType.BASE)
                .setParameter("date", date)
                .getResultList();
        return null == resultList ? new ArrayList<WorkGroupMembership>() : resultList;
    }

    private static Date dayBefore(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime();
    }

    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
