package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("stateBalancesReportAction")
@Scope(ScopeType.PAGE)
public class StateBalancesReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

    //private CashAccount cashAccount;

    @In(create = true)
    VoucherUpdateAction voucherUpdateAction;
    @In
    private VoucherService voucherService;
    @In
    private VoucherAccoutingService voucherAccoutingService;


    @Create
    public void init() {
        restrictions = new String[]{
                //"voucherDetail.account=#{majorAccountingReportAction.cashAccount.accountCode}"
        };
        //sortProperty = "date";
    }

    @Override
    protected String getEjbql() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        return  " SELECT " +
                "        voucherDetail.account as account, " +
                "        cashAccount.description as description, " +
                "        SUM(voucherDetail.debit) as totalDebit, " +
                "        SUM(voucherDetail.credit) as totalCredit," +
                "        cashAccount.accountType as type " +
                "  FROM  VoucherDetail voucherDetail " +
                "  LEFT  JOIN voucherDetail.voucher voucher " +
                "  LEFT  JOIN voucherDetail.cashAccount cashAccount " +
                "  WHERE voucher.date between '"+start+"' and '"+end+"' " +
                "  AND   voucher.state <> 'ANL' " +
                "  AND   voucher.documentType <> 'SS' " +
                "  group by voucherDetail.account, cashAccount.description ";

    }

    public void generateReport() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end   = df.format(endDate);

        String documentTitle = "ESTADO DE SALDOS Y RESULTADOS";
        //String cashAccountName = this.cashAccount.getFullName();

        //Double balance = voucherAccoutingService.getBalance(startDate, cashAccount.getAccountCode());

        log.debug("Generating products produced report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("startDate",startDate);
        reportParameters.put("endDate",endDate);
        //reportParameters.put("cashAccount",cashAccountName);
        //reportParameters.put("balance",balance);

        /** Estado de Ganancias y Perdidas **/
        Double totalProfits = voucherAccoutingService.getTotalProfits(start, end);
        Double totalLosses  = voucherAccoutingService.getTotalLosses(start, end);
        Double totalResults = totalProfits - totalLosses;

        Double perdidasExcedentes       = voucherAccoutingService.perdidasExcedentesPeriodo(start, end).doubleValue(); // Perdidas/Excedentes Anterior
        Double totalPerdidasExcedentes  = perdidasExcedentes + totalResults;

        BigDecimal saldoActivoPerdida   = BigDecimal.ZERO;
        BigDecimal saldoPasivoExcedente = BigDecimal.ZERO;
        BigDecimal saldoIngreso         = BigDecimal.ZERO;
        BigDecimal saldoEgreso          = BigDecimal.ZERO;

        if (totalPerdidasExcedentes > 0)
            saldoPasivoExcedente = BigDecimalUtil.abs(BigDecimalUtil.toBigDecimal(totalPerdidasExcedentes));
        else
            saldoActivoPerdida = BigDecimalUtil.abs(BigDecimalUtil.toBigDecimal(totalPerdidasExcedentes));

        if (totalResults > 0)
            saldoEgreso = BigDecimalUtil.abs(BigDecimalUtil.toBigDecimal(totalResults));
        else
            saldoIngreso = BigDecimalUtil.abs(BigDecimalUtil.toBigDecimal(totalResults));

        reportParameters.put("saldoActivoPerdida"   , saldoActivoPerdida);
        reportParameters.put("saldoPasivoExcedente" , saldoPasivoExcedente);
        reportParameters.put("saldoEgreso"          , saldoEgreso);
        reportParameters.put("saldoIngreso"         , saldoIngreso);


        /** **/
        HashMap<String, BigDecimal> totalResultStateBalance = voucherAccoutingService.stateBalanceReport(start, end);

        reportParameters.put("totalActiveBalance"   , BigDecimalUtil.sum(totalResultStateBalance.get("totalActiveBalance")  , saldoActivoPerdida, 2) );
        reportParameters.put("totalPassiveBalance"  , BigDecimalUtil.sum(totalResultStateBalance.get("totalPassiveBalance") , saldoPasivoExcedente, 2));
        reportParameters.put("totalEgressBalance"   , BigDecimalUtil.sum(totalResultStateBalance.get("totalEgressBalance")  , saldoEgreso, 2));
        reportParameters.put("totalIncomeBalance"   , BigDecimalUtil.sum(totalResultStateBalance.get("totalIncomeBalance")  , saldoIngreso, 2));

        /*setReportFormat(ReportFormat.PDF);*/
        super.generateReport(
                "majorAccountingReport",
                "/accounting/reports/stateBalancesReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("accounting.StateBalances.TitleReport"),
                reportParameters);
    }

    /*public void clearAccount() {
        setCashAccount(null);
    }*/

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /*public CashAccount getCashAccount() {
        return cashAccount;
    }*/

    /*public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }*/
}
