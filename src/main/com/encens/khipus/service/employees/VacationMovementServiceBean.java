package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("vacationMovementService")
@AutoCreate
public class VacationMovementServiceBean extends GenericServiceBean implements VacationMovementService {

    @In(required = false)
    private User currentUser;

    public BigDecimal balanceAt(VacationPlanning vacationPlanning, Date date) {
        if (null == vacationPlanning || null == vacationPlanning.getId()) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = (BigDecimal) getEntityManager()
                .createNamedQuery("VacationMovement.balanceAtDate")
                .setParameter("vacationPlanning", vacationPlanning)
                .setParameter("date", date)
                .getSingleResult();
        return null == result ? BigDecimal.ZERO : result;
    }

    public BigDecimal balance(VacationPlanning vacationPlanning) {
        if (null == vacationPlanning || null == vacationPlanning.getId()) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = (BigDecimal) getEntityManager()
                .createNamedQuery("VacationMovement.balance")
                .setParameter("vacationPlanning", vacationPlanning)
                .getSingleResult();
        return null == result ? BigDecimal.ZERO : result;
    }

    @SuppressWarnings({"unchecked"})
    public List<VacationMovement> findByVacationPlanning(VacationPlanning vacationPlanning) {
        if (null == vacationPlanning || null == vacationPlanning.getId()) {
            return new ArrayList<VacationMovement>();
        }
        List<VacationMovement> resultList = getEntityManager()
                .createNamedQuery("VacationMovement.findByVacationPlanning")
                .setParameter("vacationPlanning", vacationPlanning)
                .getResultList();
        return null == resultList ? new ArrayList<VacationMovement>() : resultList;
    }

    /**
     * Declara el saldo con el que arranca el empleado y apaga el devengo anterior: el numero
     * declarado ya contiene todo lo que se gano hasta esa fecha, asi que devengarlo de nuevo lo
     * contaria dos veces. Las gestiones anteriores al corte y sus devengos se eliminan, y la
     * fecha queda en el plan para que no se vuelvan a crear al sincronizar.
     */
    @SuppressWarnings({"unchecked"})
    public void postOpeningBalance(VacationPlanning vacationPlanning, Date date, BigDecimal days,
                                   String description) {
        List<VacationMovement> previous = getEntityManager()
                .createNamedQuery("VacationMovement.findAccrualUpTo")
                .setParameter("vacationPlanning", vacationPlanning)
                .setParameter("type", VacationMovementType.ACCRUAL)
                .setParameter("date", date)
                .getResultList();
        for (VacationMovement movement : previous) {
            getEntityManager().remove(movement);
        }
        /* Se borran las gestiones cuyo devengo cae hasta el corte, comparando la fecha y no el
           anio: con corte 08/08/2026 y aniversario 01/09, el devengo de 2026 es posterior al
           corte y tiene que quedar. */
        for (VacationMovement movement : previous) {
            getEntityManager().createQuery(
                    "delete from VacationGestion g where g.vacationPlanning = :vacationPlanning"
                            + " and g.gestion = :gestion")
                    .setParameter("vacationPlanning", vacationPlanning)
                    .setParameter("gestion", movement.getGestion())
                    .executeUpdate();
        }

        vacationPlanning.setOpeningDate(date);
        getEntityManager().merge(vacationPlanning);

        VacationMovement movement = newMovement(vacationPlanning, VacationMovementType.OPENING,
                date, days);
        movement.setDescription(description);
        getEntityManager().persist(movement);
        getEntityManager().flush();
    }

    public boolean hasOpeningBalance(VacationPlanning vacationPlanning) {
        if (null == vacationPlanning || null == vacationPlanning.getId()) {
            return false;
        }
        Long count = (Long) getEntityManager()
                .createNamedQuery("VacationMovement.countByType")
                .setParameter("vacationPlanning", vacationPlanning)
                .setParameter("type", VacationMovementType.OPENING)
                .getSingleResult();
        return null != count && count > 0;
    }

    public BigDecimal openingBalance(VacationPlanning vacationPlanning) {
        if (null == vacationPlanning || null == vacationPlanning.getId()) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = (BigDecimal) getEntityManager()
                .createNamedQuery("VacationMovement.sumByType")
                .setParameter("vacationPlanning", vacationPlanning)
                .setParameter("type", VacationMovementType.OPENING)
                .getSingleResult();
        return null == result ? BigDecimal.ZERO : result;
    }

    public void postAccrual(VacationGestion vacationGestion) {
        VacationPlanning planning = vacationGestion.getVacationPlanning();
        Long already = (Long) getEntityManager()
                .createNamedQuery("VacationMovement.countAccrualByGestion")
                .setParameter("vacationPlanning", planning)
                .setParameter("gestion", vacationGestion.getGestion())
                .setParameter("type", VacationMovementType.ACCRUAL)
                .getSingleResult();
        if (null != already && already > 0) {
            return;
        }

        VacationMovement movement = newMovement(planning, VacationMovementType.ACCRUAL,
                accrualDate(planning, vacationGestion.getGestion()),
                vacationGestion.getVacationDays());
        movement.setGestion(vacationGestion.getGestion());
        movement.setDescription("Devengo gestion " + vacationGestion.getGestion());
        getEntityManager().persist(movement);
        getEntityManager().flush();
    }

    public void postTaking(Vacation vacation) {
        VacationPlanning planning = vacation.getVacationGestion().getVacationPlanning();
        VacationMovement movement = newMovement(planning, VacationMovementType.TAKING,
                vacation.getInitDate(), vacation.getTotalDays().negate());
        movement.setVacation(vacation);
        movement.setGestion(vacation.getVacationGestion().getGestion());
        movement.setDescription(vacation.getDescription());
        getEntityManager().persist(movement);
        getEntityManager().flush();
    }

    public void revertTaking(Vacation vacation) {
        BigDecimal taken = sumMovementsOf(vacation);
        if (BigDecimal.ZERO.compareTo(taken) == 0) {
            return;
        }
        VacationPlanning planning = vacation.getVacationGestion().getVacationPlanning();
        VacationMovement movement = newMovement(planning, VacationMovementType.ADJUSTMENT,
                new Date(), taken.negate());
        movement.setVacation(vacation);
        movement.setGestion(vacation.getVacationGestion().getGestion());
        movement.setDescription("Anulacion de: " + vacation.getDescription());
        getEntityManager().persist(movement);
        getEntityManager().flush();
    }

    /**
     * Lo que esta vacacion movio hasta ahora. Se suma en lugar de tomar el total de dias
     * porque la vacacion pudo anularse y reactivarse, y lo que hay que devolver es el neto.
     */
    @SuppressWarnings({"unchecked"})
    private BigDecimal sumMovementsOf(Vacation vacation) {
        List<VacationMovement> movements = getEntityManager()
                .createNamedQuery("VacationMovement.findByVacation")
                .setParameter("vacation", vacation)
                .getResultList();
        BigDecimal total = BigDecimal.ZERO;
        for (VacationMovement movement : movements) {
            total = total.add(movement.getDays());
        }
        return total;
    }

    private VacationMovement newMovement(VacationPlanning planning, VacationMovementType type,
                                         Date date, BigDecimal days) {
        VacationMovement movement = new VacationMovement();
        movement.setVacationPlanning(planning);
        movement.setType(type);
        movement.setDate(date);
        movement.setDays(days);
        movement.setUser(currentUser);
        movement.setCreationDate(new Date());
        return movement;
    }

    /**
     * Los dias se ganan al CERRAR el anio de servicio, no al abrirlo. La gestion 2025 es el anio
     * que va del aniversario de 2025 al de 2026, asi que su devengo entra con el aniversario de
     * 2026. Fecharlo al inicio adelantaria dias que todavia no se ganaron.
     */
    public static Date accrualDate(VacationPlanning planning, Integer gestion) {
        Calendar accrual = Calendar.getInstance();
        accrual.setTime(planning.getInitDate());
        accrual.set(Calendar.YEAR, gestion + 1);
        return accrual.getTime();
    }
}
