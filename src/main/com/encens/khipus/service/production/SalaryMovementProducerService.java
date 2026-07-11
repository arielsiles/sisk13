package com.encens.khipus.service.production;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.SalaryMovementProducerException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.production.*;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Local
public interface SalaryMovementProducerService extends GenericService {
    public RawMaterialProducerDiscount prepareDiscount(RawMaterialProducer rawMaterialProducer, Date startDate, Date endDate,ProductiveZone productiveZone) throws EntryNotFoundException;

    Double getTotalCollectedByProductor(RawMaterialProducer rawMaterialProducer, Date date);

    public ProductiveZone getZoneProductiveByProductor(RawMaterialProducer rawMaterialProducer);

    public void moveSessionsProductor(RawMaterialProducer rawMaterialProducer, Date date,ProductiveZone productiveZone) throws SalaryMovementProducerException;

    void moveDiscountsProductor(RawMaterialProducer rawMaterialProducer, Date date, ProductiveZone productiveZone) throws SalaryMovementProducerException;

    void createSalaryMovementProducer(CustomerOrder customerOrder);

    List<RawMaterialProducer> findProducersWithCollection(Date startDate, Date endDate);

    void createSalaryMovementProducer(List<SalaryMovementProducer> salaryMovementProducerList);

    List<SalaryMovementProducer> findSalaryMovementProducerList(Date starDate, Date endDate, TypeMovementProducer typeMovementProducer);

    List<SalaryMovementProducer> findFiltered(Date startDate, Date endDate, TypeMovementProducer typeMovementProducer, String firstName, String lastName, String maidenName);

    /** @Claude OPT-4: Firma batch para pre-cargar descuentos de todos los productores de una zona **/
    Map<Long, RawMaterialProducerDiscount> prepareDiscountsBatch(Date startDate, Date endDate, ProductiveZone productiveZone);

    /**
     * Movimientos que ARRASTRAN deuda (veterinario, credito, concentrados, yogurt, tachos,
     * otros egresos) con saldo pendiente (&gt;0) y fecha &lt;= endDate, por zona, agrupados por
     * productor y ordenados FIFO (mas antiguo primero). Base del cobro con tope/arrastre.
     */
    Map<Long, List<SalaryMovementProducer>> preloadCarryMovements(Date endDate, ProductiveZone productiveZone);

    /** Movimientos de COMISION BANCO de la quincena (saldo>0), por productor y FIFO. Se aplican
     *  con trazabilidad (aplicacion) para que el saldo baje y quede PAGADO solo si se cobro. */
    Map<Long, List<SalaryMovementProducer>> preloadCommissionMovements(Date startDate, Date endDate, ProductiveZone productiveZone);

    void importSalaryMovements(List<SalaryMovementProducer> list);

}
