package com.encens.khipus.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Devengue de intereses de un DPF sobre los movimientos del mayor.
 * <p/>
 * Vive aparte porque hay DOS pantallas que tienen que dar exactamente el mismo numero:
 * la provision mensual, que acredita el pasivo mes a mes, y el cierre del certificado,
 * que lo debita. Si cada una calculara por su cuenta, cualquier diferencia de redondeo o
 * de criterio dejaria un saldo colgado en "cargos financieros por pagar" imposible de
 * rastrear.
 * <p/>
 * Convenciones del negocio, verificadas contra el historico 2025:
 * <ul>
 *   <li>Dias INCLUSIVOS en ambos extremos.</li>
 *   <li>Base 360: <code>capital * tasa * dias / 36000</code>.</li>
 *   <li>Los movimientos son filas <code>[idcuenta, fecha, debe, haber, debeMe, haberMe]</code>
 *       tal como las devuelve <code>AccountService.getAccountLedgerMovements</code>.</li>
 * </ul>
 *
 * @author
 */
public final class FixedTermDepositAccrual {

    /** tasa en % anual y base 360: 100 * 360. */
    private static final BigDecimal DAILY_RATE_DIVISOR = new BigDecimal("36000");

    /** Escala de trabajo: se redondea al final, no en cada tramo. */
    public static final int WORKING_SCALE = 10;

    private FixedTermDepositAccrual() {
    }

    /** Interes devengado de un tramo: capital * tasa * dias / 36000. */
    public static BigDecimal accrual(BigDecimal capital, BigDecimal rate, int days) {
        if (capital == null || rate == null || days <= 0) {
            return BigDecimal.ZERO;
        }
        return capital.multiply(rate).multiply(new BigDecimal(days))
                .divide(DAILY_RATE_DIVISOR, WORKING_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Capital vigente a una fecha: suma de los ABONOS hasta esa fecha inclusive. Con
     * <code>date</code> en null suma todos.
     * <p/>
     * Los debitos se ignoran a proposito. El movimiento deudor de un certificado es su
     * cancelacion, y el dia en que se cancela devenga igual: un DPF gana interes hasta esa
     * fecha INCLUSIVE, asi que restar el debito costaria un dia de interes. Que el
     * certificado este cerrado se decide con {@link #closingDate}, no restando aca.
     */
    public static BigDecimal capitalAt(List<Object[]> movements, Date date, boolean foreign) {
        BigDecimal capital = BigDecimal.ZERO;
        if (movements == null) {
            return capital;
        }
        for (Object[] movement : movements) {
            Date movementDate = DateUtils.removeTime((Date) movement[1]);
            if (movementDate == null || (date != null && movementDate.after(date))) {
                continue;
            }
            capital = capital.add(nullToZero((BigDecimal) (foreign ? movement[5] : movement[3])));
        }
        return capital;
    }

    /**
     * Fechas dentro del rango en las que el capital aumento; cada una abre un tramo nuevo.
     * Solo los abonos cortan tramo, por lo explicado en {@link #capitalAt}.
     */
    public static List<Date> capitalIncreaseDates(List<Object[]> movements, Date from, Date until,
                                                  boolean foreign) {
        List<Date> dates = new ArrayList<Date>();
        if (movements == null) {
            return dates;
        }
        for (Object[] movement : movements) {
            Date movementDate = DateUtils.removeTime((Date) movement[1]);
            if (movementDate == null || !movementDate.after(from) || movementDate.after(until)) {
                continue;
            }
            BigDecimal credit = nullToZero((BigDecimal) (foreign ? movement[5] : movement[3]));
            if (credit.doubleValue() > 0 && !dates.contains(movementDate)) {
                dates.add(movementDate);
            }
        }
        Collections.sort(dates);
        return dates;
    }

    /**
     * Fecha en que el certificado se cerro: el ultimo movimiento, si el saldo quedo en
     * cero. Null si sigue vivo.
     * <p/>
     * Se mira el saldo y no el estado a proposito. Un DPF renovado tambien queda INACTIVE
     * y ese SI devenga hasta su vencimiento; y un retiro total cargado por comprobante no
     * toca el estado, deja la cuenta en ACTIVE con saldo cero.
     */
    public static Date closingDate(List<Object[]> movements, boolean foreign) {
        if (movements == null || movements.isEmpty()) {
            return null;
        }
        BigDecimal balance = BigDecimal.ZERO;
        Date lastDate = null;
        for (Object[] movement : movements) {
            Date movementDate = DateUtils.removeTime((Date) movement[1]);
            balance = balance
                    .add(nullToZero((BigDecimal) (foreign ? movement[5] : movement[3])))
                    .subtract(nullToZero((BigDecimal) (foreign ? movement[4] : movement[2])));
            /** El mayor, no el ultimo de la lista: la consulta no garantiza el orden. */
            if (movementDate != null && (lastDate == null || movementDate.after(lastDate))) {
                lastDate = movementDate;
            }
        }
        return BigDecimalUtil.roundBigDecimal(balance, 2).compareTo(BigDecimal.ZERO) == 0
                ? lastDate : null;
    }

    /**
     * Fecha de cierre ANTICIPADO: la del cierre, pero solo si ocurrio ANTES del
     * vencimiento. Null si el certificado sigue vivo o si se liquido al vencimiento o
     * despues.
     * <p/>
     * La distincion es la que decide si hay que cortar el devengue, y no es lo mismo que
     * "esta cerrado":
     * <ul>
     *   <li><b>Renovacion o retiro al vencimiento:</b> el saldo tambien queda en cero -- en
     *       la renovacion el capital se traslada al certificado nuevo -- pero el
     *       certificado gano el plazo COMPLETO y tiene que devengar hasta su fechavence.
     *       Ademas la renovacion debita del pasivo el interes de todo el plazo, asi que si
     *       el ultimo mes no se provisionara quedaria un faltante permanente.</li>
     *   <li><b>Retiro anticipado:</b> el interes se corta el dia del retiro.</li>
     * </ul>
     * Sin vencimiento cargado no se puede clasificar, y se devuelve null (no se corta).
     * La provision no llega a ese caso: solo selecciona cuentas con fechavence.
     */
    public static Date earlyClosingDate(List<Object[]> movements, boolean foreign, Date expirationDate) {
        Date closing = closingDate(movements, foreign);
        if (closing == null || expirationDate == null) {
            return null;
        }
        return closing.before(DateUtils.removeTime(expirationDate)) ? closing : null;
    }

    /**
     * Interes devengado entre dos fechas, partiendo en tramos por cada aumento de capital.
     * Con un solo tramo -- el caso normal -- equivale a capital * tasa * dias / 36000.
     */
    public static BigDecimal accrualBetween(List<Object[]> movements, Date from, Date until,
                                            BigDecimal rate, boolean foreign) {
        if (from == null || until == null || until.before(from)) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        List<Date> increaseDates = capitalIncreaseDates(movements, from, until, foreign);
        Date segmentFrom = from;
        for (int i = 0; i <= increaseDates.size(); i++) {
            /** El dia del aumento ya devenga con el capital nuevo: el tramo anterior
             *  cierra el dia previo. */
            Date segmentUntil = i < increaseDates.size()
                    ? DateUtils.addDay(increaseDates.get(i), -1)
                    : until;
            int days = segmentUntil.before(segmentFrom)
                    ? 0 : (int) DateUtils.daysBetween(segmentFrom, segmentUntil, true);
            if (days > 0) {
                total = total.add(accrual(capitalAt(movements, segmentFrom, foreign), rate, days));
            }
            if (i < increaseDates.size()) {
                segmentFrom = increaseDates.get(i);
            }
        }
        return total;
    }

    public static BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
