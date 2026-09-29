package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.ContractWorkShift;
import com.encens.khipus.model.employees.WorkShift;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
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
@Name("contractScheduleService")
@AutoCreate
public class ContractScheduleServiceBean extends GenericServiceBean implements ContractScheduleService {

    @In
    private ScheduleLockService scheduleLockService;

    @SuppressWarnings({"unchecked"})
    public List<ContractWorkShift> findCurrent(Contract contract) {
        if (null == contract || null == contract.getId()) {
            return new ArrayList<ContractWorkShift>();
        }
        List<ContractWorkShift> resultList = getEntityManager()
                .createNamedQuery("ContractWorkShift.findCurrentByContract")
                .setParameter("contract", contract)
                .getResultList();
        return null == resultList ? new ArrayList<ContractWorkShift>() : resultList;
    }

    public void setShift(Contract contract, int dayOfWeek, WorkShift workShift, Date from)
            throws Exception {
        Date start = startOfDay(from);
        /* La vigencia nueva no puede empezar dentro de un periodo ya pagado. */
        scheduleLockService.checkOpen(start);
        ContractWorkShift current = currentOf(contract, dayOfWeek);
        if (null != current) {
            if (start.equals(current.getStartDate())) {
                /* Corregir el mismo dia en que se creo no es un cambio de horario: es
                   arreglar lo que se acaba de cargar, y no deja una vigencia de cero dias. */
                current.setWorkShift(workShift);
                update(current);
                return;
            }
            current.setEndDate(dayBefore(start));
            update(current);
        }
        ContractWorkShift shift = new ContractWorkShift();
        shift.setContract(contract);
        shift.setDayOfWeek(dayOfWeek);
        shift.setWorkShift(workShift);
        shift.setStartDate(start);
        create(shift);
    }

    /**
     * Cierra el horario hacia adelante. Lo anterior al corte se sigue evaluando con el horario
     * que regia entonces.
     * <p/>
     * Si el corte cae en la fecha en que el horario empezo -o antes-, la fila se BORRA en lugar
     * de cerrarse: cerrarla dejaria una vigencia que termina antes de empezar, que no sirve para
     * nada y despues no hay forma de sacar. Cerrar es para historia real; deshacer una carga
     * recien hecha es otra cosa.
     */
    public void close(Contract contract, Date from) throws Exception {
        Date cut = startOfDay(from);
        scheduleLockService.checkOpen(cut);
        Date end = dayBefore(cut);
        for (ContractWorkShift shift : findCurrent(contract)) {
            if (!shift.getStartDate().before(cut)) {
                delete(shift);
            } else {
                shift.setEndDate(end);
                update(shift);
            }
        }
    }

    public Long countByWorkShift(WorkShift workShift) {
        if (null == workShift || null == workShift.getId()) {
            return 0L;
        }
        return (Long) getEntityManager().createNamedQuery("ContractWorkShift.countByWorkShift")
                .setParameter("workShift", workShift).getSingleResult();
    }

    private ContractWorkShift currentOf(Contract contract, int dayOfWeek) {
        for (ContractWorkShift shift : findCurrent(contract)) {
            if (shift.getDayOfWeek().equals(dayOfWeek)) {
                return shift;
            }
        }
        return null;
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
