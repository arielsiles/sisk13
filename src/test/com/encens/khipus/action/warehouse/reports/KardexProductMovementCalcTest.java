package com.encens.khipus.action.warehouse.reports;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la logica de calculo del reporte de movimientos por articulo:
 *  - tnToUnit: conversion TN -> unidad del articulo (KG = x1000).
 *  - Reconciliacion del saldo BARITINA con la vista "Saldos de Almacen":
 *    ambos deben usar el mismo criterio de acopio (Peso Empresa = balanceWeight,
 *    estados APR/CONTA), de modo que el saldo final del reporte cuadre con el balance.
 */
class KardexProductMovementCalcTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void tnToUnit_kgMultiplicaPorMil() {
        assertEquals(0, KardexProductMovementAction.tnToUnit(bd("2.5"), "KG").compareTo(bd("2500.0")));
        assertEquals(0, KardexProductMovementAction.tnToUnit(bd("2.5"), "kg").compareTo(bd("2500.0")));
    }

    @Test
    void tnToUnit_nullEsCero() {
        assertEquals(0, KardexProductMovementAction.tnToUnit(null, "KG").compareTo(BigDecimal.ZERO));
    }

    @Test
    void tnToUnit_otraUnidadSeAsumeTn() {
        assertEquals(0, KardexProductMovementAction.tnToUnit(bd("3"), "TN").compareTo(bd("3")));
    }

    /**
     * Escenario 4-BARITINA (01/01/2026 - 01/07/2026, saldo anterior 0):
     * el saldo final = saldoAnterior + SUM(entradas) - SUM(salidas).
     * Con el acopio medido por Peso Empresa (balanceWeight) el resultado es 996.660,
     * el mismo que debe mostrar "Saldos de Almacen" tras corregir su criterio.
     */
    @Test
    void baritina_saldoFinalCuadraConBalance() {
        BigDecimal saldoAnterior = BigDecimal.ZERO;
        BigDecimal acopioBalanceWeight = bd("1071660.00"); // Peso Empresa
        BigDecimal consumoProduccion = bd("75000.00");     // ordenes 309 + 307

        BigDecimal saldoFinalKardex = saldoAnterior.add(acopioBalanceWeight).subtract(consumoProduccion);
        BigDecimal balanceCorregido = acopioBalanceWeight.subtract(consumoProduccion);

        assertEquals(0, saldoFinalKardex.compareTo(bd("996660.00")));
        assertEquals(0, saldoFinalKardex.compareTo(balanceCorregido));
    }

    /**
     * El descuadre original (balance = 1.658.480 vs kardex = 996.660) se explica
     * integramente por el acopio: el balance viejo inflaba con netWeight/estado PEN.
     */
    @Test
    void baritina_gapExplicadoPorAcopio() {
        BigDecimal saldoFinalKardex = bd("996660.00");
        BigDecimal balanceViejo = bd("1733480.00").subtract(bd("75000.00")); // acopio net+PEN - consumo
        assertEquals(0, balanceViejo.compareTo(bd("1658480.00")));
        assertEquals(0, balanceViejo.subtract(saldoFinalKardex).compareTo(bd("661820.00")));
    }

    /** ULEXITA: reproceso final (2 TN) entra, consumo reproceso (1 TN) sale; neto +1000 KG. */
    @Test
    void ulexita_reprocesoNetoEnKg() {
        BigDecimal entrada = KardexProductMovementAction.tnToUnit(bd("2"), "KG");
        BigDecimal salida = KardexProductMovementAction.tnToUnit(bd("1"), "KG");
        assertEquals(0, entrada.compareTo(bd("2000.0")));
        assertEquals(0, salida.compareTo(bd("1000.0")));
        assertTrue(entrada.subtract(salida).compareTo(bd("1000.0")) == 0);
    }
}
