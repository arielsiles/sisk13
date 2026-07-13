package com.encens.khipus.service.production;


import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.RawMaterialPayRollException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.*;

import javax.ejb.Local;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Local
public interface RawMaterialPayRollService extends GenericService {

    public DiscountProducer findDiscountProducerByDate(Date date);

    public List<DiscountProducer> findDiscountsProducerByDate(Date date);

    public RawMaterialPayRoll generatePayroll(RawMaterialPayRoll rawMaterialPayRoll, DiscountProducer discountProducer, Double totalWeightFortnight, Map<Long, ProducerTax> producerTaxCache, Map<Long, ProducerCollectionRestriction> restrictionCache, int dayFilter) throws EntryNotFoundException, RawMaterialPayRollException;

    /**
     * Genera una planilla de EXCEDENTE (pura): paga solo el excedente de cada
     * productor restringido, sin ajuste, reserva, alcohol, descuentos ni retencion.
     * Precio de excedente = override negociado por el productor (&gt;0) o, si no,
     * globalExcessPrice (precio de excedente global segun tipo de dia).
     */
    public RawMaterialPayRoll generateExcessPayroll(RawMaterialPayRoll rawMaterialPayRoll, Map<Long, ProducerCollectionRestriction> restrictionCache, int dayFilter, double globalExcessPrice) throws RawMaterialPayRollException;

    Map<Long, ProducerTax> preloadProducerTaxes(Date startDate, Date endDate);

    void calculateLiquidPayable(RawMaterialPayRoll rawMaterialPayRoll);

    void create(RawMaterialPayRoll rawMaterialPayRoll) throws EntryDuplicatedException, RawMaterialPayRollException;

    void createAll(RawMaterialPayRoll rawMaterialPayRoll) throws EntryDuplicatedException, RawMaterialPayRollException;

    void validate(RawMaterialPayRoll rawMaterialPayRoll) throws RawMaterialPayRollException;

    RawMaterialPayRoll getTotalsRawMaterialPayRoll(Date dateIni, Date dateEnd, ProductiveZone productiveZone, MetaProduct metaProduct);

    RawMaterialPayRollServiceBean.Discounts getDiscounts(Date dateIni, Date dateEnd, ProductiveZone zone, MetaProduct metaProduct);

    RawMaterialPayRollServiceBean.Discounts getDiscounts(Date dateIni, Date dateEnd, MetaProduct metaProduct, PayRollType type, DayType dayType);

    RawMaterialPayRollServiceBean.SummaryTotal getSumaryTotal(Date dateIni, Date dateEnd, ProductiveZone zone, MetaProduct metaProduct);

    List<RawMaterialPayRecordDetailDummy> generateDetails(RawMaterialPayRecord rawMaterialPayRecord) throws RawMaterialPayRollException;

    Double getTotalWeightMoney(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct);

    Double getTotalMoneyDiff(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct);

    Double getBalanceWeightTotal(Double unitPrice, Date dateIni, Date dateEnd, MetaProduct metaProduct);

    Double getTotalDiff(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct);

    List<RawMaterialPayRoll> findAll(Date startDate, Date endDate, MetaProduct metaProduct);

    /** Planillas del periodo (solo por fechas, cualquier estado). Para detectar el estado del periodo. */
    List<RawMaterialPayRoll> findAllInDates(Date startDate, Date endDate);

    /** Estado de la planilla del periodo (query escalar: refleja la BD sin caché de entidades). */
    StatePayRoll findPeriodState(Date startDate, Date endDate);

    /** Fecha fin de la ultima planilla CONTABILIZADO (null si no hay). Para sugerir la proxima quincena. */
    Date getLastAccountedEndDate();

    List<RawMaterialPayRoll> findAll();

    boolean verifDayColected(Calendar date_aux, ProductiveZone zone);

    void approvedSession(Calendar startDate, Calendar endDate, ProductiveZone productiveZone);

    public List<RawMaterialPayRoll> findAllPayRollesByGAB(Date startDate, Date endDate, ProductiveZone productiveZone);

    public void approvedNoteRejection(Calendar startDate, Calendar endDate);

    void approvedDiscounts(Calendar startDate, Calendar endDate, ProductiveZone productiveZone);

    /** Commit de la deuda al contabilizar: reduce saldo de los movimientos por sus aplicaciones. */
    void commitDiscountDebts(Date startDate, Date endDate, MetaProduct metaProduct);

    /** Reversa de la deuda al anular/revertir: restaura saldo de los movimientos. */
    void revertDiscountDebts(Date startDate, Date endDate, MetaProduct metaProduct);

    /**
     * Monto de descuento REALMENTE COBRADO (aplicado, con tope/arrastre) en el periodo, por
     * productor, para un tipo de movimiento (VETERINARIO, YOGURT, ...). Es lo que debe ir al
     * asiento (Clientes Productores): la suma cuadra con el descuento que bajo el liquido, no con
     * el valor NOMINAL de la deuda. Cada fila = [idNumber (String), suma montoaplicado (Double)].
     */
    List<Object[]> getAppliedDiscountsByProducer(Date startDate, Date endDate, MetaProduct metaProduct, TypeMovementProducer type);

    /** Cambia el estado de todas las planillas del periodo/producto. */
    void setPayRollsState(Date startDate, Date endDate, MetaProduct metaProduct, StatePayRoll state);

    /** Guarda (o limpia con null) el id del comprobante en las planillas del periodo. */
    void setPayRollsVoucherId(Date startDate, Date endDate, MetaProduct metaProduct, Long voucherId);

    /** Id del comprobante contabilizado del periodo (null si no hay). */
    Long findAccountingVoucherId(Date startDate, Date endDate, MetaProduct metaProduct);

    /** Hay una quincena anterior (motor nuevo) sin contabilizar (guard de orden). */
    boolean hasPriorUncontabilized(Date startDate, MetaProduct metaProduct);

    /** Hay una quincena POSTERIOR generada (mismo metaProducto, cualquier estado). Para bloquear
     *  revertir/borrar fuera de orden: por el arrastre de deuda, debe hacerse en orden inverso. */
    boolean hasLaterPayroll(Date startDate, MetaProduct metaProduct);

    void approvedDiscountsGAB(Calendar startDate, Calendar endDate, ProductiveZone productiveZone);

    void approvedRawMaterialPayRoll(Calendar startDate, Calendar endDate, ProductiveZone productiveZone);

    Double getReservProducer(Date startDate, Date endDate);

    void deleteReserveDiscount(Date startDate, Date endDate);

    List<DiscountProducer> findDiscountProducerByDate(Date startDate, Date endDate);

    void approvedReservProductor(Calendar startDate, Calendar endDate);

    public List<BoletaPagoProductor> findBoletaDePago(Date fechaIni,Date fechaFin, RawMaterialProducer rawMaterialProducer,ProductiveZone productiveZone,MetaProduct metaProduct);

    public List<BoletaPagoProductor> findBoletaDePagoGA(Date fechaIni,Date fechaFin, RawMaterialProducer rawMaterialProducer,ProductiveZone productiveZone,MetaProduct metaProduct);

    Double getSumAdjustmentFromRecords(Date startDate, Date endDate, MetaProduct metaProduct);

    Double getSumAdjustmentFromRecords(Date startDate, Date endDate, MetaProduct metaProduct, PayRollType type, DayType dayType);
}
