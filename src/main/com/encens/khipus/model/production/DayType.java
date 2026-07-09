package com.encens.khipus.model.production;

/**
 * Tipo de dia de una planilla de acopio.
 *
 * Junto con {@link PayRollType} (NORMAL / EXCEDENTE) distingue las planillas que
 * genera el proceso unico por quincena:
 *   NORMAL+HABIL, NORMAL+DOMINGO, EXCEDENTE+HABIL, EXCEDENTE+DOMINGO.
 *
 * NINGUNO: planillas historicas anteriores a esta separacion.
 */
public enum DayType {
    HABIL,
    DOMINGO,
    NINGUNO
}
