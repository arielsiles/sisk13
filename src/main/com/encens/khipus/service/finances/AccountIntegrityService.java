package com.encens.khipus.service.finances;

import com.encens.khipus.model.finances.DanglingAccount;

import javax.ejb.Local;
import java.util.List;

/**
 * Detecta y repara referencias colgadas a cuentas contables: columnas de
 * `configuracion` y `ck_ctas_bco` cuyo codigo no existe en el plan de cuentas
 * `arcgms`.
 * <p>
 * Todo se resuelve con SQL nativo, sin cargar la entidad CashAccount -- que es la
 * que provoca el EntityNotFoundException al renderizar Preferencias de compania.
 *
 * @author
 * @version 1.0
 */
@Local
public interface AccountIntegrityService {

    /**
     * @return las referencias colgadas en `configuracion` y `ck_ctas_bco`.
     *         Lista vacia si esta todo integro.
     */
    List<DanglingAccount> findDanglingAccounts();

    /**
     * @return true si hay al menos una cuenta colgada. Usado por el page-action
     *         guardian, que solo necesita decidir si desviar.
     */
    boolean hasDangling();

    /**
     * Deja en NULL el codigo colgado del item. Segun el origen, actualiza
     * `configuracion` o `ck_ctas_bco`.
     *
     * @return true si se anulo; false si no se pudo (p.ej. columna NOT NULL en la
     *         BD del cliente), sin propagar la excepcion.
     */
    boolean nullify(DanglingAccount item);
}
