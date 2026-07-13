package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.SalaryMovementProducer;
import com.encens.khipus.model.production.SalaryMovementProducerState;
import com.encens.khipus.model.production.TypeMovementProducer;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/29/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("salaryMovementProducerDataModel")
@Scope(ScopeType.PAGE)
public class SalaryMovementProducerDataModel extends QueryDataModel<Long, SalaryMovementProducer> {

    private Date startDate;
    private Date endDate;
    private String firstName;
    private String lastName;
    private String maidenName;
    private SalaryMovementProducerState state;

    /** Selectores de periodo (atajo para calcular startDate/endDate por quincena). */
    private Integer year;
    private Month monthEnum;
    private Integer quincena;

    /** Totalizadores de lo filtrado, calculados de forma perezosa tras cada busqueda. */
    private Double totalAmount;
    private Double totalSaldo;
    private boolean totalsDirty = true;

    private static final String[] RESTRICTIONS = {
            "salaryMovementProducer.date >= #{salaryMovementProducerDataModel.startDate}",
            "salaryMovementProducer.date <= #{salaryMovementProducerDataModel.endDate}",
            "salaryMovementProducer.date >= #{salaryMovementProducerDataModel.periodStartDate}",
            "salaryMovementProducer.date <= #{salaryMovementProducerDataModel.periodEndDate}",
            "salaryMovementProducer.typeMovementProducer = #{salaryMovementProducerDataModel.criteria.typeMovementProducer}",
            "salaryMovementProducer.state = #{salaryMovementProducerDataModel.state}",
            "upper(rawMaterialProducer.firstName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.firstName})), '%')",
            "upper(rawMaterialProducer.lastName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.lastName})), '%')",
            "upper(rawMaterialProducer.maidenName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.maidenName})), '%')"
    };

    /*private static final String[] RESTRICTIONS = {
            "salaryMovementProducer.date >= #{salaryMovementProducerDataModel.startDate}",
            "salaryMovementProducer.date <= #{salaryMovementProducerDataModel.endDate}",
            *//*"salaryMovementProducer.state = #{salaryMovementProducerDataModel.privateCriteria.state}",*//*
            "upper(rawMaterialProducer.firstName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.firstName})), '%')",
            "upper(rawMaterialProducer.lastName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.lastName})), '%')",
            "upper(rawMaterialProducer.maidenName) like concat(concat('%',upper(#{salaryMovementProducerDataModel.criteria.rawMaterialProducer.maidenName})), '%')"
    };*/

    @Override
    public String getEjbql() {
        String query = " select salaryMovementProducer " +
                       " from SalaryMovementProducer salaryMovementProducer " +
                       " left join fetch salaryMovementProducer.rawMaterialProducer rawMaterialProducer";
        return query;
    }

    @Create
    public void defaultSort() {
        sortProperty = "salaryMovementProducer.date";
        this.sortAsc = false;
        applyDefaultPeriod();
    }

    /** Defaults del filtro de periodo: anio actual, mes actual, primera quincena. */
    private void applyDefaultPeriod() {
        Calendar cal = Calendar.getInstance();
        this.year = cal.get(Calendar.YEAR);
        this.monthEnum = Month.getMonthByCalendarIndex(cal.get(Calendar.MONTH));
        this.quincena = 1;
    }

    /**
     * Inicio del periodo como filtro independiente de las fechas. Soporta seleccion PARCIAL:
     *   - anio + mes + quincena -> primer dia de la quincena (1 o 16)
     *   - anio + mes            -> primer dia del mes
     *   - solo anio             -> 1 de enero
     * Retorna null solo si no hay anio (sin anio no se puede acotar un rango).
     */
    public Date getPeriodStartDate() {
        if (year == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.clear();
        if (monthEnum == null) {
            cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        } else if (quincena == null) {
            cal.set(year, monthEnum.getValue(), 1, 0, 0, 0);
        } else {
            cal.set(year, monthEnum.getValue(), (quincena == 2 ? 16 : 1), 0, 0, 0);
        }
        return cal.getTime();
    }

    /**
     * Fin del periodo como filtro independiente de las fechas. Soporta seleccion PARCIAL:
     *   - anio + mes + quincena -> ultimo dia de la quincena (15 o fin de mes)
     *   - anio + mes            -> ultimo dia del mes
     *   - solo anio             -> 31 de diciembre
     */
    public Date getPeriodEndDate() {
        if (year == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.clear();
        if (monthEnum == null) {
            cal.set(year, Calendar.DECEMBER, 31, 0, 0, 0);
        } else if (quincena == null) {
            cal.set(year, monthEnum.getValue(), 1, 0, 0, 0);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else if (quincena == 2) {
            cal.set(year, monthEnum.getValue(), 1, 0, 0, 0);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else {
            cal.set(year, monthEnum.getValue(), 15, 0, 0, 0);
        }
        return cal.getTime();
    }

    /**
     * Rango efectivo (interseccion) entre las fechas manuales y el periodo,
     * equivalente al AND de ambas restricciones. Lo usa la exportacion a Excel.
     */
    public Date getEffectiveStartDate() {
        Date periodStart = getPeriodStartDate();
        if (startDate == null) {
            return periodStart;
        }
        if (periodStart == null) {
            return startDate;
        }
        return startDate.after(periodStart) ? startDate : periodStart;
    }

    public Date getEffectiveEndDate() {
        Date periodEnd = getPeriodEndDate();
        if (endDate == null) {
            return periodEnd;
        }
        if (periodEnd == null) {
            return endDate;
        }
        return endDate.before(periodEnd) ? endDate : periodEnd;
    }

    /** Limpia todos los filtros y vuelve al periodo por defecto. */
    public void clearFilters() {
        this.firstName = null;
        this.lastName = null;
        this.maidenName = null;
        this.state = null;
        this.startDate = null;
        this.endDate = null;
        if (getCriteria() != null) {
            getCriteria().setTypeMovementProducer(null);
        }
        // Limpiar TODO: tambien el periodo (anio/mes/quincena). Antes se re-seleccionaba el
        // periodo por defecto, por eso quedaban marcados.
        this.year = null;
        this.monthEnum = null;
        this.quincena = null;
        // Refresca lista/paginador y luego deja los TOTALES en blanco: no se recalculan hasta
        // la proxima busqueda explicita (search() marca totalsDirty=true, por eso se limpian
        // despues de llamarlo).
        search();
        this.totalAmount = null;
        this.totalSaldo = null;
        this.totalsDirty = false;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public SalaryMovementProducerState getState() {
        return state;
    }

    public void setState(SalaryMovementProducerState state) {
        this.state = state;
    }

    /** Valores para el combo del filtro por estado (PENDIENTE / PAGADO). */
    public SalaryMovementProducerState[] getStates() {
        return SalaryMovementProducerState.values();
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Month getMonthEnum() {
        return monthEnum;
    }

    public void setMonthEnum(Month monthEnum) {
        this.monthEnum = monthEnum;
    }

    public Integer getQuincena() {
        return quincena;
    }

    public void setQuincena(Integer quincena) {
        this.quincena = quincena;
    }

    /** Meses para el combo del filtro de periodo. */
    public Month[] getMonthList() {
        return Month.values();
    }

    /** Lista de anios para el combo (anio actual y los 5 previos). */
    public List<Integer> getYearList() {
        List<Integer> years = new ArrayList<Integer>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= currentYear - 5; y--) {
            years.add(y);
        }
        return years;
    }

    @Override
    public void search() {
        super.search();
        // Reinicia la cache de conteo/estado para que el paginador refleje el nuevo filtro.
        update();
        this.totalsDirty = true;
    }

    public Double getTotalAmount() {
        computeTotalsIfNeeded();
        return totalAmount;
    }

    public Double getTotalSaldo() {
        computeTotalsIfNeeded();
        return totalSaldo;
    }

    private void computeTotalsIfNeeded() {
        if (!totalsDirty) {
            return;
        }
        computeTotals();
        totalsDirty = false;
    }

    /** Suma Monto y Saldo aplicando los mismos filtros que la lista. */
    private void computeTotals() {
        TypeMovementProducer typeMovementProducer =
                getCriteria() != null ? getCriteria().getTypeMovementProducer() : null;
        Date periodStart = getPeriodStartDate();
        Date periodEnd = getPeriodEndDate();

        StringBuilder jpql = new StringBuilder();
        jpql.append("select coalesce(sum(s.valor), 0), coalesce(sum(s.saldo), 0)");
        jpql.append(" from SalaryMovementProducer s");
        jpql.append(" left join s.rawMaterialProducer rawMaterialProducer");
        jpql.append(" where 1 = 1");
        if (startDate != null) {
            jpql.append(" and s.date >= :startDate");
        }
        if (endDate != null) {
            jpql.append(" and s.date <= :endDate");
        }
        if (periodStart != null) {
            jpql.append(" and s.date >= :periodStart");
        }
        if (periodEnd != null) {
            jpql.append(" and s.date <= :periodEnd");
        }
        if (typeMovementProducer != null) {
            jpql.append(" and s.typeMovementProducer = :typeMovementProducer");
        }
        if (state != null) {
            jpql.append(" and s.state = :state");
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            jpql.append(" and upper(rawMaterialProducer.firstName) like :firstName");
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            jpql.append(" and upper(rawMaterialProducer.lastName) like :lastName");
        }
        if (maidenName != null && !maidenName.trim().isEmpty()) {
            jpql.append(" and upper(rawMaterialProducer.maidenName) like :maidenName");
        }

        Query query = getEntityManager().createQuery(jpql.toString());
        if (startDate != null) {
            query.setParameter("startDate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate, TemporalType.DATE);
        }
        if (periodStart != null) {
            query.setParameter("periodStart", periodStart, TemporalType.DATE);
        }
        if (periodEnd != null) {
            query.setParameter("periodEnd", periodEnd, TemporalType.DATE);
        }
        if (typeMovementProducer != null) {
            query.setParameter("typeMovementProducer", typeMovementProducer);
        }
        if (state != null) {
            query.setParameter("state", state);
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            query.setParameter("firstName", "%" + firstName.trim().toUpperCase() + "%");
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            query.setParameter("lastName", "%" + lastName.trim().toUpperCase() + "%");
        }
        if (maidenName != null && !maidenName.trim().isEmpty()) {
            query.setParameter("maidenName", "%" + maidenName.trim().toUpperCase() + "%");
        }

        Object[] result = (Object[]) query.getSingleResult();
        this.totalAmount = result[0] != null ? ((Number) result[0]).doubleValue() : 0.0;
        this.totalSaldo = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
    }
}
