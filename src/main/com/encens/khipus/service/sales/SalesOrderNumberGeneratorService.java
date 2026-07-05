package com.encens.khipus.service.sales;

import com.encens.khipus.model.sales.SalesOrder;

import javax.ejb.Local;

/**
 * Genera el numero correlativo de la Orden de Venta (por compania, 6 digitos).
 *
 * @author
 * @version 1.0
 */
@Local
public interface SalesOrderNumberGeneratorService {

    String generateSalesOrderNumber(SalesOrder salesOrder);
}
