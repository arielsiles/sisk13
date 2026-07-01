package com.encens.khipus.action.xproduction;

import com.encens.khipus.model.production.ProductionPlanState;
import com.encens.khipus.model.production.ProductionState;
import com.encens.khipus.model.xproduction.XProduction;
import com.encens.khipus.model.xproduction.XProductionPlan;
import com.encens.khipus.model.xproduction.XProductionProduct;
import com.encens.khipus.service.xproduction.XProductionPlanService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Componente de presentacion del Plan de Produccion en formato calendario
 * (estilo Odoo). Construye una grilla mensual (6 semanas) o semanal (1 semana),
 * comenzando en lunes, y agrega por dia: estado del plan, cantidad programada,
 * cantidad ejecutada (ordenes en ejecucion/aprobadas/finalizadas/contabilizadas)
 * y numero de ordenes.
 * <p/>
 * Es de scope PAGE para que la navegacion (mes/semana anterior-siguiente) y el
 * filtro de estado sobrevivan a los postbacks ajax sin requerir una conversacion
 * de larga duracion (la pantalla de lista hace end-conversation). No modifica la
 * logica de negocio: solo lee planes por rango via {@link XProductionPlanService}.
 */
@Name("xproductionCalendar")
@Scope(ScopeType.PAGE)
@AutoCreate
public class XProductionCalendarAction implements Serializable {

    public static final String VIEW_MONTH = "MONTH";
    public static final String VIEW_WEEK = "WEEK";

    private static final Locale ES = new Locale("es");

    @In
    private XProductionPlanService xproductionPlanService;

    private Date referenceDate;
    private String viewMode = VIEW_MONTH;
    private ProductionPlanState stateFilter;

    private List<List<CalendarDay>> weeks = new ArrayList<List<CalendarDay>>();
    private BigDecimal totalProgrammed = BigDecimal.ZERO;
    private BigDecimal totalExecuted = BigDecimal.ZERO;

    @Create
    public void init() {
        referenceDate = truncate(new Date());
        build();
    }

    /* ------------------------------------------------------------------ */
    /* Navegacion                                                         */
    /* ------------------------------------------------------------------ */

    public void prev() {
        shift(-1);
    }

    public void next() {
        shift(1);
    }

    public void goToday() {
        referenceDate = truncate(new Date());
        build();
    }

    public void showMonth() {
        viewMode = VIEW_MONTH;
        build();
    }

    public void showWeek() {
        viewMode = VIEW_WEEK;
        build();
    }

    private void shift(int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(referenceDate);
        c.add(VIEW_WEEK.equals(viewMode) ? Calendar.DAY_OF_MONTH : Calendar.MONTH,
                VIEW_WEEK.equals(viewMode) ? amount * 7 : amount);
        referenceDate = c.getTime();
        build();
    }

    /* ------------------------------------------------------------------ */
    /* Construccion de la grilla                                          */
    /* ------------------------------------------------------------------ */

    public void build() {
        boolean week = VIEW_WEEK.equals(viewMode);

        Calendar ref = Calendar.getInstance();
        ref.setTime(truncate(referenceDate));
        int refMonth = ref.get(Calendar.MONTH);

        Calendar cursor = (Calendar) ref.clone();
        if (!week) {
            cursor.set(Calendar.DAY_OF_MONTH, 1);
        }
        toMonday(cursor);

        int numWeeks = week ? 1 : 6;

        Date gridStart = cursor.getTime();
        Calendar endCal = (Calendar) cursor.clone();
        endCal.add(Calendar.DAY_OF_MONTH, numWeeks * 7 - 1);
        Date gridEnd = endCal.getTime();

        Map<String, List<XProductionPlan>> plansByDay = loadPlans(gridStart, gridEnd);

        Calendar today = Calendar.getInstance();
        toZero(today);

        weeks = new ArrayList<List<CalendarDay>>();
        totalProgrammed = BigDecimal.ZERO;
        totalExecuted = BigDecimal.ZERO;

        for (int w = 0; w < numWeeks; w++) {
            List<CalendarDay> row = new ArrayList<CalendarDay>(7);
            for (int d = 0; d < 7; d++) {
                CalendarDay day = new CalendarDay();
                day.setDate(cursor.getTime());
                day.setInMonth(week || cursor.get(Calendar.MONTH) == refMonth);
                day.setToday(sameDay(cursor, today));

                List<XProductionPlan> dayPlans = plansByDay.get(dayKey(cursor.getTime()));
                if (dayPlans != null && !dayPlans.isEmpty()) {
                    fillDay(day, dayPlans);
                }
                row.add(day);
                cursor.add(Calendar.DAY_OF_MONTH, 1);
            }
            weeks.add(row);
        }
    }

    private Map<String, List<XProductionPlan>> loadPlans(Date start, Date end) {
        Map<String, List<XProductionPlan>> map = new HashMap<String, List<XProductionPlan>>();
        List<XProductionPlan> plans = xproductionPlanService.getProductionPlanList(start, end);
        if (plans != null) {
            for (XProductionPlan plan : plans) {
                if (plan.getDate() == null) {
                    continue;
                }
                String key = dayKey(plan.getDate());
                List<XProductionPlan> list = map.get(key);
                if (list == null) {
                    list = new ArrayList<XProductionPlan>();
                    map.put(key, list);
                }
                list.add(plan);
            }
        }
        return map;
    }

    private void fillDay(CalendarDay day, List<XProductionPlan> dayPlans) {
        XProductionPlan first = dayPlans.get(0);
        BigDecimal programmed = BigDecimal.ZERO;
        BigDecimal executed = BigDecimal.ZERO;
        int orders = 0;
        Set<String> siglas = new LinkedHashSet<String>();
        List<String> orderStates = new ArrayList<String>();

        for (XProductionPlan plan : dayPlans) {
            programmed = programmed.add(sumProgrammed(plan));
            executed = executed.add(sumExecuted(plan));
            if (plan.getProductionList() != null) {
                orders += plan.getProductionList().size();
                for (XProduction production : plan.getProductionList()) {
                    String s = productionSigla(production);
                    if (s != null) {
                        siglas.add(s);
                    }
                    if (production.getState() != null) {
                        orderStates.add(production.getState().name().toLowerCase());
                    }
                }
            }
        }

        day.setPlan(first);
        day.setState(first.getState());
        day.setProgrammed(programmed);
        day.setExecuted(executed);
        day.setOrders(orders);
        day.setSiglas(joinSiglas(siglas));
        day.setOrderStates(orderStates);

        boolean matches = stateFilter == null || stateFilter.equals(first.getState());
        day.setMatchesFilter(matches);

        if (matches) {
            totalProgrammed = totalProgrammed.add(programmed);
            totalExecuted = totalExecuted.add(executed);
        }
    }

    /** Cantidad programada = suma de cantidades de los productos planificados del plan. */
    private BigDecimal sumProgrammed(XProductionPlan plan) {
        BigDecimal sum = BigDecimal.ZERO;
        if (plan.getProductionProductList() != null) {
            for (XProductionProduct product : plan.getProductionProductList()) {
                sum = sum.add(nz(product.getQuantity()));
            }
        }
        return sum;
    }

    /**
     * Cantidad ejecutada = suma de cantidades producidas en las ordenes del plan
     * que no esten pendientes ni anuladas (EJE/APR/FIN/CONTA).
     */
    private BigDecimal sumExecuted(XProductionPlan plan) {
        BigDecimal sum = BigDecimal.ZERO;
        if (plan.getProductionList() != null) {
            for (XProduction production : plan.getProductionList()) {
                if (!isExecutedState(production.getState())) {
                    continue;
                }
                if (production.getProductionProductList() != null) {
                    for (XProductionProduct product : production.getProductionProductList()) {
                        sum = sum.add(nz(product.getQuantity()));
                    }
                }
            }
        }
        return sum;
    }

    /** Sigla de producto de una orden, tomada de su formulacion (null si no tiene). */
    private String productionSigla(XProduction production) {
        if (production == null || production.getFormulation() == null) {
            return null;
        }
        String s = production.getFormulation().getSigla();
        if (s == null) {
            return null;
        }
        s = s.trim();
        return s.length() == 0 ? null : s;
    }

    /** Une las siglas distintas separadas por coma (ej. "ULEX, BAR"). */
    private static String joinSiglas(Set<String> siglas) {
        StringBuilder sb = new StringBuilder();
        for (String s : siglas) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private boolean isExecutedState(ProductionState state) {
        return state == ProductionState.EJE
                || state == ProductionState.APR
                || state == ProductionState.FIN
                || state == ProductionState.CONTA;
    }

    /* ------------------------------------------------------------------ */
    /* Helpers de fecha                                                   */
    /* ------------------------------------------------------------------ */

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static Date truncate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        toZero(c);
        return c.getTime();
    }

    private static void toZero(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private static void toMonday(Calendar c) {
        toZero(c);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        int diff = (dow == Calendar.SUNDAY) ? 6 : (dow - Calendar.MONDAY);
        c.add(Calendar.DAY_OF_MONTH, -diff);
    }

    private static boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private static String dayKey(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }

    private static String capitalize(String text) {
        if (text == null || text.length() == 0) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /* ------------------------------------------------------------------ */
    /* Getters para la vista                                              */
    /* ------------------------------------------------------------------ */

    public List<List<CalendarDay>> getWeeks() {
        return weeks;
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }

    public boolean isMonthView() {
        return VIEW_MONTH.equals(viewMode);
    }

    public boolean isWeekView() {
        return VIEW_WEEK.equals(viewMode);
    }

    public ProductionPlanState getStateFilter() {
        return stateFilter;
    }

    public void setStateFilter(ProductionPlanState stateFilter) {
        this.stateFilter = stateFilter;
    }

    public ProductionPlanState[] getStates() {
        return ProductionPlanState.values();
    }

    public Date getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(Date referenceDate) {
        this.referenceDate = referenceDate;
    }

    /** Etiqueta del periodo: "Mayo 2026" (mes) o "5 may - 11 may 2026" (semana). */
    public String getPeriodLabel() {
        if (VIEW_WEEK.equals(viewMode) && !weeks.isEmpty()) {
            List<CalendarDay> row = weeks.get(0);
            Date start = row.get(0).getDate();
            Date end = row.get(6).getDate();
            SimpleDateFormat dm = new SimpleDateFormat("d MMM", ES);
            SimpleDateFormat dmy = new SimpleDateFormat("d MMM yyyy", ES);
            return dm.format(start) + " - " + dmy.format(end);
        }
        return capitalize(new SimpleDateFormat("MMMM yyyy", ES).format(referenceDate));
    }

    public BigDecimal getTotalProgrammed() {
        return totalProgrammed;
    }

    public BigDecimal getTotalExecuted() {
        return totalExecuted;
    }

    public int getTotalProgress() {
        if (totalProgrammed == null || totalProgrammed.signum() <= 0 || totalExecuted == null) {
            return 0;
        }
        int pct = totalExecuted.multiply(BigDecimal.valueOf(100))
                .divide(totalProgrammed, 0, BigDecimal.ROUND_HALF_UP).intValue();
        if (pct < 0) {
            return 0;
        }
        return pct > 100 ? 100 : pct;
    }
}
