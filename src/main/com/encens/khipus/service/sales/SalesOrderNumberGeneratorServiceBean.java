package com.encens.khipus.service.sales;

import com.encens.khipus.model.sales.SalesOrder;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

/**
 * Genera el numero correlativo de la Orden de Venta por compania, formateado a
 * 6 digitos (ej. "000099"). Usa la secuencia compartida via
 * {@link SequenceGeneratorService}.
 *
 * @author
 * @version 1.0
 */
@Stateless
@Name("salesOrderNumberGeneratorService")
@AutoCreate
public class SalesOrderNumberGeneratorServiceBean implements SalesOrderNumberGeneratorService {

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    public synchronized String generateSalesOrderNumber(SalesOrder salesOrder) {
        String companyNumber = salesOrder.getCompanyNumber() != null
                ? salesOrder.getCompanyNumber() : Constants.defaultCompanyNumber;

        String sequenceName = "ordenventa_" + companyNumber;
        long value = sequenceGeneratorService.nextValue(sequenceName);
        return String.format("%06d", value);
    }
}
