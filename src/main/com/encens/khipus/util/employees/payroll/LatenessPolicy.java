package com.encens.khipus.util.employees.payroll;

import java.util.List;

/**
 * Cuantos dias descuenta el atraso de un mes.
 * <p/>
 * Hay dos formas de mirar el mismo dato y dan resultados distintos:
 * <ul>
 *   <li><b>Por tramos acumulados</b> —la de siempre—: se suman los minutos del mes y se busca el
 *       tramo. Cuatro atrasos de 10 minutos y uno de 40 cuestan lo mismo: son 40 minutos.</li>
 *   <li><b>Por evento</b>: cada atraso se clasifica por su tamano y se cobra por separado. Cuatro
 *       de 10 minutos son cuatro faltas de puntualidad; uno de 40 es una sola, mas grave.</li>
 * </ul>
 * La segunda necesita el atraso <b>dia por dia</b>, que solo entrega el motor de jornadas. Con el
 * motor de bandas no hay ese detalle, asi que ahi siempre manda la de tramos.
 * <p/>
 * <b>Esta clase cuenta dias, no plata.</b> Sobre que base se valoriza el dia lo decide la
 * preferencia de la compania, y es otra decision.
 *
 * @author
 * @version 6.1.0
 */
public final class LatenessPolicy {

    /** Un atraso de hasta este tamano es de los "chicos": se cobran de a cuatro. */
    public static final int SMALL_MAX_MINUTES = 30;

    /** Cuantos atrasos chicos hacen un dia. */
    public static final int SMALL_PER_DAY = 4;

    /** Desde aca el atraso ademas amerita memorandum. */
    public static final int MEMO_MIN_MINUTES = 121;

    private final double days;
    private final int memos;
    private final int smallCount;
    private final int mediumCount;

    private LatenessPolicy(double days, int memos, int smallCount, int mediumCount) {
        this.days = days;
        this.memos = memos;
        this.smallCount = smallCount;
        this.mediumCount = mediumCount;
    }

    /**
     * La regla de siempre: por minutos acumulados en el mes.
     *
     * <pre>
     *  0 –  30  →  0
     * 31 –  60  →  medio dia
     * 61 –  90  →  1 dia
     * 91 – 120  →  2 dias
     *   ≥ 121   →  3 dias
     * </pre>
     */
    public static LatenessPolicy accumulated(int totalMinutes) {
        double days = 0d;
        if (totalMinutes >= 31 && totalMinutes <= 60) {
            days = 0.5d;
        } else if (totalMinutes >= 61 && totalMinutes <= 90) {
            days = 1d;
        } else if (totalMinutes >= 91 && totalMinutes <= 120) {
            days = 2d;
        } else if (totalMinutes >= 121) {
            days = 3d;
        }
        return new LatenessPolicy(days, 0, 0, 0);
    }

    /**
     * La regla por evento, para las areas que la tengan marcada.
     *
     * <pre>
     * cada 4 atrasos de hasta 30 min  →  1 dia   (8 son 2 dias; el resto no se arrastra)
     * un atraso de 31 a 120 min       →  medio dia, por cada uno
     * un atraso de 121 min o mas      →  medio dia + memorandum, por cada uno
     * </pre>
     *
     * Las tres se suman. El unico tope lo pone quien llama: no se puede descontar mas dias que los
     * que se estan pagando.
     *
     * @param latenessByDay los minutos de atraso de cada dia del mes; los dias sin atraso no entran
     */
    public static LatenessPolicy perEvent(List<Integer> latenessByDay) {
        int small = 0;
        int medium = 0;
        int memos = 0;
        if (null != latenessByDay) {
            for (Integer minutes : latenessByDay) {
                if (null == minutes || minutes <= 0) {
                    continue;
                }
                if (minutes <= SMALL_MAX_MINUTES) {
                    small++;
                } else if (minutes < MEMO_MIN_MINUTES) {
                    medium++;
                } else {
                    memos++;
                }
            }
        }
        /* Los chicos son una EQUIVALENCIA: cuatro hacen un dia, ocho hacen dos. Los que sobran al
           cerrar el mes no se arrastran: el mes cierra y empieza de cero. */
        double days = small / SMALL_PER_DAY;
        days += 0.5d * medium;
        days += 0.5d * memos;
        return new LatenessPolicy(days, memos, small, medium);
    }

    /** Los dias descontados, nunca mas que los que se estan pagando. */
    public double getDays(double paidDays) {
        double limit = paidDays < 0 ? 0 : paidDays;
        return days > limit ? limit : days;
    }

    public double getDays() {
        return days;
    }

    /** Cuantos memorandums hay que emitir: un atraso de 121 minutos o mas, cada uno. */
    public int getMemos() {
        return memos;
    }

    /** Cuantos atrasos de hasta 30 minutos hubo. Para explicar de donde salio el dia. */
    public int getSmallCount() {
        return smallCount;
    }

    /** Cuantos atrasos de 31 a 120 minutos hubo. */
    public int getMediumCount() {
        return mediumCount;
    }
}
