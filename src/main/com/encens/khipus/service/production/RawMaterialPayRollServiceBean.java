package com.encens.khipus.service.production;

import com.encens.khipus.exception.ConcurrencyException;
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

    @Override
    public RawMaterialPayRoll generatePayroll(RawMaterialPayRoll rawMaterialPayRoll,DiscountProducer discountProducer) throws EntryNotFoundException, RawMaterialPayRollException {
        Double totalReservaGAB = 0.0;
        if(discountProducer != null) {
            Double totalWeightFortnight = collectedRawMaterialCalculatorService.calculateCollectedAmountBetweenDates(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate(), rawMaterialPayRoll.getMetaProduct());
            Double totalWeightFortnightGAB = collectedRawMaterialCalculatorService.calculateCollectedAmountBetweenDates(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate(), rawMaterialPayRoll.getMetaProduct(), rawMaterialPayRoll.getProductiveZone());
            Double percentageReserveGAB = ((totalWeightFortnightGAB * 100) / totalWeightFortnight) / 100;
            totalReservaGAB = (RoundUtil.getRoundValue(totalWeightFortnight * discountProducer.getReserve(), 2, RoundUtil.RoundMode.SYMMETRIC) * Constants.PRICE_UNIT_MILK) * percentageReserveGAB;//discountProducer.getReserveFortnight() * percentageReserveGAB;
        }

        Map<Date, Double> differences = createMapOfDifferencesWeights(rawMaterialPayRoll);
        Map<Long, Aux> map = createMapOfProducers(rawMaterialPayRoll, differences,totalReservaGAB,discountProducer);
        Double alcoholByGAB = salaryMovementGABService.getAlcoholBayGAB(rawMaterialPayRoll.getProductiveZone(), rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate());

        Double totalAmountCollected = 0.0;
        Double totalPayCollected = 0.0;
        Double totalRetention = 0.0;
        Double totalAlcohol = 0.0;
        Double totalConcentrated = 0.0;
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
            ProducerTax producerTax = getProducerTaxValid(aux.producer,rawMaterialPayRoll.getStartDate(),rawMaterialPayRoll.getEndDate());
            String codTaxLicence = producerTax != null ? producerTax.getFormNumber(): null;
            Date taxStartDate = producerTax != null ? producerTax.getGestionTax().getStartDate():null;
            Date taxEndDate = producerTax != null ? producerTax.getGestionTax().getEndDate():null;
            if (isValidLicence(codTaxLicence, taxStartDate, taxEndDate)) {
                record.setTaxLicense(codTaxLicence);
                record.setExpirationDateTaxLicence(taxStartDate);
                record.setStartDateTaxLicence(taxEndDate);
            }

            RawMaterialProducerDiscount discount = salaryMovementProducerService.prepareDiscount(aux.producer, rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate(),rawMaterialPayRoll.getProductiveZone());
            alcoholDiff += ((alcoholByGAB * (aux.procentaje)) - RoundUtil.getRoundValue(alcoholByGAB * (aux.procentaje), 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setAlcohol(RoundUtil.getRoundValue(alcoholByGAB * (aux.procentaje), 2, RoundUtil.RoundMode.SYMMETRIC));
            auxwithholdingTax = aux.withholdingTax;
            discount.setWithholdingTax(RoundUtil.getRoundValue(auxwithholdingTax, 2, RoundUtil.RoundMode.SYMMETRIC));
            discount.setRawMaterialPayRecord(record);
            record.setRawMaterialProducerDiscount(discount);
            record.setDiscountReserve(aux.reserveDiscount);
            record.setDiscountGA(aux.discountGA);
            //record.setDiscountGA(aux.collectedAmount * Constants.DISCOUNT_GA);

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
            totalVeterinary += discount.getVeterinary();
            totalYogurt += discount.getYogurt();
            totalCans += discount.getCans();
            totalOtherDiscount += discount.getOtherDiscount();
            totalIncome += discount.getOtherIncoming();

            try {
                rawMaterialProducerService.update(rawMaterialProducerService.findById(RawMaterialProducer.class,aux.producer.getId()) );
            } catch (EntryDuplicatedException e) {
                e.printStackTrace();
            } catch (ConcurrencyException e) {
                e.printStackTrace();
            }

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
                " rawMaterialProducer.accountNumber " +
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

        if (datas.size() > 0) {
            discounts.mount = ((Double) datas.get(0)[0] != null) ? ((Double) datas.get(0)[0]).doubleValue() : 0.0;
            discounts.collected = ((Double) datas.get(0)[1] != null) ? ((Double) datas.get(0)[1]).doubleValue() : 0.0;
            discounts.alcohol = ((Double) datas.get(0)[2] != null) ? ((Double) datas.get(0)[2]).doubleValue() : 0.0;
            discounts.concentrated = ((Double) datas.get(0)[3] != null) ? ((Double) datas.get(0)[3]).doubleValue() : 0.0;
            discounts.yogurt = ((Double) datas.get(0)[4] != null) ? ((Double) datas.get(0)[4]).doubleValue() : 0.0;
            discounts.recip = ((Double) datas.get(0)[5] != null) ? ((Double) datas.get(0)[5]).doubleValue() : 0.0;
            discounts.retention = ((Double) datas.get(0)[6] != null) ? ((Double) datas.get(0)[6]).doubleValue() : 0.0;
            discounts.veterinary = ((Double) datas.get(0)[7] != null) ? ((Double) datas.get(0)[7]).doubleValue() : 0.0;
            discounts.credit = ((Double) datas.get(0)[8] != null) ? ((Double) datas.get(0)[8]).doubleValue() : 0.0;
            discounts.discount = ((Double) datas.get(0)[9] != null) ? ((Double) datas.get(0)[9]).doubleValue() : 0.0;
            discounts.liquid = ((Double) datas.get(0)[10] != null) ? ((Double) datas.get(0)[10]).doubleValue() : 0.0;
            discounts.otherDiscount = ((Double) datas.get(0)[11] != null) ? ((Double) datas.get(0)[11]).doubleValue() : 0.0;
            discounts.otherIncome = ((Double) datas.get(0)[12] != null) ? ((Double) datas.get(0)[12]).doubleValue() : 0.0;
            discounts.adjustment = ((Double) datas.get(0)[13] != null) ? ((Double) datas.get(0)[13]).doubleValue() : 0.0;
            discounts.unitPrice = ((Double) datas.get(0)[14] != null) ? ((Double) datas.get(0)[14]).doubleValue() : 0.0;
        } else {
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
        }

        return discounts;
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

    private Map<Date, Double> createMapOfDifferencesWeights(RawMaterialPayRoll rawMaterialPayRoll) {
        List<Object[]> datas = findDifferencesWeights("RawMaterialPayRoll.differenceRawMaterialBetweenDates", rawMaterialPayRoll);

        Map<Date, Double> differences = new HashMap<Date, Double>();

        for (Object[] obj : datas) {
            Date date = (Date) obj[0];
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

    private Map<Long, Aux> createMapOfProducers(RawMaterialPayRoll rawMaterialPayRoll, Map<Date, Double> differences,Double totalReservaGAB,DiscountProducer discountProducer) throws RawMaterialPayRollException {
        double taxRate = rawMaterialPayRoll.getTaxRate() / 100;
        List<Object[]> collectedProducers = find("RawMaterialPayRoll.findCollectedAmountByMetaProductBetweenDates", rawMaterialPayRoll);
        Map<Long, Aux> map = new HashMap<Long, Aux>();
        Double totalMoneyCollectedByGab = 0.0;
        for (Object[] obj : collectedProducers) {
            Date date = (Date) obj[0];
            RawMaterialProducer rawMaterialProducer = (RawMaterialProducer) obj[1];
            Double amount = (Double) obj[2];

            Aux aux = map.get(rawMaterialProducer.getId());
            if (aux == null) {
                aux = new Aux();
                aux.producer = rawMaterialProducer;
                map.put(rawMaterialProducer.getId(), aux);
            }

            Double earned = amount * rawMaterialPayRoll.getUnitPrice();
            //si tiene el registro del impuesto no se le hace el descuento
            Double withholding = (hasLicense(rawMaterialProducer, rawMaterialPayRoll.getStartDate(),rawMaterialPayRoll.getEndDate()) ? 0.0 : earned * taxRate);

            aux.collectedAmount += amount;
            aux.earnedMoney += earned;
            aux.collectedTotalMoney += earned;
            aux.withholdingTax += withholding;
            aux.discountGA += amount * Constants.DISCOUNT_GA;

            totalMoneyCollectedByGab += earned;
        }
        addProrationAlcohol(map, rawMaterialPayRoll, totalMoneyCollectedByGab);
        addProrationPorcentaje(map, rawMaterialPayRoll, totalMoneyCollectedByGab, getDiffMoneyTotalGab(differences));
        if(totalReservaGAB >0.0)
        addReserveDiscountPorcentaje(map,rawMaterialPayRoll,totalReservaGAB,discountProducer,totalMoneyCollectedByGab);
        return map;
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

    private void addProrationAlcohol(Map<Long, Aux> map, RawMaterialPayRoll rawMaterialPayRoll, Double totalMoneyCollected) {
        Iterator collections = map.entrySet().iterator();
        while (collections.hasNext()) {

            Map.Entry thisEntry = (Map.Entry) collections.next();
            Aux aux = (Aux) thisEntry.getValue();
            Map<Date, Double> rawMaterialCollected = getRawMaterialCollected(aux.producer, rawMaterialPayRoll);
            //Double porcentage =RoundUtil.getRoundValue( ((aux.earnedMoney *100)/totalMoneyCollected)/100,2, RoundUtil.RoundMode.SYMMETRIC);
            //todo: lanzar un mensaje de advertencia
            Double porcentage;
            if (totalMoneyCollected != 0)
                porcentage = ((aux.earnedMoney * 100) / totalMoneyCollected) / 100;
            else
                porcentage = 0.0;
            //Double porcentage =RoundUtil.getRoundValue( ((aux.earnedMoney *100)/totalMoneyCollected)/100,5, RoundUtil.RoundMode.SYMMETRIC);
            ((Aux) thisEntry.getValue()).totaDiffMoney = totalMoneyCollected;
            ((Aux) thisEntry.getValue()).procentaje = porcentage;
        }
    }

    private void addProration(Map<Long, Aux> map, RawMaterialPayRoll rawMaterialPayRoll, Map<Date, Double> totalCollectedByGab, Map<Date, Double> differences) throws RawMaterialPayRollException {
        Iterator collections = map.entrySet().iterator();
        while (collections.hasNext()) {

            Map.Entry thisEntry = (Map.Entry) collections.next();
            Aux aux = (Aux) thisEntry.getValue();
            Map<Date, Double> rawMaterialCollected = getRawMaterialCollected(aux.producer, rawMaterialPayRoll);
            Double proration = calculateDelta(rawMaterialCollected, differences, totalCollectedByGab);
            ((Aux) thisEntry.getValue()).adjustmentAmount = proration;
            //((Aux) thisEntry.getValue()).earnedMoney = ((Aux) thisEntry.getValue()).earnedMoney - proration;
        }
    }

    private void addProrationPorcentaje(Map<Long, Aux> map, RawMaterialPayRoll rawMaterialPayRoll, Double totalMoneyCollected, Double totalDiference) {
        Iterator collections = map.entrySet().iterator();
        while (collections.hasNext()) {

            Map.Entry thisEntry = (Map.Entry) collections.next();
            Aux aux = (Aux) thisEntry.getValue();
            //Double porcentage =RoundUtil.getRoundValue( ((aux.earnedMoney *100)/totalMoneyCollected)/100,2, RoundUtil.RoundMode.SYMMETRIC);
            //todo: lanzar un mensaje de advertencia
            Double porcentage;
            if (totalMoneyCollected != 0)
                porcentage = ((aux.earnedMoney * 100) / totalMoneyCollected) / 100;
            else
                porcentage = 0.0;

            Double proration = totalDiference * porcentage;
            ((Aux) thisEntry.getValue()).adjustmentAmount = proration;
            ((Aux) thisEntry.getValue()).earnedMoney = ((Aux) thisEntry.getValue()).earnedMoney + proration;
        }
    }

    private void addReserveDiscountPorcentaje(Map<Long, Aux> map,RawMaterialPayRoll rawMaterialPayRoll, Double totalReservaGAB,  DiscountProducer discountProducer,Double totalMoneyCollected ) {
        Iterator collections = map.entrySet().iterator();
        while (collections.hasNext()) {

            Map.Entry thisEntry = (Map.Entry) collections.next();
            Aux aux = (Aux) thisEntry.getValue();
            DiscountReserve discountReserve = new DiscountReserve();
            discountReserve.setDiscountProducer(discountProducer);
            discountReserve.setStartDate(rawMaterialPayRoll.getStartDate());
            discountReserve.setEndDate(rawMaterialPayRoll.getEndDate());
            discountReserve.setMaterialProducer(aux.producer);

            //Double porcentage =RoundUtil.getRoundValue( ((aux.earnedMoney *100)/totalMoneyCollected)/100,2, RoundUtil.RoundMode.SYMMETRIC);
            //todo: lanzar un mensaje de advertencia
            Double porcentage;
            if (totalMoneyCollected != 0)
                porcentage = ((aux.earnedMoney * 100) / totalMoneyCollected) / 100;
            else
                porcentage = 0.0;

            Double proration = totalReservaGAB * porcentage;
            proration = RoundUtil.getRoundValue(proration,2, RoundUtil.RoundMode.SYMMETRIC);
            ((Aux) thisEntry.getValue()).reserveDiscount = proration;
            ((Aux) thisEntry.getValue()).earnedMoney = ((Aux) thisEntry.getValue()).earnedMoney - proration;
            discountReserve.setAmount(proration);
            aux.producer.getDiscountReserves().add(discountReserve);
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
        public Double otherIncome;
        public Double adjustment;
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
        Double totalLiquidPay = 0.0;
        for (RawMaterialPayRecord record : rawMaterialPayRoll.getRawMaterialPayRecordList()) {
            RawMaterialProducerDiscount discount = record.getRawMaterialProducerDiscount();
            double totalDiscount = 0.0;
            totalDiscount += discount.getAlcohol();
            totalDiscount += discount.getConcentrated();
            totalDiscount += discount.getWithholdingTax();
            totalDiscount += discount.getCans();
            totalDiscount += discount.getCredit();
            totalDiscount += discount.getVeterinary();
            totalDiscount += discount.getYogurt();
            totalDiscount += discount.getOtherDiscount();
            double liquidPayable = record.getEarnedMoney() - totalDiscount + discount.getOtherIncoming() - record.getDiscountGA();
            totalLiquidPay += liquidPayable;
            record.setLiquidPayable(RoundUtil.getRoundValue(liquidPayable, 2, RoundUtil.RoundMode.SYMMETRIC));
        }

        rawMaterialPayRoll.setTotalLiquidByGAB(RoundUtil.getRoundValue(totalLiquidPay, 2, RoundUtil.RoundMode.SYMMETRIC));
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
                " sum(rawMaterialPayRoll.totalGA) " +
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
        BigDecimal result = (BigDecimal)getEntityManager().createNativeQuery("select IFNULL(sum(monto),0.0) from DESCUENTORESERVA\n" +
                "where FECHAINI = :startDate\n " +
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
