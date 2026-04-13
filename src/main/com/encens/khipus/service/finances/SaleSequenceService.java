package com.encens.khipus.service.finances;

import javax.ejb.Local;

/**
 * Servicio de generacion de secuencias con transaccion independiente.
 * Reemplaza la funcion MySQL getNextSeq() con una implementacion Java pura
 * que usa REQUIRES_NEW para liberar el lock de la fila inmediatamente.
 */
@Local
public interface SaleSequenceService {

    /**
     * Obtiene el siguiente valor de secuencia para el nombre dado.
     * Opera en su propia transaccion (REQUIRES_NEW), liberando el lock al retornar.
     *
     * @param sequenceName nombre de la secuencia (ej: SECUENCIAPEDIDO, VENTADIRECTA, VALE, etc.)
     * @return siguiente valor de la secuencia
     */
    long getNextValue(String sequenceName);
}
