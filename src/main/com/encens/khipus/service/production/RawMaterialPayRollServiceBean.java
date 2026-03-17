package com.encens.khipus.service.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.RawMaterialPayRollException;
import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.*;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.RoundUtil;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.encens.khipus.exception.production.RawMaterialPayRollException.*;
import static java.util.Calendar.DAY_OF_MONTH;

@Name("rawMaterialPayRollService")
@Stateless
@AutoCreate
public class RawMaterialPayRollServiceBean extends ExtendedGenericServiceBean implements RawMaterialPayRollService {

    @In
    private RawMaterialProducerDiscountService rawMaterialProducerDiscountService;

    @In
    private SalaryMovementProducerService salaryMovementProducerService;

    @In
    private SalaryMovementGABService salaryMovementGABService;

    @In
    private CollectedRawMaterialCalculatorService collectedRawMaterialCalculatorService;

    @In
    private RawMaterialProducerService rawMaterialProducerService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void create(RawMaterialPayRoll rawMaterialPayRoll) throws EntryDuplicatedException, RawMaterialPayRollException {
        try {
            validate(rawMaterialPayRoll);
            Object args = preCreate(rawMaterialPayRoll);
            processCreate(rawMaterialPayRoll);
            postCreate(rawMaterialPayRoll, args);
            getEntityManager().flush();
        } catch (PersistenceException e) { //TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            log.debug("Persistence error..", e);
            log.info("PersistenceException caught");
            //log.error(e);
            throw new EntryDuplicatedException(e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void createAll(RawMaterialPayRoll rawMaterialPayRoll) throws EntryDuplicatedException, RawMaterialPayRollException {
        try {
            //validate(rawMaterialPayRoll);
            //Object args = preCreate(rawMaterialPayRoll);
            //processCreate(rawMaterialPayRoll);
            //postCreate(rawMaterialPayRoll, args);
            //getEntityManager().merge(rawMaterialPayRoll);
            //getEntityManager().flush();

            validate(rawMaterialPayRoll);
            Object args = preCreate(rawMaterialPayRoll);
            processCreate(rawMaterialPayRoll);
            postCreate(rawMaterialPayRoll, args);
            getEntityManager().flush();

        } catch (PersistenceException e) { //TODO when hibernate will fix this http://opensource.atlassian.com/projects/hibernate/browse/EJB-382, we have to restore EntityExistsException here.
            log.debug("Persistence error..", e);
            log.info("PersistenceException caught");
            //log.error(e);
            //throw new EntryDuplicatedException(e);
        }
    }

    @Override
    public void validate(RawMaterialPayRoll rawMaterialPayRoll) throws RawMaterialPayRollException {
        Date lastEndDate = (Date) getEntityManager().createNamedQuery("RawMaterialPayRoll.findLasEndDateByMetaProductAndProductiveZone")
                .setParameter("productiveZone", rawMaterialPayRoll.getProductiveZone())
                .setParameter("metaProduct", rawMaterialPayRoll.getMetaProduct())
                .getSingleResult();

        if (lastEndDate != null && rawMaterialPayRoll.getStartDate().compareTo(lastEndDate) <= 0) {
            throw new RawMaterialPayRollException(CROSS_WITH_ANOTHER_PAYROLL, lastEndDate);
        }

        if (lastEndDate == null) {
            lastEndDate = new Date(0L);
        }

        Calendar c = Calendar.getInstance();
        c.setTime(lastEndDate);
        c.add(DAY_OF_MONTH, 1);
        lastEndDate = c.getTime();

        Date minimumStartDate = (Date) getEntityManager().createNamedQuery("RawMaterialCollectionSession.findMinimumDateOfCollectionSessionByMetaProductBetweenDates")
                .setParameter("productiveZone", rawMaterialPayRoll.getProductiveZone())
                .setParameter("metaProduct", rawMaterialPayRoll.getMetaProduct())
                .setParameter("startDate", lastEndDate)
                .setParameter("endDate", rawMaterialPayRoll.getStartDate())
                .getSingleResult();

        if (minimumStartDate != null && minimumStartDate.compareTo(rawMaterialPayRoll.getStartDate()) < 0) {
            throw new RawMaterialPayRollException(MINIMUM_START_DATE, minimumStartDate);
        }
    }

    @Override
    public List<RawMaterialPayRecordDetailDummy> generateDetails(RawMaterialPayRecord rawMaterialPayRecord) throws RawMaterialPayRollException {
        List<RawMaterialPayRecordDetailDummy> result = new ArrayList<RawMaterialPayRecordDetailDummy>();

        Map<Date, Double> totalWeight = createMapOfCollectedAmount(rawMaterialPayRecord.getRawMaterialPayRoll());
        Map<Date, Long> countProducers = createMapOfTotalProducers(rawMaterialPayRecord.getRawMaterialPayRoll());

        double taxRate = rawMaterialPayRecord.getRawMaterialPayRoll().getTaxRate() / 100;
        List<Object[]> collectedProducers = find("RawMaterialPayRoll.findCollectedAmountByMetaProductBetweenDates", rawMaterialPayRecord.getRawMaterialPayRoll());

        RawMaterialProducer producer = rawMaterialPayRecord.getRawMaterialProducerDiscount().getRawMaterialProducer();
        Double unitPrice = rawMaterialPayRecord.getRawMaterialPayRoll().getUnitPrice();
        for (Object[] obj : collectedProducers) {
            Date date = (Date) obj[0];
            RawMaterialProducer rawMaterialProducer = (RawMaterialProducer) obj[1];
            Double amount = (Double) obj[2];

            if (rawMaterialProducer.getId().equals(producer.getId()) == false) {
                continue;
            }

            Double delta = find(totalWeight, date);
            Long count = find(countProducers, date);
            Double adjustment = delta / count;
            Double earned = (amount + adjustment) * unitPrice;
            Double withholding = (hasLicense(rawMaterialPayRecord, date) ? 0.0 : earned * taxRate);

            RawMaterialPayRecordDetailDummy dummy = new RawMaterialPayRecordDetailDummy();
            dummy.setDate(date);
            dummy.setCollectedAmount(amount);
            dummy.setProductiveZoneDelta(delta);
            dummy.setProductiveZoneAdjustment(adjustment);
            dummy.setTotalProducers(count);
            dummy.setUnitPrice(unitPrice);
            dummy.setWithholding(withholding);
            dummy.setEarned(earned);
            dummy.setGrandTotal(earned - withholding);

            result.add(dummy);
        }
        return result;
    }

    /** @Claude OPT-6: Parametro totalWeightFortnight agregado para evitar recalculo por zona **/
    @Override
    public RawMaterialPayRoll generatePayroll(RawMaterialPayRoll rawMaterialPayRoll, DiscountProducer discountProducer, Double totalWeightFortnight, int dayFilter) throws EntryNotFoundException, RawMaterialPayRollException {
        Double totalReservaGAB = 0.0;
        if(discountProducer != null && dayFilter != 2) {
            /** @Claude OPT-6: Usa totalWeightFortnight pre-calculado en vez de recalcular por zona **/
            Double totalWeightFortnightGAB = collectedRawMaterialCalculatorService.calculateCollectedAmountBetweenDates(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate(), rawMaterialPayRoll.getMetaProduct(), rawMaterialPayRoll.getProductiveZone(), dayFilter);
            Double percentageReserveGAB = ((totalWeightFortnightGAB * 100) / totalWeightFortnight) / 100;
            totalReservaGAB = (RoundUtil.getRoundValue(totalWeightFortnight * discountProducer.getReserve(), 2, RoundUtil.RoundMode.SYMMETRIC) * rawMaterialPayRoll.getUnitPrice()) * percentageReserveGAB;
        }

        Map<Date, Double> differences = createMapOfDifferencesWeights(rawMaterialPayRoll, dayFilter);

        /** @Claude OPT-3: Pre-carga batch de ProducerTax para evitar lazy loading N+1 **/
        Map<Long, ProducerTax> producerTaxCache = preloadProducerTaxes(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate());

        Map<Long, Aux> map = createMapOfProducers(rawMaterialPayRoll, differences, totalReservaGAB, discountProducer, producerTaxCache, dayFilter);
        Double alcoholByGAB = (dayFilter == 2) ? 0.0 : salaryMovementGABService.getAlcoholBayGAB(rawMaterialPayRoll.getProductiveZone(), rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate());

        /** @Claude OPT-4: Pre-carga batch de descuentos por zona en vez de por productor **/
        Map<Long, RawMaterialProducerDiscount> discountsBatch = (dayFilter == 2)
                ? new HashMap<Long, RawMaterialProducerDiscount>()
                : salaryMovementProducerService.prepareDiscountsBatch(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate(), rawMaterialPayRoll.getProductiveZone());

        Double totalAmountCollected = 0.0;
        Double totalPayCollected = 0.0;
        Double totalRetention = 0.0;
        Double totalAlcohol = 0.0;
        Double totalConcentrated = 0.0;
        Double totalCommission = 0.0;
        Double totalCredit = 0.0;
        Double totalVeterinary = 0.0;
        Double totalYogurt = 0.0;
        Double totalCans = 0.0;
        Double totalIncome = 0.0;
        Double totalAdjustment = 0.0;
        Double totalReserve = 0.0;
        Double totalGA = 0.0;
        Double totalOtherDiscount = 0.0;
        Double auxcollectedAmount = 0.0;
        Double auxadjustmentAmount = 0.0;
        Double auxearnedMoney = 0.0;
        Double auxwithholdingTax = 0.0;
        Double auxcollectedTotalMoney = 0.0;
        Double alcoholDiff = 0.0;
        for (Aux aux : map.values()) {
            RawMaterialPayRecord record = new RawMaterialPayRecord();
            auxcollectedAmount = aux.collectedAmount;
            record.setTotalAmount(RoundUtil.getRoundValue(auxcollectedAmount, 2, RoundUtil.RoundMode.SYMMETRIC));
            auxadjustmentAmount = aux.adjustmentAmount;
            record.setProductiveZoneAdjustment(RoundUtil.getRoundValue(auxadjustmentAmount, 2, RoundUtil.RoundMode.SYMMETRIC));
            auxearnedMoney = aux.earnedMoney;
            record.setEarnedMoney(RoundUtil.getRoundValue(auxearnedMoney, 2, RoundUtil.RoundMode.SYMMETRIC));
            auxearnedMoney = aux.earnedMoney;
            auxcollectedTotalMoney = aux.collectedTotalMoney;
            record.setTotalPayCollected(RoundUtil.getRoundValue(rawMaterialPayRoll.getUnitPrice() * auxcollectedAmount, 2, RoundUtil.RoundMode.SYMMETRIC));

            /** @Claude OPT-3: Usa cache pre-cargado de ProducerTax en vez de lazy loading **/
            ProducerTax producerTax = producerTaxCache.get(aux.producer.getId());
            String codTaxLicence = producerTax != null ? producerTax.getFormNumber(): null;
            Date taxStartDate = producerTax != null ? producerTax.getGestionTax().getStartDate():null;
            Date taxEndDate = producerTax != null ? producerTax.getGestionTax().getEndDate():null;
            if (isValidLicence(codTaxLicence, taxStartDate, taxEndDate)) {
                record.setTaxLicense(codTaxLicence);
                record.setExpirationDateTaxLicence(taxStartDate);
                record.setStartDateTaxLicence(taxEndDate);
            }

            /** @Claude OPT-4: Usa descuentos pre-cargados por zona en vez de query individual **/
            RawMaterialProducerDiscount discount = discountsBatch.get(aux.producer.getId());
            if (discount == null) {
                discount = new RawMaterialProducerDiscount();
                discount.setConcentrated(0.0);
                discount.setCommission(0.0);
                discount.setYogurt(0.0);
                discount.setVeterinary(0.0);
                discount.setCredit(0.0);
                discount.setCans(0.0);
                discount.setOtherDiscount(0.0);
                discount.setOtherIncoming(0.0);
            }
            discount.setRawMaterialProducer(aux.producer);
            alcoholDiff += ((alcoholByGAB * (aux.procentaje)) - RoundUtil.getRoundValue(alcoholByGAB * (aux.procentaje), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setAlcohol(RoundUtil.getRoundValue(alcoholByGAB * (aux.procentaje), 2, RoundUtil.RoundMode.SYMMETRIC));
            auxwithholdingTax = aux.withholdingTax;
            discount.setWithholdingTax(RoundUtil.getRoundValue(auxwithholdingTax, 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setRawMaterialPayRecord(record);
            record.setRawMaterialProducerDiscount(discount);
            record.setDiscountReserve(aux.reserveDiscount);
            record.setDiscountGA(RoundUtil.getRoundValue(aux.discountGA, 2, RoundUtil.RoundMode.SYMMETRIC));

            rawMaterialPayRoll.getRawMaterialPayRecordList().add(record);
            record.setRawMaterialPayRoll(rawMaterialPayRoll);
            totalAmountCollected += auxcollectedAmount;
            totalAdjustment += auxadjustmentAmount;
            totalPayCollected += auxcollectedTotalMoney;
            totalRetention += auxwithholdingTax;
            totalReserve += aux.reserveDiscount;
            totalGA += record.getDiscountGA();

            totalCredit += discount.getCredit();
            totalAlcohol += discount.getAlcohol();
            totalConcentrated += discount.getConcentrated();
            totalCommission += discount.getCommission();
            totalVeterinary += discount.getVeterinary();
            totalYogurt += discount.getYogurt();
            totalCans += discount.getCans();
            totalOtherDiscount += discount.getOtherDiscount();
            totalIncome += discount.getOtherIncoming();

            /** @Claude OPT-5: Eliminado findById+update redundante - DiscountReserve se persiste directamente en applyProrations **/

        }
        alcoholDiff = RoundUtil.getRoundValue(alcoholDiff, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalAlcohol += alcoholDiff;
        totalAmountCollected = RoundUtil.getRoundValue(totalAmountCollected, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalPayCollected = RoundUtil.getRoundValue(totalPayCollected, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalRetention = RoundUtil.getRoundValue(totalRetention, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalReserve = RoundUtil.getRoundValue(totalReserve, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalCredit = RoundUtil.getRoundValue(totalCredit, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalAlcohol = RoundUtil.getRoundValue(totalAlcohol, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalConcentrated = RoundUtil.getRoundValue(totalConcentrated, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalCommission = RoundUtil.getRoundValue(totalCommission, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalVeterinary = RoundUtil.getRoundValue(totalVeterinary, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalYogurt = RoundUtil.getRoundValue(totalYogurt, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalCans = RoundUtil.getRoundValue(totalCans, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalOtherDiscount = RoundUtil.getRoundValue(totalOtherDiscount, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalAdjustment = RoundUtil.getRoundValue(totalAdjustment, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalIncome = RoundUtil.getRoundValue(totalIncome, 2, RoundUtil.RoundMode.SYMMETRIC);
        totalGA = RoundUtil.getRoundValue(totalGA, 2, RoundUtil.RoundMode.SYMMETRIC);

        if (alcoholDiff != 0) {
            Double aux = rawMaterialPayRoll.getRawMaterialPayRecordList().get(0).getRawMaterialProducerDiscount().getAlcohol();
            rawMaterialPayRoll.getRawMaterialPayRecordList().get(0).getRawMaterialProducerDiscount().setAlcohol(aux + alcoholDiff);
        }

        calculateLiquidPayable(rawMaterialPayRoll);
        rawMaterialPayRoll.setTotalCollectedByGAB(totalAmountCollected);
        rawMaterialPayRoll.setTotalMountCollectdByGAB(totalPayCollected);
        rawMaterialPayRoll.setTotalRetentionGAB(totalRetention);
        rawMaterialPayRoll.setTotalReserveDicount(totalReserve);
        rawMaterialPayRoll.setTotalCreditByGAB(totalCredit);
        rawMaterialPayRoll.setTotalAlcoholByGAB(totalAlcohol);
        rawMaterialPayRoll.setTotalConcentratedByGAB(totalConcentrated);
        rawMaterialPayRoll.setTotalVeterinaryByGAB(totalVeterinary);
        rawMaterialPayRoll.setTotalYogourdByGAB(totalYogurt);
        rawMaterialPayRoll.setTotalRecipByGAB(totalCans);
        rawMaterialPayRoll.setTotalOtherDiscountByGAB(totalOtherDiscount);
        rawMaterialPayRoll.setTotalAdjustmentByGAB(totalAdjustment);
        rawMaterialPayRoll.setTotalOtherIncomeByGAB(totalIncome);
        rawMaterialPayRoll.setTotalGA(totalGA);
        rawMaterialPayRoll.setTotalCommission(totalCommission);
        return rawMaterialPayRoll;
    }

    private ProducerTax getProducerTaxValid(RawMaterialProducer producer, Date startDate, Date endDate) {
        for(ProducerTax producerTax:producer.getProducerTaxes())
        {
            if(producerTax.getGestionTax().getEndDate().compareTo(endDate) >= 0)
                if(producerTax.getGestionTax().getStartDate().compareTo(startDate) <= 0)
                    return producerTax;
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public DiscountProducer findDiscountProducerByDate(Date date) {
        List<DiscountProducer> discountProducers = new ArrayList<DiscountProducer>();
        try {
            discountProducers = (List<DiscountProducer>) getEntityManager().createQuery(" SELECT discountProducer from DiscountProducer discountProducer " +
                    " where discountProducer.startDate <= :date " +
                    " and discountProducer.endDate >= :date " +
                    " and discountProducer.state = 'ENABLE'")
                    .setParameter("date", date,TemporalType.DATE)
                    .getResultList();
        }catch(NoResultException e){
            return null;
        }
        if(discountProducers.size() == 0)
            return null;

        return discountProducers.get(0);
    }

    public List<BoletaPagoProductor> findBoletaDePago(Date fechaIni,Date fechaFin, RawMaterialProducer rawMaterialProducer,ProductiveZone productiveZone,MetaProduct metaProduct){
        List<Object[]> datos= new ArrayList<Object[]>();
        List<BoletaPagoProductor> boletaPagoProductors = new ArrayList<BoletaPagoProductor>();
        if(rawMaterialProducer != null && productiveZone != null)
            datos = getEntityManager().createQuery(consultaBoletaDePago(rawMaterialProducer,productiveZone))
                    .setParameter("fechaIni",fechaIni,TemporalType.DATE)
                    .setParameter("fechaFin",fechaFin,TemporalType.DATE)
                    .setParameter("rawMaterialProducer",rawMaterialProducer)
                    .setParameter("productiveZone",productiveZone)
                    .setParameter("metaProduct",metaProduct)
                    .getResultList();
        else{
            if(rawMaterialProducer != null)
            datos = getEntityManager().createQuery(consultaBoletaDePago(rawMaterialProducer, productiveZone))
                    .setParameter("fechaIni",fechaIni,TemporalType.DATE)
                    .setParameter("fechaFin",fechaFin,TemporalType.DATE)
                    .setParameter("rawMaterialProducer",rawMaterialProducer)
                    .setParameter("metaProduct", metaProduct)
                    .getResultList();
            if(productiveZone != null)
                datos = getEntityManager().createQuery(consultaBoletaDePago(rawMaterialProducer, productiveZone))
                        .setParameter("fechaIni",fechaIni,TemporalType.DATE)
                        .setParameter("fechaFin",fechaFin,TemporalType.DATE)
                        .setParameter("productiveZone", productiveZone)
                        .setParameter("metaProduct",metaProduct)
                        .getResultList();
            if(rawMaterialProducer == null && productiveZone == null)
                datos = getEntityManager().createQuery(consultaBoletaDePago(rawMaterialProducer, productiveZone))
                        .setParameter("fechaIni",fechaIni,TemporalType.DATE)
                        .setParameter("fechaFin",fechaFin,TemporalType.DATE)
                        .setParameter("metaProduct", metaProduct)
                        .getResultList();
        }

        for(Object[] dato:datos)
        {
            BoletaPagoProductor boletaPagoProductor = new BoletaPagoProductor();
            boletaPagoProductor.setNombrecompletoProductor((String)dato[1] +" "+ (String)dato[2]+" "+ (String)dato[3]);
            boletaPagoProductor.setNombreGAB((String)dato[18]);
            boletaPagoProductor.setTotalLitrosLeche((Double)dato[4]);
            boletaPagoProductor.setPrecioLeche((Double)dato[5]);
            boletaPagoProductor.setTotalBrutoBs((Double)dato[6]);
            boletaPagoProductor.setRetencion((Double)dato[7]);
            boletaPagoProductor.setReserva((Double)dato[19]);
            boletaPagoProductor.setAlcohol((Double)dato[8]);
            boletaPagoProductor.setConcentrados((Double)dato[9]);
            boletaPagoProductor.setCredito((Double)dato[10]);
            boletaPagoProductor.setVeterinario((Double)dato[11]);
            boletaPagoProductor.setYogurt((Double)dato[12]);
            boletaPagoProductor.setTachos((Double)dato[13]);
            boletaPagoProductor.setAjustes((Double)dato[15]);
            boletaPagoProductor.setOtrosDescuentos((Double) dato[14]);
            boletaPagoProductor.setOtrosIngresos((Double)dato[16]);
            boletaPagoProductor.setLiquidoPagable((Double)dato[17]);
            boletaPagoProductor.setCi((String)dato[20]);
            boletaPagoProductor.setDescuentoGA((Double)dato[21]);
            boletaPagoProductor.setNumerocuenta((String)dato[22]);
            boletaPagoProductor.setComision((Double)dato[23]);

            boletaPagoProductors.add(boletaPagoProductor);
        }
        return boletaPagoProductors;
    }

    public List<BoletaPagoProductor> findBoletaDePagoGA(Date fechaIni,Date fechaFin, RawMaterialProducer rawMaterialProducer,ProductiveZone productiveZone,MetaProduct metaProduct){
        List<Object[]> datos= new ArrayList<Object[]>();
        List<BoletaPagoProductor> boletaPagoProductors = new ArrayList<BoletaPagoProductor>();
        if(rawMaterialProducer != null && productiveZone != null) {
            datos = getEntityManager().createQuery(consultaBoletaDePagoGA(rawMaterialProducer, productiveZone))
                    .setParameter("fechaIni", fechaIni, TemporalType.DATE)
                    .setParameter("fechaFin", fechaFin, TemporalType.DATE)
                    .setParameter("rawMaterialProducer", rawMaterialProducer)
                    .setParameter("productiveZone", productiveZone)
                    .setParameter("metaProduct", metaProduct)
                    .getResultList();
        } else {
            if(rawMaterialProducer != null)
                datos = getEntityManager().createQuery(consultaBoletaDePagoGA(rawMaterialProducer, productiveZone))
                        .setParameter("fechaIni",fechaIni,TemporalType.DATE)
                        .setParameter("fechaFin",fechaFin,TemporalType.DATE)
                        .setParameter("rawMaterialProducer",rawMaterialProducer)
                        .setParameter("metaProduct", metaProduct)
                        .getResultList();
            if(productiveZone != null)
                datos = getEntityManager().createQuery(consultaBoletaDePagoGA(rawMaterialProducer, productiveZone))
                        .setParameter("fechaIni",fechaIni,TemporalType.DATE)
                        .setParameter("fechaFin",fechaFin,TemporalType.DATE)
                        .setParameter("productiveZone", productiveZone)
                        .setParameter("metaProduct",metaProduct)
                        .getResultList();
            if(rawMaterialProducer == null && productiveZone == null)
                datos = getEntityManager().createQuery(consultaBoletaDePagoGA(rawMaterialProducer, productiveZone))
                        .setParameter("fechaIni",fechaIni,TemporalType.DATE)
                        .setParameter("fechaFin",fechaFin,TemporalType.DATE)
                        .setParameter("metaProduct", metaProduct)
                        .getResultList();
        }

        for(Object[] dato:datos)
        {
            BoletaPagoProductor boletaPagoProductor = new BoletaPagoProductor();
            boletaPagoProductor.setNombrecompletoProductor((String)dato[1] +" "+ (String)dato[2]+" "+ (String)dato[3]);
            boletaPagoProductor.setNombreGAB((String)dato[18]);
            boletaPagoProductor.setTotalLitrosLeche((Double)dato[4]);
            boletaPagoProductor.setPrecioLeche((Double)dato[5]);
            boletaPagoProductor.setTotalBrutoBs((Double)dato[6]);
            boletaPagoProductor.setRetencion((Double)dato[7]);
            boletaPagoProductor.setReserva((Double)dato[19]);
            boletaPagoProductor.setAlcohol((Double)dato[8]);
            boletaPagoProductor.setConcentrados((Double)dato[9]);
            boletaPagoProductor.setCredito((Double)dato[10]);
            boletaPagoProductor.setVeterinario((Double)dato[11]);
            boletaPagoProductor.setYogurt((Double)dato[12]);
            boletaPagoProductor.setTachos((Double)dato[13]);
            boletaPagoProductor.setAjustes((Double)dato[15]);
            boletaPagoProductor.setOtrosDescuentos((Double) dato[14]);
            boletaPagoProductor.setOtrosIngresos((Double)dato[16]);
            boletaPagoProductor.setLiquidoPagable((Double)dato[17]);
            boletaPagoProductor.setCi((String)dato[20]);
            boletaPagoProductor.setDescuentoGA((Double)dato[21]);

            boletaPagoProductors.add(boletaPagoProductor);
        }

        return boletaPagoProductors;
    }

    private String consultaBoletaDePago(RawMaterialProducer rawMaterialProducer,ProductiveZone productiveZone){

        String query= "SELECT " +
                " rawMaterialPayRecord.id, " +
                " rawMaterialProducer.firstName, " +
                " rawMaterialProducer.lastName, " +
                " rawMaterialProducer.maidenName, " +
                " RawMaterialPayRecord.totalAmount, " +
                " rawMaterialPayRoll.unitPrice, " +
                " RawMaterialPayRecord.totalPayCollected, " +
                " rawMaterialProducerDiscount.withholdingTax, " +
                " rawMaterialProducerDiscount.alcohol, " +
                " rawMaterialProducerDiscount.concentrated, " +
                " rawMaterialProducerDiscount.credit, " +
                " rawMaterialProducerDiscount.veterinary, " +
                " rawMaterialProducerDiscount.yogurt, " +
                " rawMaterialProducerDiscount.cans, " +
                " rawMaterialProducerDiscount.otherDiscount, " +
                " rawMaterialPayRecord.productiveZoneAdjustment, " +
                " rawMaterialProducerDiscount.otherIncoming, " +
                " rawMaterialPayRecord.liquidPayable, " +
                " productiveZone.name, " +
                " rawMaterialPayRecord.discountReserve, " +
                " rawMaterialProducer.idNumber, " +
                " rawMaterialPayRecord.discountGA, " +
                " rawMaterialProducer.accountNumber, " +
                " rawMaterialProducerDiscount.commission " +
                " FROM RawMaterialPayRoll rawMaterialPayRoll " +
                " inner join RawMaterialPayRoll.rawMaterialPayRecordList rawMaterialPayRecord " +
                " inner join rawMaterialPayRecord.rawMaterialProducerDiscount rawMaterialProducerDiscount " +
                " inner join rawMaterialProducerDiscount.rawMaterialProducer rawMaterialProducer " +
                " inner join RawMaterialPayRoll.productiveZone productiveZone " +
                " where rawMaterialPayRoll.startDate =:fechaIni" +
                " and rawMaterialPayRoll.endDate =:fechaFin" +
                " and rawMaterialPayRecord.liquidPayable > 0" +
                " and rawMaterialPayRoll.metaProduct =:metaProduct";
        if(rawMaterialProducer!=null)
        {
            query += " and rawMaterialProducer =:rawMaterialProducer";
        }

        if(productiveZone!=null)
        {
            query += " and productiveZone =:productiveZone";
        }
        query +=" order by rawMaterialProducer.firstName asc";
        return query;
    }

    private String consultaBoletaDePagoGA(RawMaterialProducer rawMaterialProducer,ProductiveZone productiveZone){

        /*String query= "SELECT " +
                " rawMaterialPayRecord.id, " +
                " rawMaterialProducer.firstName, " +
                " rawMaterialProducer.lastName, " +
                " rawMaterialProducer.maidenName, " +
                " RawMaterialPayRecord.totalAmount, " +
                " rawMaterialPayRoll.unitPrice, " +
                " RawMaterialPayRecord.totalPayCollected, " +
                " rawMaterialProducerDiscount.withholdingTax, " +
                " rawMaterialProducerDiscount.alcohol, " +
                " rawMaterialProducerDiscount.concentrated, " +
                " rawMaterialProducerDiscount.credit, " +
                " rawMaterialProducerDiscount.veterinary, " +
                " rawMaterialProducerDiscount.yogurt, " +
                " rawMaterialProducerDiscount.cans, " +
                " rawMaterialProducerDiscount.otherDiscount, " +
                " rawMaterialPayRecord.productiveZoneAdjustment, " +
                " rawMaterialProducerDiscount.otherIncoming, " +
                " rawMaterialPayRecord.liquidPayable, " +
                " productiveZone.name, " +
                " rawMaterialPayRecord.discountReserve, " +
                " rawMaterialProducer.idNumber, " +
                " rawMaterialPayRecord.discountGA " +
                " FROM RawMaterialPayRoll rawMaterialPayRoll " +
                " inner join RawMaterialPayRoll.rawMaterialPayRecordList rawMaterialPayRecord " +
                " inner join rawMaterialPayRecord.rawMaterialProducerDiscount rawMaterialProducerDiscount " +
                " inner join rawMaterialProducerDiscount.rawMaterialProducer rawMaterialProducer " +
                " inner join RawMaterialPayRoll.productiveZone productiveZone " +
                " where rawMaterialPayRoll.startDate >= :fechaIni" +
                " and rawMaterialPayRoll.endDate <= :fechaFin" +
                " and rawMaterialPayRecord.liquidPayable > 0" +
                " and rawMaterialPayRoll.metaProduct =:metaProduct";*/

        String query= "SELECT " +
                " 01, " +
                " rawMaterialProducer.firstName, " +
                " rawMaterialProducer.lastName, " +
                " rawMaterialProducer.maidenName, " +
                " sum(RawMaterialPayRecord.totalAmount) as totalAmount, " +
                " rawMaterialPayRoll.unitPrice, " +
                " sum(RawMaterialPayRecord.totalPayCollected) as totalPayCollected, " +
                " sum(rawMaterialProducerDiscount.withholdingTax) as withholdingTax, " +
                " sum(rawMaterialProducerDiscount.alcohol) as alcohol, " +
                " sum(rawMaterialProducerDiscount.concentrated) as concentrated, " +
                " sum(rawMaterialProducerDiscount.credit) as credit, " +
                " sum(rawMaterialProducerDiscount.veterinary) as veterinary, " +
                " sum(rawMaterialProducerDiscount.yogurt) as yogurt, " +
                " sum(rawMaterialProducerDiscount.cans) as cans, " +
                " sum(rawMaterialProducerDiscount.otherDiscount) as otherDiscount, " +
                " sum(rawMaterialPayRecord.productiveZoneAdjustment) as productiveZoneAdjustment, " +
                " sum(rawMaterialProducerDiscount.otherIncoming) as otherIncoming, " +
                " sum(rawMaterialPayRecord.liquidPayable) as liquidPayable, " +
                " productiveZone.name, " +
                " sum(rawMaterialPayRecord.discountReserve) as discountReserve, " +
                " rawMaterialProducer.idNumber, " +
                " sum(rawMaterialPayRecord.discountGA) as discountGA " +
                " FROM RawMaterialPayRoll rawMaterialPayRoll " +
                " inner join RawMaterialPayRoll.rawMaterialPayRecordList rawMaterialPayRecord " +
                " inner join rawMaterialPayRecord.rawMaterialProducerDiscount rawMaterialProducerDiscount " +
                " inner join rawMaterialProducerDiscount.rawMaterialProducer rawMaterialProducer " +
                " inner join RawMaterialPayRoll.productiveZone productiveZone " +
                " where rawMaterialPayRoll.startDate >= :fechaIni" +
                " and rawMaterialPayRoll.endDate <= :fechaFin" +
                " and rawMaterialPayRecord.liquidPayable > 0" +
                " and rawMaterialPayRoll.metaProduct =:metaProduct " +
                " group by rawMaterialProducer.firstName, rawMaterialProducer.lastName, rawMaterialProducer.maidenName, rawMaterialPayRoll.unitPrice, productiveZone.name, rawMaterialProducer.idNumber ";

        if(rawMaterialProducer!=null){
            query += " and rawMaterialProducer =:rawMaterialProducer";
        }
        if(productiveZone!=null){
            query += " and productiveZone =:productiveZone";
        }
        query +=" order by rawMaterialProducer.firstName asc";
        return query;
    }

    public List<DiscountProducer> findDiscountsProducerByDate(Date date) {
        List<DiscountProducer> discountProducers = new ArrayList<DiscountProducer>();
        try {
            discountProducers = (List<DiscountProducer>) getEntityManager().createQuery(" SELECT discountProducer from DiscountProducer discountProducer " +
                    " where discountProducer.startDate <= :date " +
                    " and discountProducer.endDate >= :date " +
                    " and discountProducer.state = 'ENABLE'")
                    .setParameter("date", date,TemporalType.DATE)
                    .getResultList();
        }catch(NoResultException e){
            return discountProducers;
        }
        if(discountProducers.size() == 0)
            return discountProducers;

        return discountProducers;
    }

    private Map<Date, Double>
    createMapOfCollectedWeights(RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> counts = findTotalCollection("RawMaterialPayRoll.totalCollectedGabBetweenDates", rawMaterialPayRoll);
        Map<Date, Double> countProducers = new HashMap<Date, Double>();

        for (Object[] obj : counts) {
            Date date = (Date) obj[0];
            Double count = (Double) obj[1];

            countProducers.put(date, count);
        }

        return countProducers;
    }

    private Map<Date, Double> createMapOfWeights(RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> counts = findTotalCollection("RawMaterialPayRoll.totalCollectedGabBetweenDates", rawMaterialPayRoll);
        Map<Date, Double> countProducers = new HashMap<Date, Double>();

        for (Object[] obj : counts) {
            Date date = (Date) obj[0];
            Double count = (Double) obj[1];

            countProducers.put(date, count);
        }

        return countProducers;
    }

    public Discounts getDiscounts(Date dateIni, Date dateEnd, ProductiveZone zone, MetaProduct metaProduct) {
        Discounts discounts = new Discounts();

        List<Object[]> datas = getEntityManager().createNamedQuery("RawMaterialPayRoll.getDiscounts")
                .setParameter("startDate", dateIni, TemporalType.DATE)
                .setParameter("endDate", dateEnd, TemporalType.DATE)
                        //.setParameter("productiveZone", zone)
                .setParameter("metaProduct", metaProduct)
                .getResultList();

        discounts.mount = 0.0;
        discounts.collected = 0.0;
        discounts.alcohol = 0.0;
        discounts.concentrated = 0.0;
        discounts.yogurt = 0.0;
        discounts.recip = 0.0;
        discounts.retention = 0.0;
        discounts.veterinary = 0.0;
        discounts.credit = 0.0;
        discounts.discount = 0.0;
        discounts.liquid = 0.0;
        discounts.otherDiscount = 0.0;
        discounts.otherIncome = 0.0;
        discounts.adjustment = 0.0;
        discounts.unitPrice = 0.0;
        discounts.commission = 0.0;
        discounts.reserve = 0.0;
        discounts.ga = 0.0;

        for (Object[] row : datas) {
            discounts.mount += row[0] != null ? (Double) row[0] : 0.0;
            discounts.collected += row[1] != null ? (Double) row[1] : 0.0;
            discounts.alcohol += row[2] != null ? (Double) row[2] : 0.0;
            discounts.concentrated += row[3] != null ? (Double) row[3] : 0.0;
            discounts.yogurt += row[4] != null ? (Double) row[4] : 0.0;
            discounts.recip += row[5] != null ? (Double) row[5] : 0.0;
            discounts.retention += row[6] != null ? (Double) row[6] : 0.0;
            discounts.veterinary += row[7] != null ? (Double) row[7] : 0.0;
            discounts.credit += row[8] != null ? (Double) row[8] : 0.0;
            discounts.discount += row[9] != null ? (Double) row[9] : 0.0;
            discounts.liquid += row[10] != null ? (Double) row[10] : 0.0;
            discounts.otherDiscount += row[11] != null ? (Double) row[11] : 0.0;
            discounts.otherIncome += row[12] != null ? (Double) row[12] : 0.0;
            discounts.adjustment += row[13] != null ? (Double) row[13] : 0.0;
            discounts.commission += row[14] != null ? (Double) row[14] : 0.0;
            if (row[15] != null) discounts.unitPrice = (Double) row[15];
            discounts.reserve += row[16] != null ? (Double) row[16] : 0.0;
            discounts.ga += row[17] != null ? (Double) row[17] : 0.0;
        }

        return discounts;
    }

    public Double getSumAdjustmentFromRecords(Date startDate, Date endDate, MetaProduct metaProduct) {
        List<Double> result = getEntityManager().createQuery(
                "SELECT COALESCE(SUM(r.productiveZoneAdjustment), 0.0) " +
                "FROM RawMaterialPayRecord r " +
                "WHERE r.rawMaterialPayRoll.startDate = :startDate " +
                "AND r.rawMaterialPayRoll.endDate <= :endDate " +
                "AND r.rawMaterialPayRoll.metaProduct = :metaProduct")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .setParameter("metaProduct", metaProduct)
                .getResultList();
        return result.isEmpty() || result.get(0) == null ? 0.0 : result.get(0);
    }

    public SummaryTotal getSumaryTotal(Date dateIni, Date dateEnd, ProductiveZone zone, MetaProduct metaProduct) {
        SummaryTotal summaryTotal = new SummaryTotal();

        List<Object[]> datas = getEntityManager().createNamedQuery("RawMaterialPayRoll.getSumaryTotal")
                .setParameter("startDate", dateIni, TemporalType.DATE)
                .setParameter("endDate", dateEnd, TemporalType.DATE)
                        //.setParameter("productiveZone", zone)
                .setParameter("metaProduct", metaProduct)
                .getResultList();
        //summaryTotal.differencesTotal = ((Double)datas.get(0)[0] !=null) ? (Double)datas.get(0)[0] : 0.0 ;
        /*summaryTotal.balanceWeightTotal = ((Double) datas.get(0)[0] != null) ? (Double) datas.get(0)[0] : 0.0;
        summaryTotal.collectedTotal = ((Double) datas.get(0)[1] != null) ? (Double) datas.get(0)[1] : 0.0;*/
        //Change to MYSQL:
        summaryTotal.balanceWeightTotal = ( datas.get(0)[0] != null) ?  (Integer) datas.get(0)[0] : 0.0;
        summaryTotal.collectedTotal = ( datas.get(0)[1] != null) ? (Integer) datas.get(0)[1] : 0.0;
        return summaryTotal;
    }

    private Map<Date, Double> createMapOfDifferencesWeights(RawMaterialPayRoll rawMaterialPayRoll, int dayFilter) {
        List<Object[]> datas = findDifferencesWeights("RawMaterialPayRoll.differenceRawMaterialBetweenDates", rawMaterialPayRoll);

        Map<Date, Double> differences = new HashMap<Date, Double>();

        for (Object[] obj : datas) {
            Date date = (Date) obj[0];
            if (!shouldIncludeDate(date, dayFilter)) continue;
            Double receivedAmount = (Double) obj[1];
            Double weightedAmount = (Double) obj[2];
            /*Double diffs =  RoundUtil.getRoundValue((receivedAmount.doubleValue() * rawMaterialPayRoll.getUnitPrice()),2, RoundUtil.RoundMode.SYMMETRIC) -
                            RoundUtil.getRoundValue((weightedAmount.doubleValue() * rawMaterialPayRoll.getUnitPrice()),2, RoundUtil.RoundMode.SYMMETRIC);*/
            Double diffs = weightedAmount.doubleValue() * rawMaterialPayRoll.getUnitPrice() - receivedAmount.doubleValue() * rawMaterialPayRoll.getUnitPrice();

            //Double diffs = (receivedAmount.doubleValue() * rawMaterialPayRoll.getUnitPrice()) - (weightedAmount.doubleValue() * rawMaterialPayRoll.getUnitPrice());
            differences.put(date, diffs);
        }
        return differences;
    }

    public Double getBalanceWeightTotal(Double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct) {

        List<Object[]> datas = findWeights("RawMaterialPayRoll.getWeightedAndCollectedBetweenDates", startDate, endDate, metaProduct);
        Double weight = 0.0;

        for (Object[] obj : datas) {
            weight += (Double) obj[1];
        }
        return weight;
    }

    public Double getTotalWeightMoney(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct) {
        List<Object[]> datas = findWeights("RawMaterialPayRoll.getWeightedAndCollectedBetweenDates", startDate, endDate, metaProduct);
        Double totalWeightMoney = 0.0;
        Double weight = 0.0;

        for (Object[] obj : datas) {
            weight += (Double) obj[1];
        }
        totalWeightMoney = weight * unitPrice;
        return totalWeightMoney;
    }

    public Double getTotalMoneyDiff(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct) {
        List<Object[]> datas = findWeights("RawMaterialPayRoll.getWeightedAndCollectedBetweenDates", startDate, endDate, metaProduct);

        Double totalMoneyDiff = 0.0;
        Double weight = 0.0;
        Double collected = 0.0;

        for (Object[] obj : datas) {

            collected += (Double) obj[0];
            weight += (Double) obj[1];
        }

        totalMoneyDiff = (weight - collected) * unitPrice;

        return totalMoneyDiff;
    }

    public Double getTotalDiff(double unitPrice, Date startDate, Date endDate, MetaProduct metaProduct) {
        List<Object[]> datas = findWeights("RawMaterialPayRoll.getWeightedAndCollectedBetweenDates", startDate, endDate, metaProduct);

        Double totalDiff = 0.0;
        Double weight = 0.0;
        Double collected = 0.0;

        for (Object[] obj : datas) {

            collected += (Double) obj[0];
            weight += (Double) obj[1];
        }

        totalDiff = weight - collected;

        return totalDiff;
    }


    public Double getTotalWeightMoney(RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> datas = findWeights("getTotalWeightedAndCollectedBetweenDates", rawMaterialPayRoll);
        Double totalWeightMoney = 0.0;
        Double weight = 0.0;

        for (Object[] obj : datas) {
            weight += (Double) obj[1];
        }
        totalWeightMoney = weight * rawMaterialPayRoll.getUnitPrice();
        return totalWeightMoney;
    }

    public Double getTotalMoneyDiff(RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> datas = findWeights("getTotalWeightedAndCollectedBetweenDates", rawMaterialPayRoll);

        Double totalMoneyDiff = 0.0;
        Double weight = 0.0;
        Double collected = 0.0;

        for (Object[] obj : datas) {

            collected += (Double) obj[0];
            weight += (Double) obj[1];
        }

        totalMoneyDiff = (weight - collected) * rawMaterialPayRoll.getUnitPrice();

        return totalMoneyDiff;
    }

    private List<Object[]> findWeights(String namedQuery, Date startDate, Date endDate, MetaProduct metaProduct) {
        List<Object[]> result = null;

        try {
            result = getEntityManager().createNamedQuery(namedQuery)
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                            //.setParameter("productiveZone", rawMaterialPayRoll.getProductiveZone())
                    .setParameter("metaProduct", metaProduct)
                    .getResultList();
        } catch (Exception e) {

        }
        return result;
    }

    private List<Object[]> findWeights(String namedQuery, RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> result = null;

        try {
            result = getEntityManager().createNamedQuery(namedQuery)
                    .setParameter("startDate", rawMaterialPayRoll.getStartDate())
                    .setParameter("endDate", rawMaterialPayRoll.getEndDate())
                            //.setParameter("productiveZone", rawMaterialPayRoll.getProductiveZone())
                    .setParameter("metaProduct", rawMaterialPayRoll.getMetaProduct())
                    .getResultList();
        } catch (Exception e) {

        }
        return result;
    }

    private List<Object[]> findDifferencesWeights(String namedQuery, RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> result = null;

        try {
            result = getEntityManager().createNamedQuery("RawMaterialPayRoll.differenceRawMaterialBetweenDates")
                    .setParameter("startDate", rawMaterialPayRoll.getStartDate())
                    .setParameter("endDate", rawMaterialPayRoll.getEndDate())
                    .setParameter("productiveZone", rawMaterialPayRoll.getProductiveZone())
                    .setParameter("metaProduct", rawMaterialPayRoll.getMetaProduct())
                    .getResultList();
        } catch (Exception e) {

        }
        return result;
    }

    private List<Object[]> findTotalCollection(String namedQuery, RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> result = null;
        try {
            result = getEntityManager().createNamedQuery(namedQuery)
                    .setParameter("startDate", rawMaterialPayRoll.getStartDate())
                    .setParameter("endDate", rawMaterialPayRoll.getEndDate())
                    .setParameter("productiveZone", rawMaterialPayRoll.getProductiveZone())
                    .setParameter("metaProduct", rawMaterialPayRoll.getMetaProduct())
                    .getResultList();
        } catch (Exception e) {

        }

        return result;
    }

    /** @Claude OPT-2, OPT-3: Parametro producerTaxCache para evitar lazy loading N+1 **/
    private Map<Long, Aux> createMapOfProducers(RawMaterialPayRoll rawMaterialPayRoll, Map<Date, Double> differences, Double totalReservaGAB, DiscountProducer discountProducer, Map<Long, ProducerTax> producerTaxCache, int dayFilter) throws RawMaterialPayRollException {
        double taxRate = rawMaterialPayRoll.getTaxRate() / 100;
        List<Object[]> collectedProducers = find("RawMaterialPayRoll.findCollectedAmountByMetaProductBetweenDates", rawMaterialPayRoll);
        Map<Long, Aux> map = new HashMap<Long, Aux>();
        Double totalMoneyCollectedByGab = 0.0;

        /** @Claude OPT-2: Cache de licencia fiscal por productor para evitar N+1 queries **/
        Map<Long, Boolean> licenseCache = new HashMap<Long, Boolean>();

        for (Object[] obj : collectedProducers) {
            Date date = (Date) obj[0];
            if (!shouldIncludeDate(date, dayFilter)) continue;
            RawMaterialProducer rawMaterialProducer = (RawMaterialProducer) obj[1];
            Double amount = (Double) obj[2];

            Aux aux = map.get(rawMaterialProducer.getId());
            if (aux == null) {
                aux = new Aux();
                aux.producer = rawMaterialProducer;
                map.put(rawMaterialProducer.getId(), aux);
            }

            Double earned = amount * rawMaterialPayRoll.getUnitPrice();

            /** @Claude OPT-2: Usa cache de licencia en vez de recalcular por cada fila **/
            Boolean hasLic = licenseCache.get(rawMaterialProducer.getId());
            if (hasLic == null) {
                /** @Claude OPT-3: Usa ProducerTax pre-cargado en vez de lazy loading **/
                ProducerTax producerTax = producerTaxCache.get(rawMaterialProducer.getId());
                hasLic = hasLicenseFromTax(producerTax);
                licenseCache.put(rawMaterialProducer.getId(), hasLic);
            }
            Double withholding = (dayFilter == 2) ? 0.0 : (hasLic ? 0.0 : earned * taxRate);

            aux.collectedAmount += amount;
            aux.earnedMoney += earned;
            aux.collectedTotalMoney += earned;
            aux.withholdingTax += withholding;
            aux.discountGA += (dayFilter == 2) ? 0.0 : amount * Constants.DISCOUNT_GA;

            totalMoneyCollectedByGab += earned;
        }

        /** @Claude OPT-7: Consolidacion de addProrationAlcohol + addProrationPorcentaje + addReserveDiscountPorcentaje en una sola iteracion **/
        applyProrations(map, rawMaterialPayRoll, totalMoneyCollectedByGab, getDiffMoneyTotalGab(differences), totalReservaGAB, discountProducer);

        // R7. Excluir productores sin acopio: no generar registros ni aplicar descuentos
        Iterator<Aux> it = map.values().iterator();
        while (it.hasNext()) {
            if (it.next().collectedAmount <= 0.0) {
                it.remove();
            }
        }

        return map;
    }

    private boolean shouldIncludeDate(Date date, int dayFilter) {
        if (dayFilter == 0) return true;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        boolean isSunday = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
        if (dayFilter == 1) return !isSunday;
        if (dayFilter == 2) return isSunday;
        return true;
    }

    public Double getDiffTotalMoney(Map<Date, Double> differences) {
        Iterator collections = differences.entrySet().iterator();
        Double totaldiff = 0.0;
        while (collections.hasNext()) {

            Map.Entry thisEntry = (Map.Entry) collections.next();
            Double valor = (Double) thisEntry.getValue();
            totaldiff += valor;
        }

        return RoundUtil.getRoundValue(totaldiff, 2, RoundUtil.RoundMode.SYMMETRIC);
    }

    /**
     * @Claude OPT-3: Pre-carga batch de ProducerTax para evitar lazy loading N+1.
     * Reemplaza las llamadas individuales a getProducerTaxValid() por productor
     * con una sola query SQL que trae todos los registros fiscales validos.
     */
    private Map<Long, ProducerTax> preloadProducerTaxes(Date startDate, Date endDate) {
        List<ProducerTax> taxes = getEntityManager().createQuery(
                "SELECT pt FROM ProducerTax pt JOIN FETCH pt.gestionTax " +
                "WHERE pt.gestionTax.startDate <= :startDate " +
                "AND pt.gestionTax.endDate >= :endDate")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();

        Map<Long, ProducerTax> result = new HashMap<Long, ProducerTax>();
        for (ProducerTax pt : taxes) {
            result.put(pt.getRawMaterialProducerTax().getId(), pt);
        }
        return result;
    }

    /** @Claude OPT-2: Evalua licencia desde ProducerTax pre-cargado sin lazy loading **/
    private boolean hasLicenseFromTax(ProducerTax producerTax) {
        if (producerTax == null)
            return false;
        if (!isValidLicence(producerTax.getFormNumber(), producerTax.getGestionTax().getStartDate(), producerTax.getGestionTax().getEndDate()))
            return false;
        return true;
    }

    /**
     * @Claude OPT-7: Consolida addProrationAlcohol + addProrationPorcentaje + addReserveDiscountPorcentaje
     * en una sola iteracion del mapa de productores. OPT-1: Eliminada query muerta de getRawMaterialCollected.
     * OPT-5: DiscountReserve se persiste directamente con getEntityManager().persist().
     */
    private void applyProrations(Map<Long, Aux> map, RawMaterialPayRoll rawMaterialPayRoll,
            Double totalMoneyCollected, Double totalDifference,
            Double totalReservaGAB, DiscountProducer discountProducer) {
        for (Aux aux : map.values()) {
            Double porcentage = (totalMoneyCollected != 0)
                    ? ((aux.earnedMoney * 100) / totalMoneyCollected) / 100
                    : 0.0;

            // Porcentaje (antes addProrationAlcohol)
            /** @Claude OPT-1: Eliminada query muerta getRawMaterialCollected - resultado nunca se usaba **/
            aux.totaDiffMoney = totalMoneyCollected;
            aux.procentaje = porcentage;

            // Ajuste por diferencia de peso (antes addProrationPorcentaje)
            Double proration = RoundUtil.getRoundValue(totalDifference * porcentage, 2, RoundUtil.RoundMode.SYMMETRIC);
            aux.adjustmentAmount = proration;
            aux.earnedMoney += proration;

            // Descuento de reserva (antes addReserveDiscountPorcentaje)
            if (totalReservaGAB > 0.0 && discountProducer != null) {
                Double reserveProration = RoundUtil.getRoundValue(totalReservaGAB * porcentage, 2, RoundUtil.RoundMode.SYMMETRIC);
                aux.reserveDiscount = reserveProration;
                aux.earnedMoney -= reserveProration;

                /** @Claude OPT-5: Persiste DiscountReserve directamente en vez de cascade via producer **/
                DiscountReserve discountReserve = new DiscountReserve();
                discountReserve.setDiscountProducer(discountProducer);
                discountReserve.setStartDate(rawMaterialPayRoll.getStartDate());
                discountReserve.setEndDate(rawMaterialPayRoll.getEndDate());
                discountReserve.setMaterialProducer(aux.producer);
                discountReserve.setAmount(reserveProration);
                getEntityManager().persist(discountReserve);
            }
        }
    }

    /** @Claude: Metodo legacy conservado para compatibilidad con generateDetails() **/
    private void addProration(Map<Long, Aux> map, RawMaterialPayRoll rawMaterialPayRoll, Map<Date, Double> totalCollectedByGab, Map<Date, Double> differences) throws RawMaterialPayRollException {
        Iterator collections = map.entrySet().iterator();
        while (collections.hasNext()) {

            Map.Entry thisEntry = (Map.Entry) collections.next();
            Aux aux = (Aux) thisEntry.getValue();
            Map<Date, Double> rawMaterialCollected = getRawMaterialCollected(aux.producer, rawMaterialPayRoll);
            Double proration = calculateDelta(rawMaterialCollected, differences, totalCollectedByGab);
            ((Aux) thisEntry.getValue()).adjustmentAmount = proration;
        }
    }

    private Double getDiffMoneyTotalGab(Map<Date, Double> differences) {
        Double total = 0.0;
        Iterator collections = differences.entrySet().iterator();
        while (collections.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) collections.next();
            total += (Double) thisEntry.getValue();
        }
        return total;
    }


    private Double calculateDelta(Map<Date, Double> rawMaterialCollected, Map<Date, Double> differences, Map<Date, Double> totalCollectedByGab) throws RawMaterialPayRollException {
        Iterator collections = rawMaterialCollected.entrySet().iterator();
        Double total = 0.0d;
        Double aux = 0.0d;
        Double differ = 0.0d;
        Double totalBayGab = 0.0d;
        while (collections.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) collections.next();
            Double mountCollected = (Double) thisEntry.getValue();
            Date date = (Date) thisEntry.getKey();
            Double diff = find(differences, date);
            Double totalWeight = find(totalCollectedByGab, date);

            //aux =RoundUtil.getRoundValue(mountCollected * (diff/totalWeight),2, RoundUtil.RoundMode.SYMMETRIC);
            if (totalWeight != 0)
                aux = mountCollected * (diff / totalWeight);
            else
                aux = 0.0;
            differ = (diff - aux);
            totalBayGab = (totalWeight - mountCollected);
            differences.put(date, differ);
            totalCollectedByGab.put(date, totalBayGab);
            total += aux;
            //System.out.println(date.toString() +" : "+ mountCollected.toString()+" * "+ "("+ diff.toString()+"/"+ totalWeight.toString()+") = "+ aux.toString());
            // total = total;
        }
        total = RoundUtil.getRoundValue(total, 2, RoundUtil.RoundMode.SYMMETRIC);
        //System.out.println("Total: "+total.toString());
        return total;
    }

    private Map<Date, Double> getRawMaterialCollected(RawMaterialProducer rawMaterialProducer, RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> datas = findRawMawterilCollected(rawMaterialProducer, rawMaterialPayRoll);
        Map<Date, Double> result = new HashMap<Date, Double>();
        for (Object[] obj : datas) {
            Date date = (Date) obj[0];
            Double count = (Double) obj[1];

            result.put(date, count);
        }
        return result;
    }

    private List<Object[]> findRawMawterilCollected(RawMaterialProducer rawMaterialProducer, RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> result = null;

        try {
            result = getEntityManager().createNamedQuery("RawMaterialPayRoll.getRawMaterialCollentionByProductor")
                    .setParameter("startDate", rawMaterialPayRoll.getStartDate())
                    .setParameter("endDate", rawMaterialPayRoll.getEndDate())
                    .setParameter("rawMaterialProducer", rawMaterialProducer)
                    .setParameter("metaProduct", rawMaterialPayRoll.getMetaProduct())
                    .getResultList();
        } catch (Exception e) {

        }
        return result;
    }

    private <T> T find(Map<Date, T> map, Date date) throws RawMaterialPayRollException {
        T result = map.get(date);
        if (result == null) {
            throw new RawMaterialPayRollException(NO_COLLECTION_ON_DATE, date);
        }
        return result;
    }

    private Map<Date, Double> createMapOfCollectedAmount(RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> collectedTotal = find("RawMaterialPayRoll.findTotalCollectedByMetaProductBetweenDates", rawMaterialPayRoll);
        Map<Date, Double> totalWeight = new HashMap<Date, Double>();
        for (Object[] obj : collectedTotal) {
            Date date = (Date) obj[0];
            Double received = (Double) obj[1];
            Double weighted = (Double) obj[2];

            totalWeight.put(date, weighted - received);
        }
        return totalWeight;
    }

    private Map<Date, Long> createMapOfTotalProducers(RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> counts = find("RawMaterialPayRoll.totalCountProducersByMetaProductBetweenDates", rawMaterialPayRoll);
        Map<Date, Long> countProducers = new HashMap<Date, Long>();
        for (Object[] obj : counts) {
            Date date = (Date) obj[0];
            Long count = (Long) obj[1];

            countProducers.put(date, count);
        }
        return countProducers;
    }

    private boolean hasLicense(RawMaterialPayRecord rawMaterialPayRecord, Date date) {
        if (!isValidLicence(rawMaterialPayRecord.getTaxLicense(), rawMaterialPayRecord.getStartDateTaxLicence(), rawMaterialPayRecord.getExpirationDateTaxLicence()))
            return false;
        if (!isDateInRange(date, rawMaterialPayRecord.getStartDateTaxLicence(), rawMaterialPayRecord.getExpirationDateTaxLicence()))
            return false;

        return true;
    }

    private boolean hasLicense(RawMaterialProducer rawMaterialProducer, Date startDate,Date endDate) {
        ProducerTax producerTax = getProducerTaxValid(rawMaterialProducer,startDate,endDate);
        if(producerTax == null)
            return false;
        if (!isValidLicence(producerTax.getFormNumber(), producerTax.getGestionTax().getStartDate(), producerTax.getGestionTax().getEndDate()))
            return false;

        return true;
    }

    private boolean isDateInRange(Date date, Date start, Date end) {
        if (date.compareTo(start) < 0) return false;
        if (date.compareTo(end) > 0) return false;
        return true;
    }

    private boolean isValidLicence(String license, Date startDate, Date endDate) {
        if (endDate == null) return false;
        if (startDate == null) return false;
        if (startDate.compareTo(endDate) > 0) return false;
        if (StringUtils.isBlank(license)) return false;
        return true;
    }


    private List<Object[]> find(String namedQuery, RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> result = null;
        try {
            result = getEntityManager().createNamedQuery(namedQuery)
                    .setParameter("metaProduct", rawMaterialPayRoll.getMetaProduct())
                    .setParameter("startDate", rawMaterialPayRoll.getStartDate())
                    .setParameter("endDate", rawMaterialPayRoll.getEndDate())
                    .setParameter("productiveZone", rawMaterialPayRoll.getProductiveZone())
                    .getResultList();
        } catch (Exception e) {

        }
        return result;
    }

    public List<DiscountProducer> findDiscountProducerByDate(Date startDate, Date endDate) {
        List<DiscountProducer> discountProducers = new ArrayList<DiscountProducer>();
        try {
            discountProducers = (List<DiscountProducer>) getEntityManager().createQuery(" SELECT discountProducer from DiscountProducer discountProducer " +
                    " where discountProducer.startDate = :startDate " +
                    " and discountProducer.endDate = :endDate " +
                    " and discountProducer.state = 'ENABLE'")
                    .setParameter("startDate", startDate,TemporalType.DATE)
                    .setParameter("endDate", endDate,TemporalType.DATE)
                    .getResultList();
        }catch(NoResultException e){
            return null;
        }

        return discountProducers;
    }



    //region: borrar
    /*
    public List<GeneratedPayroll> findValidGeneratedPayrollsByGestionAndMount(Gestion gestion, Month month) {
        try {
            userTransaction.begin();
            List<GeneratedPayroll> resultList = em.createNamedQuery("GeneratedPayroll.findGeneratedPayrollsByGestionAndType")
                    .setParameter("gestion", gestion)
                    .setParameter("month", month).setParameter("generatedPayrollType", GeneratedPayrollType.OFFICIAL).getResultList();
            userTransaction.commit();
            return resultList;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.debug("Rollback failed", e1);
            }
        }
        return new ArrayList<GeneratedPayroll>();
    }
    */
    //endregion borrar
    class Aux {
        public RawMaterialProducer producer;
        public Double collectedAmount = 0.0;
        public Double adjustmentAmount = 0.0;
        public Double collectedTotalMoney = 0.0;
        public Double earnedMoney = 0.0;
        public Double withholdingTax = 0.0;
        public Double procentaje = 0.0;
        public Double totaDiffMoney = 0.0;
        public Double totalCollected = 0.0;
        public Double totalWeight = 0.0;
        public Double totalCollectedMoney = 0.0;
        public Double totalWeightMoney = 0.0;
        public Double reserveDiscount = 0.0;
        public Double discountGA = 0.0;
    }

    public class Discounts {
        public Double mount;
        public Double collected;
        public Double unitPrice;
        public Double alcohol;
        public Double concentrated;
        public Double yogurt;
        public Double veterinary;
        public Double credit;
        public Double recip;
        public Double discount;
        public Double liquid;
        public Double retention;
        public Double otherDiscount;
        public Double commission;
        public Double otherIncome;
        public Double adjustment;
        public Double reserve;
        public Double ga;
    }

    public class SummaryTotal {
        public Double collectedTotal;
        public Double collectedTotalMoney;
        public Double differencesTotal;
        public Double balanceWeightTotal;
    }

    /*@Override
    public void calculateLiquidPayable(RawMaterialPayRoll rawMaterialPayRoll) {
        Double totalLiquidPay = 0.0;
        for(RawMaterialPayRecord record : rawMaterialPayRoll.getRawMaterialPayRecordList()) {
            RawMaterialProducerDiscount discount = record.getRawMaterialProducerDiscount();
            double totalDiscount = 0.0;
            totalDiscount += RoundUtil.getRoundValue(discount.getAlcohol(),2, RoundUtil.RoundMode.SYMMETRIC);
            totalDiscount += RoundUtil.getRoundValue(discount.getConcentrated(),2, RoundUtil.RoundMode.SYMMETRIC);
            totalDiscount += RoundUtil.getRoundValue(discount.getWithholdingTax(),2, RoundUtil.RoundMode.SYMMETRIC);
            totalDiscount += RoundUtil.getRoundValue(discount.getCans(),2, RoundUtil.RoundMode.SYMMETRIC);
            totalDiscount += RoundUtil.getRoundValue(discount.getCredit(),2, RoundUtil.RoundMode.SYMMETRIC);
            totalDiscount += RoundUtil.getRoundValue(discount.getVeterinary(),2, RoundUtil.RoundMode.SYMMETRIC);
            totalDiscount += RoundUtil.getRoundValue(discount.getYogurt(),2, RoundUtil.RoundMode.SYMMETRIC);
            totalDiscount += RoundUtil.getRoundValue(discount.getOtherDiscount(),2, RoundUtil.RoundMode.SYMMETRIC);
            double liquidPayable = record.getEarnedMoney() - totalDiscount + discount.getOtherIncoming();
            totalLiquidPay += liquidPayable;
            record.setLiquidPayable(RoundUtil.getRoundValue(liquidPayable,2, RoundUtil.RoundMode.SYMMETRIC));
        }
        rawMaterialPayRoll.setTotalLiquidByGAB(RoundUtil.getRoundValue(totalLiquidPay,2, RoundUtil.RoundMode.SYMMETRIC));
    }*/

    @Override
    public void calculateLiquidPayable(RawMaterialPayRoll rawMaterialPayRoll) {
        BigDecimal totalLiquidPay = BigDecimal.ZERO;
        for (RawMaterialPayRecord record : rawMaterialPayRoll.getRawMaterialPayRecordList()) {
            RawMaterialProducerDiscount discount = record.getRawMaterialProducerDiscount();
            BigDecimal totalDiscount = BigDecimal.ZERO;
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getAlcohol()));
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getConcentrated()));
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getWithholdingTax()));
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getCans()));
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getCredit()));
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getVeterinary()));
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getYogurt()));
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getOtherDiscount()));
            totalDiscount = totalDiscount.add(BigDecimal.valueOf(discount.getCommission()));

            BigDecimal liquidPayable = BigDecimal.valueOf(record.getEarnedMoney())
                    .subtract(totalDiscount)
                    .add(BigDecimal.valueOf(discount.getOtherIncoming()))
                    .subtract(BigDecimal.valueOf(record.getDiscountGA()));

            record.setLiquidPayable(liquidPayable.setScale(2, RoundingMode.HALF_UP).doubleValue());
            totalLiquidPay = totalLiquidPay.add(liquidPayable);
        }

        rawMaterialPayRoll.setTotalLiquidByGAB(totalLiquidPay.setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    public RawMaterialPayRoll getTotalsRawMaterialPayRoll(Date dateIni, Date dateEnd, ProductiveZone productiveZone, MetaProduct metaProduct) {

        String query = createQuery(productiveZone, metaProduct);
        RawMaterialPayRoll rawMaterialPayRoll = new RawMaterialPayRoll();
        Query queryObj = getEntityManager().createQuery(query)
                .setParameter("startDate", dateIni, TemporalType.DATE)
                .setParameter("endDate", dateEnd, TemporalType.DATE);
        if (productiveZone != null)
            queryObj.setParameter("productiveZone", productiveZone);
        if (metaProduct != null)
            queryObj.setParameter("metaProduct", metaProduct);

        try {
            /*List<Object[]> datas = getEntityManager().createNamedQuery("RawMaterialPayRoll.getTotalsRawMaterialPayRoll")
                                                      .setParameter("startDate", dateIni)
                                                      .setParameter("endDate", dateEnd)
                                                      .setParameter("productiveZone", productiveZone)
                                                      //.setParameter("metaProduct",metaProduct)
                                                      .getResultList();*/
            List<Object[]> datas = queryObj.getResultList();
            rawMaterialPayRoll.setTotalCollectedByGAB((Double) (datas.get(0)[0]));
            rawMaterialPayRoll.setTotalMountCollectdByGAB((Double) (datas.get(0)[1]));
            rawMaterialPayRoll.setTotalRetentionGAB((Double) (datas.get(0)[2]));
            rawMaterialPayRoll.setTotalCreditByGAB((Double) (datas.get(0)[3]));
            rawMaterialPayRoll.setTotalVeterinaryByGAB((Double) (datas.get(0)[4]));
            rawMaterialPayRoll.setTotalAlcoholByGAB((Double) (datas.get(0)[5]));
            rawMaterialPayRoll.setTotalConcentratedByGAB((Double) (datas.get(0)[6]));
            rawMaterialPayRoll.setTotalYogourdByGAB((Double) (datas.get(0)[7]));
            rawMaterialPayRoll.setTotalRecipByGAB((Double) (datas.get(0)[8]));
            rawMaterialPayRoll.setTotalDiscountByGAB((Double) (datas.get(0)[9]));
            rawMaterialPayRoll.setTotalAdjustmentByGAB((Double) (datas.get(0)[10]));
            rawMaterialPayRoll.setTotalOtherIncomeByGAB((Double) (datas.get(0)[11]));
            rawMaterialPayRoll.setTotalLiquidByGAB((Double) (datas.get(0)[12]));
            //rawMaterialPayRoll.setProductiveZone((ProductiveZone) (datas.get(0)[13]));
            rawMaterialPayRoll.setUnitPrice((Double) (datas.get(0)[14]));
            rawMaterialPayRoll.setTotalReserveDicount((Double) (datas.get(0)[15]));
            rawMaterialPayRoll.setIue((Double) (datas.get(0)[16]));
            rawMaterialPayRoll.setIt((Double) (datas.get(0)[17]));
            rawMaterialPayRoll.setTaxRate((Double) (datas.get(0)[18]));
            rawMaterialPayRoll.setTotalGA((Double) (datas.get(0)[19]));
            rawMaterialPayRoll.setTotalCommission((Double) (datas.get(0)[20]));
        } catch (Exception e) {
            log.debug("Not found totals RawMaterialPayRoll...." + e);
        }

        return rawMaterialPayRoll;
    }

    private String createQuery(ProductiveZone productiveZone, MetaProduct metaProduct) {
        String restricZone = (productiveZone == null) ? "" : " and rawMaterialPayRoll.productiveZone = :productiveZone ";
        String restricMeta = (metaProduct == null) ? "" : " and rawMaterialPayRoll.metaProduct = :metaProduct ";

        return "select " +
                "sum(rawMaterialPayRoll.totalCollectedByGAB), " +
                "sum(rawMaterialPayRoll.totalMountCollectdByGAB), " +
                "sum(rawMaterialPayRoll.totalRetentionGAB), " +
                "sum(rawMaterialPayRoll.totalCreditByGAB), " +
                "sum(rawMaterialPayRoll.totalVeterinaryByGAB), " +
                "sum(rawMaterialPayRoll.totalAlcoholByGAB), " +
                "sum(rawMaterialPayRoll.totalConcentratedByGAB), " +
                "sum(rawMaterialPayRoll.totalYogourdByGAB), " +
                "sum(rawMaterialPayRoll.totalRecipByGAB), " +
                "sum(rawMaterialPayRoll.totalDiscountByGAB)," +
                "sum(rawMaterialPayRoll.totalAdjustmentByGAB)," +
                "sum(rawMaterialPayRoll.totalOtherIncomeByGAB)," +
                "sum(rawMaterialPayRoll.totalLiquidByGAB), " +
                "sum(rawMaterialPayRoll.productiveZone), " +
                "rawMaterialPayRoll.unitPrice, " +
                "sum(rawMaterialPayRoll.totalReserveDicount), " +
                " rawMaterialPayRoll.iue, " +
                " rawMaterialPayRoll.it, " +
                " rawMaterialPayRoll.taxRate, " +
                " sum(rawMaterialPayRoll.totalGA), " +
                " sum(rawMaterialPayRoll.totalCommission) " +
                "from RawMaterialPayRoll rawMaterialPayRoll " +
                "where rawMaterialPayRoll.startDate = :startDate " +
                "and rawMaterialPayRoll.endDate <=  :endDate"
                + restricZone + restricMeta
                + " GROUP BY rawMaterialPayRoll.unitPrice,rawMaterialPayRoll.iue,rawMaterialPayRoll.it,rawMaterialPayRoll.taxRate ";
    }

    @Override
    public List<RawMaterialPayRoll> findAll(Date startDate, Date endDate, MetaProduct metaProduct) {
        List<RawMaterialPayRoll> rawMaterialPayRolls = getEntityManager().createNamedQuery("RawMaterialPayRoll.getMaterialPayRollInDates")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .setParameter("metaProduct", metaProduct)
                .getResultList();
        return rawMaterialPayRolls;
    }

    @Override
    public List<RawMaterialPayRoll> findAllPayRollesByGAB(Date startDate, Date endDate, ProductiveZone productiveZone) {
        List<RawMaterialPayRoll> rawMaterialPayRolls;
        if(productiveZone != null) {
            rawMaterialPayRolls = getEntityManager().createNamedQuery("RawMaterialPayRoll.getPayRollInDatesAndGAB")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .setParameter("productiveZone", productiveZone)
                    .getResultList();
        }
        else{
            rawMaterialPayRolls = getEntityManager().createNamedQuery("RawMaterialPayRoll.getPayRollInDates")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .getResultList();
        }
        return rawMaterialPayRolls;
    }

    @Override
    public void approvedNoteRejection(Calendar startDate, Calendar endDate) {
        getEntityManager().createQuery("update RawMaterialRejectionNote rawMaterialRejectionNote set rawMaterialRejectionNote.state = 'APPROVED'" +
                " where rawMaterialRejectionNote.date between :startDate and :endDate ")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .executeUpdate();
    }

    @Override
    public void approvedDiscounts(Calendar startDate, Calendar endDate, ProductiveZone productiveZone) {
        if(productiveZone != null) {
            getEntityManager().createQuery("update SalaryMovementProducer salaryMovementProducer set salaryMovementProducer.state = 'APPROVED'" +
                    " where salaryMovementProducer.date between :startDate and :endDate " +
                    " and salaryMovementProducer.productiveZone = :productiveZone")
                    .setParameter("startDate",startDate,TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .setParameter("productiveZone", productiveZone)
                    .executeUpdate();
        }else{
            getEntityManager().createQuery("update SalaryMovementProducer salaryMovementProducer set salaryMovementProducer.state = 'APPROVED'" +
                    " where salaryMovementProducer.date between :startDate and :endDate ")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .executeUpdate();
        }
    }

    @Override
    public void approvedReservProductor(Calendar startDate, Calendar endDate) {
        getEntityManager().createQuery("update DiscountProducer discountProducer set discountProducer.state = 'APPROVED'" +
                " where discountProducer.startDate = :startDate" +
                " and discountProducer.endDate = :endDate ")
                .setParameter("startDate", startDate, TemporalType.DATE)
                .setParameter("endDate", endDate, TemporalType.DATE)
                .executeUpdate();
    }

    @Override
    public void approvedDiscountsGAB(Calendar startDate, Calendar endDate, ProductiveZone productiveZone) {
        if(productiveZone != null) {
            getEntityManager().createQuery("update SalaryMovementGAB salaryMovementGAB set salaryMovementGAB.state = 'APPROVED'" +
                    " where salaryMovementGAB.date between :startDate and :endDate " +
                    " and salaryMovementGAB.productiveZone = :productiveZone")
                    .setParameter("startDate",startDate,TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .setParameter("productiveZone", productiveZone)
                    .executeUpdate();
        }else{
            getEntityManager().createQuery("update SalaryMovementGAB salaryMovementGAB set salaryMovementGAB.state = 'APPROVED'" +
                    " where salaryMovementGAB.date between :startDate and :endDate ")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .executeUpdate();
        }
    }

    @Override
    public void approvedRawMaterialPayRoll(Calendar startDate, Calendar endDate, ProductiveZone productiveZone) {
        if(productiveZone != null) {
            getEntityManager().createQuery("update RawMaterialPayRoll rawMaterialPayRoll set rawMaterialPayRoll.state = 'APPROVED'" +
                    " where rawMaterialPayRoll.startDate = :startDate " +
                    " and rawMaterialPayRoll.endDate = :endDate " +
                    " and rawMaterialPayRoll.productiveZone = :productiveZone")
                    .setParameter("startDate",startDate,TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .setParameter("productiveZone", productiveZone)
                    .executeUpdate();
        }else{
            getEntityManager().createQuery("update RawMaterialPayRoll rawMaterialPayRoll set rawMaterialPayRoll.state = 'APPROVED'" +
                    " where rawMaterialPayRoll.startDate = :startDate " +
                    " and rawMaterialPayRoll.endDate = :endDate " )
                    .setParameter("startDate",startDate,TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .executeUpdate();
        }
    }

    @Override
    public Double getReservProducer(Date startDate, Date endDate) {
        BigDecimal result = (BigDecimal)getEntityManager().createNativeQuery("select IFNULL(sum(monto),0.0) from descuentoreserva " +
                "where FECHAINI = :startDate " +
                "and FECHAFIN  = :endDate")
                .setParameter("startDate",startDate,TemporalType.DATE)
                .setParameter("endDate",endDate,TemporalType.DATE )
                .getSingleResult();

        return result.doubleValue();
    }

    @Override
    public void deleteReserveDiscount(Date startDate, Date endDate) {
        getEntityManager().createNativeQuery("delete from descuentoreserva where fechaini = :startDate\n" +
                "and fechafin = :endDate")
                .setParameter("startDate",startDate,TemporalType.DATE)
                .setParameter("endDate",endDate,TemporalType.DATE)
                .executeUpdate();
    }

    @Override
    public List<RawMaterialPayRoll> findAll() {
        List<RawMaterialPayRoll> rawMaterialPayRolls = getEntityManager().createNamedQuery("RawMaterialPayRoll.getAllMaterialPayRoll")
                .getResultList();
        return rawMaterialPayRolls;
    }

    @Override
    public boolean verifDayColected(Calendar date_aux, ProductiveZone zone) {
        List<Object> list = getEntityManager().createQuery("SELECT rawMaterialCollectionSession " +
                "from RawMaterialCollectionSession rawMaterialCollectionSession" +
                " where rawMaterialCollectionSession.date = :date_aux" +
                " and rawMaterialCollectionSession.productiveZone = :zone ")
                .setParameter("date_aux", date_aux.getTime(), TemporalType.DATE)
                .setParameter("zone", zone)
                .getResultList();

        return (list.size() == 0) ? false : true;
    }

    @Override
    public void approvedSession(Calendar startDate, Calendar endDate, ProductiveZone productiveZone) {
        if(productiveZone !=null) {
            getEntityManager().createQuery("update RawMaterialCollectionSession rawMaterialCollectionSession set rawMaterialCollectionSession.state = 'APPROVED'" +
                    " where rawMaterialCollectionSession.date between :startDate and :endDate " +
                    " and rawMaterialCollectionSession.productiveZone = :productiveZone")
                    .setParameter("startDate",startDate,TemporalType.DATE)
                    .setParameter("endDate",endDate,TemporalType.DATE)
                    .setParameter("productiveZone",productiveZone)
                    .executeUpdate();
        }else{
            getEntityManager().createQuery("update RawMaterialCollectionSession rawMaterialCollectionSession set rawMaterialCollectionSession.state = 'APPROVED'" +
                    " where rawMaterialCollectionSession.date between :startDate and :endDate ")
                    .setParameter("startDate", startDate, TemporalType.DATE)
                    .setParameter("endDate", endDate, TemporalType.DATE)
                    .executeUpdate();
        }
    }
}

