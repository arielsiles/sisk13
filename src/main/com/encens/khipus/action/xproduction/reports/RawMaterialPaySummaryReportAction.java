package com.encens.khipus.action.xproduction.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.model.production.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.ClientService;
import com.encens.khipus.service.production.RawMaterialPayRollService;
import com.encens.khipus.service.production.RawMaterialPayRollServiceBean;
import com.encens.khipus.service.production.SalaryMovementProducerService;
import com.encens.khipus.service.production.TypeMovementProducerService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.VoucherBuilder;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Encens S.R.L.
 * Action to generate payroll summary report by payment method and currency
 *
 * @author
 * @version $Id: SummaryPayrollByPaymentMethodReportAction.java  22-ene-2010 11:38:12$
 */
@Name("rawMaterialPaySummaryReportAction")
@Scope(ScopeType.CONVERSATION)
//@Restrict("#{s:hasPermission('RAWMATERIALPAYSUMMARY','VIEW')}")
public class RawMaterialPaySummaryReportAction extends GenericReportAction {
    @In
    RawMaterialPayRollService rawMaterialPayRollService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    protected FacesMessages facesMessages;
    @In
    private TypeMovementProducerService typeMovementProducerService;
    @In
    private SalaryMovementProducerService salaryMovementProducerService;
    @In
    private ClientService clientService;

    private String summaryReportTitle;
    private String gestionTitle;

    private Gestion gestion;
    private Month month;
    private Periodo periodo;
    private ProductiveZone zone;
    private MetaProduct metaProduct;
    private double unitPrice;
    private double mountCollection;
    private double totalCollectionXUnitPrice;
    private double differences;
    private double weightReal;
    private double totalWeightRealXUnitPrice;
    private double yogurt;
    private double veterinari;
    private double credit;
    private double recipient;
    private RawMaterialPayRollServiceBean.SummaryTotal summaryTotal;
    private RawMaterialPayRollServiceBean.Discounts discounts;

    private Calendar dateIni;
    private Calendar dateEnd;

    private List<GestionPayroll> gestionPayrollList;


    private GeneratedPayrollType generatedPayrollType = GeneratedPayrollType.OFFICIAL;


    public void generateReport() {

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        dateIni = Calendar.getInstance();
        dateEnd = Calendar.getInstance();
        dateIni.set(gestion.getYear(), month.getValue(), periodo.getInitDay());
        dateEnd.set(gestion.getYear(), month.getValue(), periodo.getEndDay(month.getValue() + 1, gestion.getYear()));
        sdf.setCalendar(dateIni);
        sdf.setCalendar(dateEnd);

        try {
            addSummaryTotal(reportParameters);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        log.debug("generating expenseBudgetReport......................................");

        reportParameters.put("reportTitle", messages.get("Report.titleGeneral"));
        reportParameters.put("period", messages.get("Report.period"));
        reportParameters.put("startDate", df.format(dateIni.getTime()));
        reportParameters.put("endDate", df.format(dateEnd.getTime()));

        super.generateReport(
                "rawMaterialPaySummaryReportAction",
                "/production/reports/rawMaterialPaySummaryReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Report.rawMaterialPaySummaryReportAction"),
                reportParameters);
    }

    private void    addSummaryTotal(HashMap<String, Object> params) throws ParseException {
        //discounts = rawMaterialPayRollService.getDiscounts(dateIni.getTime(),dateEnd.getTime(),null,null);
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        Date startDate = dateFormat.parse(dateFormat.format(dateIni.getTime()));
        Date endDate = dateFormat.parse(dateFormat.format(dateEnd.getTime()));

        discounts = rawMaterialPayRollService.getDiscounts(startDate, endDate, zone, metaProduct);

        summaryTotal = rawMaterialPayRollService.getSumaryTotal(startDate, endDate, zone, metaProduct);

        Double totalMoneyCollected = discounts.mount;
        Double totalDifferencesMoney = rawMaterialPayRollService.getTotalMoneyDiff(discounts.unitPrice, startDate, endDate, metaProduct);
        Double diffTotal = rawMaterialPayRollService.getTotalDiff(discounts.unitPrice, startDate, endDate, metaProduct);
        Double balanceWeightTotal = rawMaterialPayRollService.getBalanceWeightTotal(discounts.unitPrice, startDate, endDate, metaProduct);
        Double totalMoneyBalance = totalMoneyCollected + totalDifferencesMoney;
        Double reservProducer = rawMaterialPayRollService.getReservProducer(startDate,endDate);
        Double reserveGA = discounts.collected * Constants.DISCOUNT_GA;

        Double total = totalMoneyBalance + discounts.otherIncome;
        params.put("total_collected", df.format(discounts.collected));
        params.put("diff_total", df.format(diffTotal));
        params.put("price_unit", df.format(discounts.unitPrice));
        params.put("total_money_collected", df.format(totalMoneyCollected));
        params.put("difference_money", df.format(totalDifferencesMoney));
        params.put("total_money", df.format(totalMoneyBalance));
        params.put("weight_balance_total", df.format(balanceWeightTotal));
        params.put("total_other_incom", df.format(total));

        //discounts
        Double totalDiscount = discounts.alcohol + discounts.concentrated + discounts.yogurt
                + discounts.veterinary + discounts.credit + discounts.recip + discounts.retention
                + discounts.otherDiscount + reservProducer + reserveGA + discounts.commission;
        //Double liquidPay = totalMoney - totalDifferences;
        params.put("alcohol", df.format(discounts.alcohol));
        params.put("concentrated", df.format(discounts.concentrated));
        params.put("yogurt", df.format(discounts.yogurt));
        params.put("veterinary", df.format(discounts.veterinary));
        params.put("credit", df.format(discounts.credit));
        params.put("recip", df.format(discounts.recip));
        params.put("retention", df.format(discounts.retention));
        params.put("otrosDescuentos", df.format(discounts.otherDiscount));
        params.put("comision", df.format(discounts.commission));
        params.put("otrosIngresos", df.format(discounts.otherIncome));
        Double iue, it,porcentageIUE;
        RawMaterialPayRoll rawMaterialPayRoll =  rawMaterialPayRollService.getTotalsRawMaterialPayRoll(startDate,endDate,null,null);
        porcentageIUE = rawMaterialPayRoll.getIue() / rawMaterialPayRoll.getTaxRate();

        //iue = discounts.retention * 0.625;
        iue = discounts.retention * porcentageIUE;
        //it = discounts.retention * 0.375;
        it = discounts.retention - iue;
        params.put("iue", df.format(iue));
        params.put("it", df.format(it));
        params.put("reserva_productores", df.format(reservProducer));
        params.put("reserveGA", df.format(reserveGA));
        params.put("total_differences", df.format(totalDiscount));
        //todo: modificar ajustar el prorrateo
        Double totalLiquid = total - totalDiscount;
        //params.put("liquid_pay", df.format(discounts.liquid));
        params.put("liquid_pay", df.format(totalLiquid));

    }


    public void accountingPeriod(){

        Date startDate = DateUtils.getDate(gestion.getYear(), month.getValue()+1, periodo.getInitDay());
        Date endDate = DateUtils.getDate(gestion.getYear(), month.getValue()+1, periodo.getEndDay(month.getValue() + 1, gestion.getYear()));

        System.out.println("Fecha Inicio: " + DateUtils.format(startDate, "dd/MM/yyyy"));
        System.out.println("Fecha Fin: " + DateUtils.format(endDate, "dd/MM/yyyy"));

        discounts = rawMaterialPayRollService.getDiscounts(startDate, endDate, zone, metaProduct);
        summaryTotal = rawMaterialPayRollService.getSumaryTotal(startDate, endDate, zone, metaProduct);

        Double totalMoneyCollected = discounts.mount;
        Double totalDifferencesMoney = rawMaterialPayRollService.getTotalMoneyDiff(discounts.unitPrice, startDate, endDate, metaProduct);
        Double diffTotal = rawMaterialPayRollService.getTotalDiff(discounts.unitPrice, startDate, endDate, metaProduct);
        Double balanceWeightTotal = rawMaterialPayRollService.getBalanceWeightTotal(discounts.unitPrice, startDate, endDate, metaProduct);
        Double totalMoneyBalance = totalMoneyCollected + totalDifferencesMoney;
        Double reservProducer = rawMaterialPayRollService.getReservProducer(startDate,endDate);
        Double reserveGA = discounts.collected * Constants.DISCOUNT_GA;
        Double total = totalMoneyBalance + discounts.otherIncome;

        Double totalDiscount = discounts.alcohol + discounts.concentrated + discounts.yogurt
                + discounts.veterinary + discounts.credit + discounts.recip + discounts.retention
                + discounts.otherDiscount + reservProducer + reserveGA + discounts.commission;

        Double iue, it,porcentageIUE;
        RawMaterialPayRoll rawMaterialPayRoll =  rawMaterialPayRollService.getTotalsRawMaterialPayRoll(startDate,endDate,null,null);
        porcentageIUE = rawMaterialPayRoll.getIue() / rawMaterialPayRoll.getTaxRate();
        iue = discounts.retention * porcentageIUE;
        it = discounts.retention - iue;
        Double totalLiquid = total - totalDiscount;


        System.out.println(".......PARA CONTABILIZAR......");
        System.out.println("-----> Total: " + BigDecimalUtil.toBigDecimal(totalMoneyBalance));
        System.out.println("-----> alcohol: " + discounts.alcohol);
        System.out.println("-----> concentrated: " + discounts.concentrated);
        System.out.println("-----> yogurt: " + discounts.yogurt);
        System.out.println("-----> veterinary: " + discounts.veterinary);
        System.out.println("-----> credit: " + discounts.credit);
        System.out.println("-----> recip: " + discounts.recip);
        System.out.println("-----> retention: " + discounts.retention);
        System.out.println("-----> commission: " + discounts.commission);
        System.out.println("-----> otherIncome: " + discounts.otherIncome);

        System.out.println("-----> IT: " + it);
        System.out.println("-----> IUE: " + iue);

        System.out.println("-----> Total Descuentos: " + totalDiscount);
        System.out.println("-----> Liquido Pagable: " + totalLiquid);


        Voucher voucher = VoucherBuilder.newGeneralVoucher(null, "REGISTRO ACOPIO DE LECHE "
                                                                + periodo.getQuincenaLiteral()
                                                                + month.getMonthLiteral().toUpperCase()
                                                                + " " + gestion.getYear());
        voucher.setDocumentType(Constants.CB_VOUCHER_DOCTYPE);
        voucher.setDate(endDate);

        VoucherDetail voucherDebit = new VoucherDetail();
        voucherDebit.setAccount(Constants.ACCOUNT_LECHECRUDA);
        voucherDebit.setDebit(BigDecimalUtil.toBigDecimal(totalMoneyBalance));
        voucherDebit.setCredit(BigDecimal.ZERO);
        voucherDebit.setCurrency(FinancesCurrencyType.P);
        voucherDebit.setExchangeAmount(BigDecimal.ONE);
        voucherDebit.setDebitMe(BigDecimal.ZERO);
        voucherDebit.setCreditMe(BigDecimal.ZERO);
        voucher.addVoucherDetail(voucherDebit);

        if (discounts.commission > 0){
            VoucherDetail voucherCredt1 = new VoucherDetail();
            voucherCredt1.setAccount(Constants.ACCOUNT_FONDOSCUSTODIA);
            voucherCredt1.setDebit(BigDecimal.ZERO);
            voucherCredt1.setCredit(BigDecimalUtil.toBigDecimal(discounts.commission));
            voucherCredt1.setCurrency(FinancesCurrencyType.P);
            voucherCredt1.setExchangeAmount(BigDecimal.ONE);
            voucherCredt1.setDebitMe(BigDecimal.ZERO);
            voucherCredt1.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt1);

        }

        if (it > 0){
            VoucherDetail voucherCredt2 = new VoucherDetail();
            voucherCredt2.setAccount(Constants.ACCOUNT_IT_RETENIDO);
            voucherCredt2.setDebit(BigDecimal.ZERO);
            voucherCredt2.setCredit(BigDecimalUtil.toBigDecimal(it));
            voucherCredt2.setCurrency(FinancesCurrencyType.P);
            voucherCredt2.setExchangeAmount(BigDecimal.ONE);
            voucherCredt2.setDebitMe(BigDecimal.ZERO);
            voucherCredt2.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt2);
        }

        if (iue > 0){
            VoucherDetail voucherCredt3 = new VoucherDetail();
            voucherCredt3.setAccount(Constants.ACCOUNT_IUE_RETENIDO);
            voucherCredt3.setDebit(BigDecimal.ZERO);
            voucherCredt3.setCredit(BigDecimalUtil.toBigDecimal(iue));
            voucherCredt3.setCurrency(FinancesCurrencyType.P);
            voucherCredt3.setExchangeAmount(BigDecimal.ONE);
            voucherCredt3.setDebitMe(BigDecimal.ZERO);
            voucherCredt3.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt3);
        }

        if (totalLiquid > 0){
            VoucherDetail voucherCredt4 = new VoucherDetail();
            voucherCredt4.setAccount(Constants.ACCOUNT_ACREEDORES_BIENESSERVICIOS);
            voucherCredt4.setDebit(BigDecimal.ZERO);
            voucherCredt4.setCredit(BigDecimalUtil.toBigDecimal(totalLiquid));
            voucherCredt4.setCurrency(FinancesCurrencyType.P);
            voucherCredt4.setExchangeAmount(BigDecimal.ONE);
            voucherCredt4.setDebitMe(BigDecimal.ZERO);
            voucherCredt4.setCreditMe(BigDecimal.ZERO);
            voucherCredt4.setProviderCode(Constants.PROVIDER_CODE_PRODUCTORES);
            voucher.addVoucherDetail(voucherCredt4);
        }

        if (discounts.veterinary > 0){

            TypeMovementProducer type = typeMovementProducerService.findTypeMovementProducer(SalaryMovementProducerTypeEnum.VETE);
            List<SalaryMovementProducer> salaryMovementProducerList = salaryMovementProducerService.findSalaryMovementProducerList(startDate, endDate, type);

            for (SalaryMovementProducer salaryMovementProducer : salaryMovementProducerList){
                Client client = clientService.findClientByIdNumber(salaryMovementProducer.getRawMaterialProducer().getIdNumber());
                VoucherDetail voucherCredt4 = new VoucherDetail();
                voucherCredt4.setAccount(Constants.ACCOUNT_CLIENTESPRODUCTORES);
                voucherCredt4.setDebit(BigDecimal.ZERO);
                voucherCredt4.setCredit(BigDecimalUtil.toBigDecimal(salaryMovementProducer.getValor()));
                voucherCredt4.setCurrency(FinancesCurrencyType.P);
                voucherCredt4.setExchangeAmount(BigDecimal.ONE);
                voucherCredt4.setDebitMe(BigDecimal.ZERO);
                voucherCredt4.setCreditMe(BigDecimal.ZERO);
                voucherCredt4.setClient(client);
                voucher.addVoucherDetail(voucherCredt4);
            }


            /*VoucherDetail voucherCredt4 = new VoucherDetail();
            voucherCredt4.setAccount(Constants.ACCOUNT_CLIENTESPRODUCTORES);
            voucherCredt4.setDebit(BigDecimal.ZERO);
            voucherCredt4.setCredit(BigDecimalUtil.toBigDecimal(discounts.veterinary));
            voucherCredt4.setCurrency(FinancesCurrencyType.P);
            voucherCredt4.setExchangeAmount(BigDecimal.ONE);
            voucherCredt4.setDebitMe(BigDecimal.ZERO);
            voucherCredt4.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt4);*/
        }

        if (discounts.yogurt > 0){
            TypeMovementProducer type = typeMovementProducerService.findTypeMovementProducer(SalaryMovementProducerTypeEnum.LACT);
            List<SalaryMovementProducer> salaryMovementProducerList = salaryMovementProducerService.findSalaryMovementProducerList(startDate, endDate, type);

            for (SalaryMovementProducer salaryMovementProducer : salaryMovementProducerList){
                Client client = clientService.findClientByIdNumber(salaryMovementProducer.getRawMaterialProducer().getIdNumber());
                VoucherDetail voucherCredt5 = new VoucherDetail();
                voucherCredt5.setAccount(Constants.ACCOUNT_CLIENTES);
                voucherCredt5.setDebit(BigDecimal.ZERO);
                voucherCredt5.setCredit(BigDecimalUtil.toBigDecimal(salaryMovementProducer.getValor()));
                voucherCredt5.setCurrency(FinancesCurrencyType.P);
                voucherCredt5.setExchangeAmount(BigDecimal.ONE);
                voucherCredt5.setDebitMe(BigDecimal.ZERO);
                voucherCredt5.setCreditMe(BigDecimal.ZERO);
                voucherCredt5.setClient(client);
                voucher.addVoucherDetail(voucherCredt5);
            }
            /*VoucherDetail voucherCredt5 = new VoucherDetail();
            voucherCredt5.setAccount(Constants.ACCOUNT_CLIENTES);
            voucherCredt5.setDebit(BigDecimal.ZERO);
            voucherCredt5.setCredit(BigDecimalUtil.toBigDecimal(discounts.yogurt));
            voucherCredt5.setCurrency(FinancesCurrencyType.P);
            voucherCredt5.setExchangeAmount(BigDecimal.ONE);
            voucherCredt5.setDebitMe(BigDecimal.ZERO);
            voucherCredt5.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt5);*/
        }

        if (discounts.credit > 0){
            VoucherDetail voucherCredt6 = new VoucherDetail();
            voucherCredt6.setAccount(Constants.ACCOUNT_FONDOSCUSTODIA);
            voucherCredt6.setDebit(BigDecimal.ZERO);
            voucherCredt6.setCredit(BigDecimalUtil.toBigDecimal(discounts.credit));
            voucherCredt6.setCurrency(FinancesCurrencyType.P);
            voucherCredt6.setExchangeAmount(BigDecimal.ONE);
            voucherCredt6.setDebitMe(BigDecimal.ZERO);
            voucherCredt6.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt6);
        }

        if (discounts.alcohol > 0){
            VoucherDetail voucherCredt6 = new VoucherDetail();
            voucherCredt6.setAccount(Constants.ACCOUNT_FONDOSCUSTODIA);
            voucherCredt6.setDebit(BigDecimal.ZERO);
            voucherCredt6.setCredit(BigDecimalUtil.toBigDecimal(discounts.alcohol));
            voucherCredt6.setCurrency(FinancesCurrencyType.P);
            voucherCredt6.setExchangeAmount(BigDecimal.ONE);
            voucherCredt6.setDebitMe(BigDecimal.ZERO);
            voucherCredt6.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt6);
        }

        if (discounts.concentrated > 0){
            VoucherDetail voucherCredt6 = new VoucherDetail();
            voucherCredt6.setAccount(Constants.ACCOUNT_FONDOSCUSTODIA);
            voucherCredt6.setDebit(BigDecimal.ZERO);
            voucherCredt6.setCredit(BigDecimalUtil.toBigDecimal(discounts.concentrated));
            voucherCredt6.setCurrency(FinancesCurrencyType.P);
            voucherCredt6.setExchangeAmount(BigDecimal.ONE);
            voucherCredt6.setDebitMe(BigDecimal.ZERO);
            voucherCredt6.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt6);
        }

        if (discounts.recip > 0){
            VoucherDetail voucherCredt6 = new VoucherDetail();
            voucherCredt6.setAccount(Constants.ACCOUNT_FONDOSCUSTODIA);
            voucherCredt6.setDebit(BigDecimal.ZERO);
            voucherCredt6.setCredit(BigDecimalUtil.toBigDecimal(discounts.recip));
            voucherCredt6.setCurrency(FinancesCurrencyType.P);
            voucherCredt6.setExchangeAmount(BigDecimal.ONE);
            voucherCredt6.setDebitMe(BigDecimal.ZERO);
            voucherCredt6.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt6);
        }

        if (discounts.otherDiscount > 0){
            VoucherDetail voucherCredt6 = new VoucherDetail();
            voucherCredt6.setAccount(Constants.ACCOUNT_FONDOSCUSTODIA);
            voucherCredt6.setDebit(BigDecimal.ZERO);
            voucherCredt6.setCredit(BigDecimalUtil.toBigDecimal(discounts.otherDiscount));
            voucherCredt6.setCurrency(FinancesCurrencyType.P);
            voucherCredt6.setExchangeAmount(BigDecimal.ONE);
            voucherCredt6.setDebitMe(BigDecimal.ZERO);
            voucherCredt6.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt6);
        }

        if (reservProducer > 0){
            VoucherDetail voucherCredt6 = new VoucherDetail();
            voucherCredt6.setAccount(Constants.ACCOUNT_FONDOSCUSTODIA);
            voucherCredt6.setDebit(BigDecimal.ZERO);
            voucherCredt6.setCredit(BigDecimalUtil.toBigDecimal(reservProducer));
            voucherCredt6.setCurrency(FinancesCurrencyType.P);
            voucherCredt6.setExchangeAmount(BigDecimal.ONE);
            voucherCredt6.setDebitMe(BigDecimal.ZERO);
            voucherCredt6.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt6);
        }

        if (reserveGA > 0){
            VoucherDetail voucherCredt6 = new VoucherDetail();
            voucherCredt6.setAccount(Constants.ACCOUNT_FONDOSCUSTODIA);
            voucherCredt6.setDebit(BigDecimal.ZERO);
            voucherCredt6.setCredit(BigDecimalUtil.toBigDecimal(reserveGA));
            voucherCredt6.setCurrency(FinancesCurrencyType.P);
            voucherCredt6.setExchangeAmount(BigDecimal.ONE);
            voucherCredt6.setDebitMe(BigDecimal.ZERO);
            voucherCredt6.setCreditMe(BigDecimal.ZERO);
            voucher.addVoucherDetail(voucherCredt6);
        }

        voucherAccoutingService.saveVoucher(voucher);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"Se contabilizó la quincena correctamente.");
    }

    @Override
    protected String getEjbql() {
        return "";
    }

    @Create
    public void init() {
        this.month = Month.getMonth(new Date());
        Calendar end = Calendar.getInstance();
        end.setTime(new Date());
        if(end.get(Calendar.DAY_OF_MONTH) > 15)
            this.periodo = Periodo.SECONDPERIODO;
        else
            this.periodo = Periodo.FIRSTPERIODO;
    }

    public RawMaterialPayRollService getRawMaterialPayRollService() {
        return rawMaterialPayRollService;
    }

    public void setRawMaterialPayRollService(RawMaterialPayRollService rawMaterialPayRollService) {
        this.rawMaterialPayRollService = rawMaterialPayRollService;
    }

    public String getSummaryReportTitle() {
        return summaryReportTitle;
    }

    public void setSummaryReportTitle(String summaryReportTitle) {
        this.summaryReportTitle = summaryReportTitle;
    }

    public String getGestionTitle() {
        return gestionTitle;
    }

    public void setGestionTitle(String gestionTitle) {
        this.gestionTitle = gestionTitle;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
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

    public Periodo getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Periodo periodo) {
        this.periodo = periodo;
    }

    public ProductiveZone getZone() {
        return zone;
    }

    public void setZone(ProductiveZone zone) {
        this.zone = zone;
    }

    @Factory(value = "periodos", scope = ScopeType.STATELESS)
    public Periodo[] getPeriodos() {
        return Periodo.values();
    }

    public GeneratedPayrollType getGeneratedPayrollType() {
        return generatedPayrollType;
    }

    public void setGeneratedPayrollType(GeneratedPayrollType generatedPayrollType) {
        this.generatedPayrollType = generatedPayrollType;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public void selectProductiveZone(ProductiveZone productiveZone) {
        try {
            productiveZone = getService().findById(ProductiveZone.class, productiveZone.getId());
            setZone(productiveZone);
        } catch (Exception ex) {
            log.error("Caught Error", ex);
        }
    }

    public String getFullNameOfProductiveZone() {
        return (zone == null ? "" : zone.getFullName());
    }

    public void setFullNameOfProductiveZone(String fullName) {

    }

    protected GenericService getService() {
        return rawMaterialPayRollService;
    }

}