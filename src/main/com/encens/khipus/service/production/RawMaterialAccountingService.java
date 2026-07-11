package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.production.MetaProduct;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface RawMaterialAccountingService extends GenericService {

    /**
     * Arma y guarda el comprobante contable de la quincena (bloques quincena habil,
     * domingos y excedentes) y lo devuelve (con id). Es el asiento que efectiviza el pago.
     * glossPeriodo = texto del periodo para la glosa (ej. "1RA QUINCENA MAYO 2026").
     */
    Voucher contabilizar(Date startDate, Date endDate, MetaProduct metaProduct, String glossPeriodo);
}
