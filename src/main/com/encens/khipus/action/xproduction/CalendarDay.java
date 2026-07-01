package com.encens.khipus.action.xproduction;

import com.encens.khipus.model.production.ProductionPlanState;
import com.encens.khipus.model.xproduction.XProductionPlan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Celda de calendario (un dia) usada por {@link XProductionCalendarAction} para
 * renderizar la vista mensual/semanal del Plan de Produccion.
 * <p/>
 * Es un POJO de presentacion (no es una entidad): agrega los valores ya
 * calculados de un dia (programado, ejecutado, avance, numero de ordenes y
 * estado) para evitar invocar logica de negocio durante el render.
 */
public class CalendarDay {

    private Date date;
    /** Plan de referencia del dia (para abrirlo); puede ser null si el dia esta vacio. */
    private XProductionPlan plan;
    /** True si la fecha pertenece al mes que se esta visualizando (vista mensual). */
    private boolean inMonth;
    /** True si la fecha es hoy. */
    private boolean today;
    /** True si el dia (su plan) coincide con el filtro de estado aplicado. */
    private boolean matchesFilter = true;

    private ProductionPlanState state;
    private BigDecimal programmed = BigDecimal.ZERO;
    private BigDecimal executed = BigDecimal.ZERO;
    private int orders;
    /** Siglas de producto (formulacion) del dia, distintas y separadas por coma (ej. "ULEX, BAR"). */
    private String siglas = "";
    /** Sufijo de clase CSS del estado de cada orden del dia (pen/eje/apr/fin/conta/anl),
     *  para pintar un dot de color por orden. */
    private List<String> orderStates = new ArrayList<String>();

    public boolean isHasPlan() {
        return plan != null && matchesFilter;
    }

    public int getDayNumber() {
        if (date == null) {
            return 0;
        }
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        return c.get(java.util.Calendar.DAY_OF_MONTH);
    }

    /** Porcentaje de avance ejecutado/programado, acotado a 0..100. */
    public int getProgress() {
        if (programmed == null || programmed.signum() <= 0 || executed == null) {
            return 0;
        }
        int pct = executed.multiply(BigDecimal.valueOf(100))
                .divide(programmed, 0, BigDecimal.ROUND_HALF_UP).intValue();
        if (pct < 0) {
            return 0;
        }
        return pct > 100 ? 100 : pct;
    }

    /** Clave i18n del estado del plan (o vacio si no hay plan). */
    public String getStateKey() {
        return state != null ? state.getResourceKey() : "";
    }

    /** Sufijo de clase CSS segun estado: pen/apr/fin/sus. */
    public String getStateCss() {
        return state != null ? state.name().toLowerCase() : "";
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public XProductionPlan getPlan() {
        return plan;
    }

    public void setPlan(XProductionPlan plan) {
        this.plan = plan;
    }

    public boolean isInMonth() {
        return inMonth;
    }

    public void setInMonth(boolean inMonth) {
        this.inMonth = inMonth;
    }

    public boolean isToday() {
        return today;
    }

    public void setToday(boolean today) {
        this.today = today;
    }

    public boolean isMatchesFilter() {
        return matchesFilter;
    }

    public void setMatchesFilter(boolean matchesFilter) {
        this.matchesFilter = matchesFilter;
    }

    public ProductionPlanState getState() {
        return state;
    }

    public void setState(ProductionPlanState state) {
        this.state = state;
    }

    public BigDecimal getProgrammed() {
        return programmed;
    }

    public void setProgrammed(BigDecimal programmed) {
        this.programmed = programmed;
    }

    public BigDecimal getExecuted() {
        return executed;
    }

    public void setExecuted(BigDecimal executed) {
        this.executed = executed;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public String getSiglas() {
        return siglas;
    }

    public void setSiglas(String siglas) {
        this.siglas = siglas;
    }

    public List<String> getOrderStates() {
        return orderStates;
    }

    public void setOrderStates(List<String> orderStates) {
        this.orderStates = orderStates;
    }
}
