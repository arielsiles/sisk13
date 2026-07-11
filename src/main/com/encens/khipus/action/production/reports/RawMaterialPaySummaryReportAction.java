package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.model.production.*;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.customers.ClientService;
import com.encens.khipus.service.production.*;
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
    @In
    CollectedRawMaterialCalculatorService collectedRawMaterialCalculatorService;
    @In
    private CompanyConfigurationService companyConfigurationService;

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

    private boolean soloDomingos;

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

        // Cabecera como la boleta de pago: dos lineas de compania (de configuracion),
        // titulo del reporte y periodo.
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "CompanyConfiguration.notFound");
        }
        reportParameters.put("empresaLinea1", companyConfiguration != null ? companyConfiguration.getTitle() : "");
        reportParameters.put("empresaLinea2", companyConfiguration != null ? companyConfiguration.getCompanyName() : "");
        reportParameters.put("tituloReporte", "RESUMEN GENERAL DE PAGO A PRODUCTORES LECHEROS");
        reportParameters.put("periodoTexto", "PERIODO DEL " + df.format(dateIni.getTime()) + " AL " + df.format(dateEnd.getTime()));

        if (soloDomingos) {
            try {
                DateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
                Date startDateSun = dateFormat2.parse(dateFormat2.format(dateIni.getTime()));
                Date endDateSun = dateFormat2.parse(dateFormat2.format(dateEnd.getTime()));
                java.util.List<Integer> sundayDays = collectedRawMaterialCalculatorService
                        .getSundayDaysWithCollection(startDateSun, endDateSun, metaProduct);
                StringBuilder sb = new StringBuilder("Domingos: ");
                for (int i = 0; i < sundayDays.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(sundayDays.get(i));
                }
                reportParameters.put("domingos_acopio", sb.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                reportParameters.put("domingos_acopio", "");
            }
        } else {
            reportParameters.put("domingos_acopio", "");
        }

        super.generateReport(
                "rawMaterialPaySummaryReportAction",
                "/production/reports/rawMaterialPaySummaryReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Report.rawMaterialPaySummaryReportAction"),
                reportParameters);
    }

    private void    addSummaryTotal(HashMap<String, Object> params) throws ParseException {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        Date startDate = dateFormat.parse(dateFormat.format(dateIni.getTime()));
        Date endDate = dateFormat.parse(dateFormat.format(dateEnd.getTime()));

        // ===== BLOQUE QUINCENA (DIAS HABILES) = reproduce el reporte anterior =====
        //   Solo las planillas NORMAL/HABIL llevan diferencia, descuentos, retencion,
        //   IT/IUE, reserva y GA. Domingos y excedentes son planillas puras.
        discounts = rawMaterialPayRollService.getDiscounts(startDate, endDate, metaProduct,
                PayRollType.NORMAL, DayType.HABIL);

        Double totalMoneyCollected = discounts.mount;
        Double totalDifferencesMoney = rawMaterialPayRollService.getSumAdjustmentFromRecords(startDate, endDate, metaProduct,
                PayRollType.NORMAL, DayType.HABIL);
        Double diffTotal = (discounts.unitPrice != 0) ? totalDifferencesMoney / discounts.unitPrice : 0.0;
        Double balanceWeightTotal = discounts.collected + diffTotal;
        Double totalMoneyBalance = totalMoneyCollected + totalDifferencesMoney;
        Double reservProducer = discounts.reserve;
        Double reserveGA = discounts.ga;

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

        iue = discounts.retention * porcentageIUE;
        it = discounts.retention - iue;
        params.put("iue", df.format(iue));
        params.put("it", df.format(it));
        params.put("reserva_productores", df.format(reservProducer));
        params.put("reserveGA", df.format(reserveGA));
        params.put("total_differences", df.format(totalDiscount));
        params.put("liquid_pay", df.format(discounts.liquid));   // Liquido Habiles

        // ===== BLOQUE DOMINGOS (planilla pura: litros x precio) =====
        RawMaterialPayRollServiceBean.Discounts dom = rawMaterialPayRollService.getDiscounts(startDate, endDate, metaProduct,
                PayRollType.NORMAL, DayType.DOMINGO);
        params.put("dom_litros", df.format(dom.collected));
        params.put("dom_pu", df.format(dom.unitPrice));
        params.put("dom_total", df.format(dom.liquid));          // Liquido Domingos

        // ===== BLOQUE EXCEDENTES (puras: litros x precio de excedente) =====
        RawMaterialPayRollServiceBean.Discounts exQ = rawMaterialPayRollService.getDiscounts(startDate, endDate, metaProduct,
                PayRollType.EXCEDENTE, DayType.HABIL);
        RawMaterialPayRollServiceBean.Discounts exD = rawMaterialPayRollService.getDiscounts(startDate, endDate, metaProduct,
                PayRollType.EXCEDENTE, DayType.DOMINGO);
        params.put("exq_litros", df.format(exQ.collected));
        params.put("exq_pu", df.format(exQ.unitPrice));
        params.put("exq_total", df.format(exQ.liquid));
        params.put("exd_litros", df.format(exD.collected));
        params.put("exd_pu", df.format(exD.unitPrice));
        params.put("exd_total", df.format(exD.liquid));
        params.put("exc_total", df.format(exQ.liquid + exD.liquid));   // Liquido Excedentes

        // ===== TOTAL DEFINITIVO = suma de los subtotales de los bloques =====
        Double liquidTotal = discounts.liquid + dom.liquid + exQ.liquid + exD.liquid;
        params.put("liquid_total", df.format(liquidTotal));
    }


    public void accountingPeriod(){

        Date startDate = DateUtils.getDate(gestion.getYear(), month.getValue()+1, periodo.getInitDay());
        Date endDate = DateUtils.getDate(gestion.getYear(), month.getValue()+1, periodo.getEndDay(month.getValue() + 1, gestion.getYear()));

        System.out.println("Fecha Inicio: " + DateUtils.format(startDate, "dd/MM/yyyy"));
        System.out.println("Fecha Fin: " + DateUtils.format(endDate, "dd/MM/yyyy"));

        // Bloque QUINCENA (dias habiles): unico con diferencia, descuentos, retencion,
        // IT/IUE, reserva y GA. Domingos y excedentes son planillas puras.
        discounts = rawMaterialPayRollService.getDiscounts(startDate, endDate, metaProduct,
                PayRollType.NORMAL, DayType.HABIL);

        Double totalMoneyCollected = discounts.mount;
        Double totalDifferencesMoney = rawMaterialPayRollService.getSumAdjustmentFromRecords(startDate, endDate, metaProduct,
                PayRollType.NORMAL, DayType.HABIL);
        Double diffTotal = (discounts.unitPrice != 0) ? totalDifferencesMoney / discounts.unitPrice : 0.0;
        Double balanceWeightTotal = discounts.collected + diffTotal;
        Double totalMoneyBalance = totalMoneyCollected + totalDifferencesMoney;
        Double reservProducer = discounts.reserve;
        Double reserveGA = discounts.ga;
        Double total = totalMoneyBalance + discounts.otherIncome;

        Double totalDiscount = discounts.alcohol + discounts.concentrated + discounts.yogurt
                + discounts.veterinary + discounts.credit + discounts.recip + discounts.retention
                + discounts.otherDiscount + reservProducer + reserveGA + discounts.commission;

        Double iue, it,porcentageIUE;
        RawMaterialPayRoll rawMaterialPayRoll =  rawMaterialPayRollService.getTotalsRawMaterialPayRoll(startDate,endDate,null,null);
        porcentageIUE = rawMaterialPayRoll.getIue() / rawMaterialPayRoll.getTaxRate();
        iue = discounts.retention * porcentageIUE;
        it = discounts.retention - iue;
        Double totalLiquid = discounts.liquid;

        // Bloques puros DOMINGOS y EXCEDENTES: solo leche cruda (debe) / acreedores
        // productores (haber) por su liquido; sin retencion ni descuentos.
        RawMaterialPayRollServiceBean.Discounts domingos = rawMaterialPayRollService.getDiscounts(startDate, endDate, metaProduct,
                PayRollType.NORMAL, DayType.DOMINGO);
        RawMaterialPayRollServiceBean.Discounts excedenteQuincena = rawMaterialPayRollService.getDiscounts(startDate, endDate, metaProduct,
                PayRollType.EXCEDENTE, DayType.HABIL);
        RawMaterialPayRollServiceBean.Discounts excedenteDomingo = rawMaterialPayRollService.getDiscounts(startDate, endDate, metaProduct,
                PayRollType.EXCEDENTE, DayType.DOMINGO);
        Double domingoLiquid = domingos.liquid;
        Double excedenteLiquid = excedenteQuincena.liquid + excedenteDomingo.liquid;


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
        voucherDebit.setGloss("ACOPIO QUINCENA (HABILES)");
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
            voucherCredt4.setGloss("LIQUIDO QUINCENA (HABILES)");
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

        // ===== BLOQUE DOMINGOS (puro): Debe leche cruda / Haber acreedores productores =====
        if (domingoLiquid > 0){
            VoucherDetail domingoDebit = new VoucherDetail();
            domingoDebit.setAccount(Constants.ACCOUNT_LECHECRUDA);
            domingoDebit.setDebit(BigDecimalUtil.toBigDecimal(domingoLiquid));
            domingoDebit.setCredit(BigDecimal.ZERO);
            domingoDebit.setCurrency(FinancesCurrencyType.P);
            domingoDebit.setExchangeAmount(BigDecimal.ONE);
            domingoDebit.setDebitMe(BigDecimal.ZERO);
            domingoDebit.setCreditMe(BigDecimal.ZERO);
            domingoDebit.setGloss("ACOPIO DOMINGOS");
            voucher.addVoucherDetail(domingoDebit);

            VoucherDetail domingoCredit = new VoucherDetail();
            domingoCredit.setAccount(Constants.ACCOUNT_ACREEDORES_BIENESSERVICIOS);
            domingoCredit.setDebit(BigDecimal.ZERO);
            domingoCredit.setCredit(BigDecimalUtil.toBigDecimal(domingoLiquid));
            domingoCredit.setCurrency(FinancesCurrencyType.P);
            domingoCredit.setExchangeAmount(BigDecimal.ONE);
            domingoCredit.setDebitMe(BigDecimal.ZERO);
            domingoCredit.setCreditMe(BigDecimal.ZERO);
            domingoCredit.setProviderCode(Constants.PROVIDER_CODE_PRODUCTORES);
            domingoCredit.setGloss("LIQUIDO DOMINGOS");
            voucher.addVoucherDetail(domingoCredit);
        }

        // ===== BLOQUE EXCEDENTES (puro): Debe leche cruda / Haber acreedores productores =====
        if (excedenteLiquid > 0){
            VoucherDetail excedenteDebit = new VoucherDetail();
            excedenteDebit.setAccount(Constants.ACCOUNT_LECHECRUDA);
            excedenteDebit.setDebit(BigDecimalUtil.toBigDecimal(excedenteLiquid));
            excedenteDebit.setCredit(BigDecimal.ZERO);
            excedenteDebit.setCurrency(FinancesCurrencyType.P);
            excedenteDebit.setExchangeAmount(BigDecimal.ONE);
            excedenteDebit.setDebitMe(BigDecimal.ZERO);
            excedenteDebit.setCreditMe(BigDecimal.ZERO);
            excedenteDebit.setGloss("ACOPIO EXCEDENTES");
            voucher.addVoucherDetail(excedenteDebit);

            VoucherDetail excedenteCredit = new VoucherDetail();
            excedenteCredit.setAccount(Constants.ACCOUNT_ACREEDORES_BIENESSERVICIOS);
            excedenteCredit.setDebit(BigDecimal.ZERO);
            excedenteCredit.setCredit(BigDecimalUtil.toBigDecimal(excedenteLiquid));
            excedenteCredit.setCurrency(FinancesCurrencyType.P);
            excedenteCredit.setExchangeAmount(BigDecimal.ONE);
            excedenteCredit.setDebitMe(BigDecimal.ZERO);
            excedenteCredit.setCreditMe(BigDecimal.ZERO);
            excedenteCredit.setProviderCode(Constants.PROVIDER_CODE_PRODUCTORES);
            excedenteCredit.setGloss("LIQUIDO EXCEDENTES");
            voucher.addVoucherDetail(excedenteCredit);
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

    public boolean isSoloDomingos() {
        return soloDomingos;
    }

    public void setSoloDomingos(boolean soloDomingos) {
        this.soloDomingos = soloDomingos;
    }

    protected GenericService getService() {
        return rawMaterialPayRollService;
    }

}