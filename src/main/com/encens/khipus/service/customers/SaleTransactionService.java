package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.CustomerOrder;

import javax.ejb.Local;

/**
 * Servicio transaccional para la creacion atomica de ventas.
 * Persiste el pedido y actualiza inventario en una sola transaccion REQUIRES_NEW.
 */
@Local
public interface SaleTransactionService {

    /**
     * Crea la venta y actualiza inventario atomicamente.
     * Si cualquier paso falla, toda la operacion se revierte.
     *
     * @param customerOrder pedido ya construido con su lista de articulos
     */
    void createSaleWithInventory(CustomerOrder customerOrder);
}
