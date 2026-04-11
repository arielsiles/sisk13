package com.encens.khipus.action.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.production.RawMaterialPayRollException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.*;
import com.encens.khipus.service.employees.GestionService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.CollectedRawMaterialCalculatorService;
import com.encens.khipus.service.production.ProductiveZoneService;
import com.encens.khipus.service.production.RawMaterialPayRollService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.encens.khipus.exception.production.RawMaterialPayRollException.*;
import static org.jboss.seam.international.StatusMessage.Severity.ERROR;
import static org.jboss.seam.international.StatusMessage.Severity.WARN;

@Name("rawMaterialPayRollAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialPayRollAction extends GenericAction<RawMaterialPayRoll> {

    //private Date month;
    private int fortnight;
    private boolean readonly;
    private boolean delete = false;
    private boolean editPriceMilk = false;
    private boolean editIUE = false;
    private boolean editIT = false;
    private Gestion gestion;
    private Month month;
    private Periodo periodo;
    private List<GestionPayroll> gestionPayrollList;
    private boolean sinDomingos = false;
    private boolean soloDomingos = false;
    private ProductiveZone productiveZone = null;

    private List<RawMaterialPayRoll> rawMaterialPayRollList;

    @In
    private RawMaterialPayRollService rawMaterialPayRollService;

    @In
    private ProductiveZoneService productiveZoneService;

    @In
    private GestionService gestionService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    /** @Claude OPT-6: Inyeccion de servicio para pre-calcular peso total quincenal **/
    @In
    private CollectedRawMaterialCalculatorService collectedRawMaterialCalculatorService;

    @Override
    protected GenericService getService() {
        return rawMaterialPayRollService;
    }

    @Factory(value = "rawMaterialPayRoll", scope = ScopeType.STATELESS)
    public RawMaterialPayRoll initRawMaterialPayRoll() {
        /*try {
            getInstance().setUnitPrice(companyConfigurationService.findUnitPriceMilk());
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            getInstance().setIt(companyConfigurationService.findIT());
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            getInstance().setIue(companyConfigurationService.finIUE());
        } catch (CompanyConfigurationNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
        if (getInstance().getUnitPrice() == 0.0) {
            getInstance().setIt(0.3);
            getInstance().setIue(0.5);
            //getInstance().setUnitPrice(3.5);
            getInstance().setUnitPrice(Constants.PRICE_UNIT_MILK);
            getInstance().setTaxRate(getInstance().getIt() + getInstance().getIue());
        }
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return "\"" + getInstance().getStartDate() + "\" - \"" + getInstance().getEndDate() + "\"";
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String startNew() {
        return Outcome.SUCCESS;
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String startNewApprove() {
        return Outcome.SUCCESS;
    }

    @Factory(value = "periodosPayRollGenerate", scope = ScopeType.STATELESS)
    public Periodo[] getPeriodos() {
        return Periodo.values();
    }

    public int[] getFortnightList() {
        return new int[]{1, 2};
    }

    public int getFortnight() {
        return fortnight;
    }

    public void setFortnight(int fortnight) {
        this.fortnight = fortnight;
    }

    /*public Date getMonth() {
        return month;
    }

    public void setMonth(Date month) {
        this.month = month;
    }*/

    public boolean getReadonly() {
        return readonly;
    }

    @Override
    @End(ifOutcome = Outcome.SUCCESS)
    public String create() {
        if (getInstance().getRawMaterialPayRecordList().size() == 0) {
            facesMessages.addFromResourceBundle(WARN, "RawMaterialPayRoll.warn.thereIsNoRecordsGenerated");
            return Outcome.REDISPLAY;
        }

        try {
            RawMaterialPayRoll rawMaterialPayRoll = getInstance();
            rawMaterialPayRollService.create(rawMaterialPayRoll);
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (RawMaterialPayRollException ex) {
            print(ex);
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (Exception ex) {
            if (ex.getCause() instanceof RawMaterialPayRollException) {
                print((RawMaterialPayRollException) ex.getCause());
                return Outcome.REDISPLAY;
            } else {
                facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
                return Outcome.REDISPLAY;
            }
        }
    }

    private void print(RawMaterialPayRollException ex) {
        if (ex.getCode() == CROSS_WITH_ANOTHER_PAYROLL) {
            facesMessages.addFromResourceBundle(WARN, "RawMaterialPayRoll.warning.StartDateCrossWithAnotherPayRoll", ex.getDate());
        } else if (ex.getCode() == MINIMUM_START_DATE) {
            facesMessages.addFromResourceBundle(WARN, "RawMaterialPayRoll.warning.StartDateMustBe", ex.getDate());
        } else if (ex.getCode() == NO_COLLECTION_ON_DATE) {
            facesMessages.addFromResourceBundle(WARN, "RawMaterialPayRoll.warning.NoCollectionOnDate", ex.getDate());
        } else {
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    @Begin(join = true, ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String selectJoin(RawMaterialPayRoll instance) {
        return select(instance);
    }

    public void selectProductiveZone(ProductiveZone productiveZone) {
        try {
            this.productiveZone = getService().findById(ProductiveZone.class, productiveZone.getId());
            getInstance().setProductiveZone(productiveZone);
        } catch (Exception ex) {
            log.error("Caught Error", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    public String generate() {
        try {
            RawMaterialPayRoll rawMaterialPayRoll = getInstance();
            Calendar dateIni = Calendar.getInstance();
            Calendar dateEnd = Calendar.getInstance();
            dateIni.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
            dateEnd.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            rawMaterialPayRoll.setStartDate(dateFormat.parse(dateFormat.format(dateIni.getTime())));
            rawMaterialPayRoll.setEndDate(dateFormat.parse(dateFormat.format(dateEnd.getTime())));

            if (rawMaterialPayRoll.getStartDate().compareTo(rawMaterialPayRoll.getEndDate()) > 0) {
                facesMessages.addFromResourceBundle(WARN, "RawMaterialPayRoll.warning.startDateGreaterThanEndDate");
                return Outcome.FAIL;
            }

            List<DiscountProducer> discountProducers = rawMaterialPayRollService.findDiscountsProducerByDate(rawMaterialPayRoll.getEndDate());
            if (discountProducers.size() > 1) {
                addDatesDuplicatesMessage();
                return Outcome.REDISPLAY;
            }
            DiscountProducer discountProducer = discountProducers.isEmpty() ? null : discountProducers.get(0);

            if (discountProducer == null || discountProducer.getReserve() == 0.0)
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "RawMaterialPayRoll.info.NoFoundReserve");

            List<ProductiveZone> productiveZones = productiveZoneService.findAllThatDoNotHaveCollectionForm(
                    rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate());

            Double totalWeightFortnight = collectedRawMaterialCalculatorService.calculateCollectedAmountBetweenDates(
                    rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate(), rawMaterialPayRoll.getMetaProduct(), getDayFilter());

            Map<Long, ProducerTax> producerTaxCache = rawMaterialPayRollService.preloadProducerTaxes(
                    rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate());

            for (ProductiveZone zone : productiveZones) {
                if (zone.getGroup().equals("ILVA")) {
                    RawMaterialPayRoll payRoll = new RawMaterialPayRoll();
                    payRoll.setEndDate(rawMaterialPayRoll.getEndDate());
                    payRoll.setStartDate(rawMaterialPayRoll.getStartDate());
                    payRoll.setCompany(rawMaterialPayRoll.getCompany());
                    payRoll.setMetaProduct(rawMaterialPayRoll.getMetaProduct());
                    payRoll.setUnitPrice(rawMaterialPayRoll.getUnitPrice());
                    payRoll.setTaxRate(rawMaterialPayRoll.getTaxRate());
                    payRoll.setProductiveZone(zone);
                    payRoll.setIt(rawMaterialPayRoll.getIt());
                    payRoll.setIue(rawMaterialPayRoll.getIue());

                    rawMaterialPayRollService.validate(payRoll);
                    rawMaterialPayRollService.generatePayroll(payRoll, discountProducer, totalWeightFortnight, producerTaxCache, getDayFilter());
                    rawMaterialPayRollService.createAll(payRoll);

                    for (int i = 0; i < payRoll.getRawMaterialPayRecordList().size(); i++) {
                        RawMaterialPayRecord rec = payRoll.getRawMaterialPayRecordList().get(i);
                        if (rec.getLiquidPayable() < 0) {
                            String producerName = rec.getRawMaterialProducerDiscount().getRawMaterialProducer().getFullName();
                            facesMessages.add(StatusMessage.Severity.ERROR,
                                "Liquido pagable negativo: " + producerName + " (" + zone.getFullName() + ") = " + rec.getLiquidPayable() + " Bs");
                        }
                    }
                }
            }

            addSuccessGenerateAllPayRoll(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate());
            return Outcome.SUCCESS;

        } catch (RawMaterialPayRollException ex) {
            print(ex);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (Exception ex) {
            if (ex.getCause() instanceof RawMaterialPayRollException) {
                print((RawMaterialPayRollException) ex.getCause());
                return Outcome.REDISPLAY;
            } else {
                log.error("Caught Error", ex);
                facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
                return Outcome.REDISPLAY;
            }
        }
        return Outcome.REDISPLAY;
    }

    private void addDatesDuplicatesMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "DiscountProducer.FindDatesDuplicatesMessage");
    }

    public String approvedPayRoll() {
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            startDate.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
            endDate.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));

        if(rawMaterialPayRollService.findAllPayRollesByGAB(startDate.getTime(),endDate.getTime(),productiveZone).size() == 0)
        {
            addNotFoundPayRollesPendingMessage(startDate, endDate);

            return Outcome.REDISPLAY;
        }

        rawMaterialPayRollService.approvedSession(startDate, endDate, productiveZone);
        rawMaterialPayRollService.approvedNoteRejection(startDate, endDate);
        rawMaterialPayRollService.approvedDiscounts(startDate,endDate,productiveZone);
        rawMaterialPayRollService.approvedDiscountsGAB(startDate, endDate, productiveZone);
        rawMaterialPayRollService.approvedRawMaterialPayRoll(startDate,endDate,productiveZone);
        rawMaterialPayRollService.approvedReservProductor(startDate,endDate);

            addSuccessApprobedPayRoll(startDate,endDate);
        return Outcome.SUCCESS;
    }

    private void addSuccessApprobedPayRoll(Calendar startDate, Calendar endDate) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"RawMaterialPayRoll.info.SuccessApprobedPayRoll",startDate.getTime(),endDate.getTime());
    }

    private void addNotFoundPayRollesPendingMessage(Calendar startDate, Calendar endDate) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"RawMaterialPayRoll.error.NotFoundPayRolles",startDate.getTime(),endDate.getTime());
    }


    private void addSuccessGenerateAllPayRoll(Date startDate, Date endDate) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"RawMaterialPayRoll.info.SuccessGenerateAllPayRoll",startDate,endDate);
    }

    public String deleteAll() throws ReferentialIntegrityException, ConcurrencyException, ParseException {
        RawMaterialPayRoll rawMaterialPayRoll = getInstance();
        Calendar dateIni = Calendar.getInstance();
        Calendar dateEnd = Calendar.getInstance();
        dateIni.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
        dateEnd.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        rawMaterialPayRoll.setStartDate(dateFormat.parse(dateFormat.format(dateIni.getTime())));
        rawMaterialPayRoll.setEndDate(dateFormat.parse(dateFormat.format(dateEnd.getTime())));
        rawMaterialPayRollService.deleteReserveDiscount(rawMaterialPayRoll.getStartDate(),rawMaterialPayRoll.getEndDate());

        List<RawMaterialPayRoll> rawMaterialPayRolls = rawMaterialPayRollService.findAll(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate(), rawMaterialPayRoll.getMetaProduct());
        if(rawMaterialPayRolls.size() == 0)
        {
            addWarnGeneratePayRollMessage(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate());
            return Outcome.SUCCESS;
        }
        for (RawMaterialPayRoll payRoll : rawMaterialPayRolls) {
            if(payRoll.getState().equals(StatePayRoll.APPROVED))
            {
                addErrorGeneratePayRollMessage(rawMaterialPayRoll.getStartDate(),rawMaterialPayRoll.getEndDate());
                return Outcome.SUCCESS;
            }
        }
        for (RawMaterialPayRoll payRoll : rawMaterialPayRolls) {
            rawMaterialPayRollService.delete(payRoll);
        }
        addDeleteGeneratePayRollMessage(rawMaterialPayRoll.getStartDate(), rawMaterialPayRoll.getEndDate());
        return Outcome.SUCCESS;
    }

    private void addWarnGeneratePayRollMessage(Date startDate, Date endDate) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,"RawMaterialPayRoll.warning.deleteAllPayRoll",startDate,endDate);
    }

    private void addDeleteGeneratePayRollMessage(Date startDate, Date endDate) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"RawMaterialPayRoll.message.deleteAllPayRoll",startDate,endDate);
    }

    private void addErrorGeneratePayRollMessage(Date startDate,Date endDate) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"RawMaterialPayRoll.error.deleteAllPayRoll",startDate,endDate);
    }

    public void redefine() {
        getInstance().getRawMaterialPayRecordList().clear();
        getInstance().setTotalLiquidByGAB(0.0);
        getInstance().setTotalAdjustmentByGAB(0.0);
        getInstance().setTotalCollectedByGAB(0.0);
        getInstance().setTotalConcentratedByGAB(0.0);
        getInstance().setTotalAlcoholByGAB(0.0);
        getInstance().setTotalYogourdByGAB(0.0);
        getInstance().setTotalCreditByGAB(0.0);
        getInstance().setTotalDiscountByGAB(0.0);
        getInstance().setTotalMountCollectdByGAB(0.0);
        getInstance().setTotalOtherDiscountByGAB(0.0);
        getInstance().setTotalVeterinaryByGAB(0.0);
        getInstance().setTotalRetentionGAB(0.0);
        getInstance().setTotalRecipByGAB(0.0);
        readonly = false;
    }

    public List<RawMaterialPayRoll> getRawMaterialPayRollList() {
        return rawMaterialPayRollList;
    }

    public void setRawMaterialPayRollList(List<RawMaterialPayRoll> rawMaterialPayRollList) {
        this.rawMaterialPayRollList = rawMaterialPayRollList;
    }

    public boolean isDelete() {
        List<RawMaterialPayRoll> rawMaterialPayRolls = rawMaterialPayRollService.findAll();
        return !rawMaterialPayRolls.isEmpty();
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    /* Devuelve la ultima gestion
    public Gestion getGestion() {
        if(getInstance().getId() != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getInstance().getStartDate());
            this.gestion = gestionService.getGestion(cal.get(Calendar.YEAR));
        }else
            gestion = gestionService.getLastGestion();

        return gestion;
    }*/

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        if(getInstance().getId() != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getInstance().getStartDate());
            this.month = Month.getMonth(cal.getTime());
        }
        else
           month = Month.getMonth(new Date());

        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public Periodo getPeriodo() {
        if(getInstance().getId() != null)
        {
            Calendar end = Calendar.getInstance();
            end.setTime(getInstance().getEndDate());
            if(end.get(Calendar.DAY_OF_MONTH) > 15)
                this.periodo = Periodo.SECONDPERIODO;
            else
                this.periodo = Periodo.FIRSTPERIODO;
        }else
        {
            Calendar end = Calendar.getInstance();
            end.setTime(new Date());
            if(end.get(Calendar.DAY_OF_MONTH) > 15)
                this.periodo = Periodo.SECONDPERIODO;
            else
                this.periodo = Periodo.FIRSTPERIODO;
        }
        return periodo;
    }

    public void setPeriodo(Periodo periodo) {
        this.periodo = periodo;
    }

    public List<GestionPayroll> getGestionPayrollList() {
        return gestionPayrollList;
    }

    public void setGestionPayrollList(List<GestionPayroll> gestionPayrollList) {
        this.gestionPayrollList = gestionPayrollList;
    }

    public void cleanGestionList() {
        setGestionPayrollList(null);
    }

    public String getCompletPeriod() {
        if (periodo != null || month != null)
            return periodo.getPeriodoLiteral() + "del mes de " + month.getMonthLiteral();
        else
            return "";
    }

    public boolean isPending() {
        return StatePayRoll.PENDING.equals(getInstance().getState());
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public boolean isEditPriceMilk() {
        return editPriceMilk;
    }

    public void setEditPriceMilk(boolean editPriceMilk) {
        this.editPriceMilk = editPriceMilk;
    }

    public boolean isEditIUE() {
        return editIUE;
    }

    public void setEditIUE(boolean editIUE) {
        this.editIUE = editIUE;
    }

    public boolean isEditIT() {
        return editIT;
    }

    public void setEditIT(boolean editIT) {
        this.editIT = editIT;
    }

    public boolean isSinDomingos() {
        return sinDomingos;
    }

    public void setSinDomingos(boolean sinDomingos) {
        this.sinDomingos = sinDomingos;
        if (sinDomingos) this.soloDomingos = false;
    }

    public boolean isSoloDomingos() {
        return soloDomingos;
    }

    public void setSoloDomingos(boolean soloDomingos) {
        this.soloDomingos = soloDomingos;
        if (soloDomingos) this.sinDomingos = false;
    }

    private int getDayFilter() {
        if (sinDomingos) return 1;
        if (soloDomingos) return 2;
        return 0;
    }
}
