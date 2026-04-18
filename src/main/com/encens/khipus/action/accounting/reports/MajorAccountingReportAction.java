package com.encens.khipus.action.accounting.reports;

import com.encens.khipus.action.accounting.VoucherUpdateAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CashAccountType;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.service.finances.CashAccountService;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.service.finances.VoucherServiceBean;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.hssf.usermodel.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("majorAccountingReportAction")
@Scope(ScopeType.PAGE)
public class MajorAccountingReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

    private CashAccount cashAccount;
    private CashAccountType cashAccountType;

    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;
    @In(create = true)
    VoucherUpdateAction voucherUpdateAction;
    @In
    private VoucherService voucherService;
    @In
    private VoucherAccoutingService voucherAccoutingService;
    @In
    private CashAccountService cashAccountService;
    @In
    protected Map<String, String> messages;

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
                "        voucher.date, " +
                "        voucherDetail.account as account, " +
                "        voucher.documentType, " +
                "        voucher.documentNumber as transactionNumber, " +
                "        voucher.gloss, " +
                "        voucherDetail.debit as debit, " +
                "        voucherDetail.credit as credit " +
                "  FROM  Voucher voucher " +
                "  LEFT  JOIN voucher.voucherDetailList voucherDetail " +
                "  WHERE voucher.date between '"+start+"' and '"+end+"' " +
                "  AND   voucherDetail.account = '"+ cashAccount.getAccountCode() +"'" +
                "  AND   voucher.state <> 'ANL'" +
                "  order by voucher.date";
    }

    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        if (cashAccountType != null) {
            generateGroupedReport(companyConfiguration);
            return;
        }

        if (cashAccount == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "MajorAccounting.selectAccountOrType");
            return;
        }

        List<MajorAccountingRow> rows = buildSingleAccountRows();

        try {
            if (getReportFormat() != null && (getReportFormat().name().equals("XLS") || getReportFormat().name().equals("XLSX"))) {
                exportarExcelAgrupado(rows, companyConfiguration);
            } else {
                exportarPDFAgrupado(rows, companyConfiguration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int natureSign(CashAccount ca) {
        if (ca == null || ca.getAccountType() == null) return +1;
        CashAccountType t = ca.getAccountType();
        boolean debtor = (t == CashAccountType.A || t == CashAccountType.E);
        if (Boolean.TRUE.equals(ca.getRegulating())) debtor = !debtor;
        return debtor ? +1 : -1;
    }

    public void generateGroupedReport(CompanyConfiguration companyConfiguration) {

        List<MajorAccountingRow> rows = buildGroupedRows();

        if (rows.isEmpty()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "MajorAccounting.noDataForType");
            return;
        }

        try {
            if (getReportFormat() != null && (getReportFormat().name().equals("XLS") || getReportFormat().name().equals("XLSX"))) {
                exportarExcelAgrupado(rows, companyConfiguration);
            } else {
                exportarPDFAgrupado(rows, companyConfiguration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<MajorAccountingRow> buildGroupedRows() {

        String start = DateUtils.format(startDate, "yyyy-MM-dd");
        String end = DateUtils.format(endDate, "yyyy-MM-dd");

        List<CashAccount> cashAccountList = cashAccountService.findCashAccountListByType(this.cashAccountType);
        List<MajorAccountingRow> rows = new ArrayList<MajorAccountingRow>();

        if (cashAccountList == null || cashAccountList.isEmpty()) {
            return rows;
        }

        List<String> accountCodes = new ArrayList<String>(cashAccountList.size());
        Map<String, CashAccount> accountByCode = new HashMap<String, CashAccount>();
        for (CashAccount ca : cashAccountList) {
            accountCodes.add(ca.getAccountCode());
            accountByCode.put(ca.getAccountCode(), ca);
        }

        Map<String, Double> initialBalancesMap = voucherAccoutingService.getBalancesByAccountCodes(startDate, accountCodes);

        List<VoucherServiceBean.VoucherTransaction> transactions =
                voucherService.getTransactionsByAccountCodes(start, end, accountCodes);

        String currentCode = null;
        CashAccount currentAccount = null;
        BigDecimal currentInitial = BigDecimal.ZERO;
        BigDecimal running = BigDecimal.ZERO;
        int currentSign = 1;

        for (VoucherServiceBean.VoucherTransaction t : transactions) {
            String code = t.getAccount();
            if (!code.equals(currentCode)) {
                currentCode = code;
                currentAccount = accountByCode.get(code);
                currentSign = natureSign(currentAccount);
                Double ib = initialBalancesMap.get(code);
                BigDecimal rawIb = ib != null ? BigDecimal.valueOf(ib) : BigDecimal.ZERO;
                currentInitial = currentSign == 1 ? rawIb : rawIb.negate();
                running = currentInitial;
            }

            BigDecimal debit = t.getDebit() != null ? t.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = t.getCredit() != null ? t.getCredit() : BigDecimal.ZERO;
            BigDecimal delta = debit.subtract(credit);
            if (currentSign == -1) delta = delta.negate();
            running = running.add(delta);

            String gloss = t.getGloss() != null ? t.getGloss().replaceAll("[\n\r]", " ") : "";

            rows.add(new MajorAccountingRow(
                    code,
                    currentAccount != null ? currentAccount.getFullName() : code,
                    currentInitial,
                    t.getDate(),
                    t.getDocumentType(),
                    t.getDocumentNumber(),
                    gloss,
                    debit,
                    credit,
                    running
            ));
        }

        Set<String> codesWithMovement = new HashSet<String>();
        for (MajorAccountingRow r : rows) {
            codesWithMovement.add(r.getAccountCode());
        }

        List<MajorAccountingRow> noMovementRows = new ArrayList<MajorAccountingRow>();
        for (CashAccount ca : cashAccountList) {
            String code = ca.getAccountCode();
            if (codesWithMovement.contains(code)) continue;

            Double ib = initialBalancesMap.get(code);
            if (ib == null || ib == 0.0) continue;

            int sign = natureSign(ca);
            BigDecimal rawIb = BigDecimal.valueOf(ib);
            BigDecimal initialBalance = sign == 1 ? rawIb : rawIb.negate();
            noMovementRows.add(new MajorAccountingRow(
                    code, ca.getFullName(), initialBalance,
                    "", "", "", "",
                    BigDecimal.ZERO, BigDecimal.ZERO, initialBalance));
        }

        rows.addAll(noMovementRows);

        Collections.sort(rows, new Comparator<MajorAccountingRow>() {
            public int compare(MajorAccountingRow a, MajorAccountingRow b) {
                return a.getAccountCode().compareTo(b.getAccountCode());
            }
        });

        return rows;
    }

    private List<MajorAccountingRow> buildSingleAccountRows() {

        String start = DateUtils.format(startDate, "yyyy-MM-dd");
        String end = DateUtils.format(endDate, "yyyy-MM-dd");

        List<MajorAccountingRow> rows = new ArrayList<MajorAccountingRow>();

        int sign = natureSign(cashAccount);
        Double ib = voucherAccoutingService.getBalance(startDate, cashAccount.getAccountCode());
        BigDecimal rawIb = ib != null ? BigDecimal.valueOf(ib) : BigDecimal.ZERO;
        BigDecimal initialBalance = sign == 1 ? rawIb : rawIb.negate();

        List<VoucherServiceBean.VoucherTransaction> transactionList =
                voucherService.getTransactionMajorAccounting(start, end, cashAccount.getAccountCode());

        BigDecimal running = initialBalance;

        if (transactionList != null) {
            for (VoucherServiceBean.VoucherTransaction t : transactionList) {
                BigDecimal debit = t.getDebit() != null ? t.getDebit() : BigDecimal.ZERO;
                BigDecimal credit = t.getCredit() != null ? t.getCredit() : BigDecimal.ZERO;
                BigDecimal delta = debit.subtract(credit);
                if (sign == -1) delta = delta.negate();
                running = running.add(delta);

                String gloss = t.getGloss() != null ? t.getGloss().replaceAll("[\n\r]", " ") : "";

                rows.add(new MajorAccountingRow(
                        cashAccount.getAccountCode(),
                        cashAccount.getFullName(),
                        initialBalance,
                        t.getDate(),
                        t.getDocumentType(),
                        t.getDocumentNumber(),
                        gloss,
                        debit,
                        credit,
                        running
                ));
            }
        }

        if (rows.isEmpty()) {
            rows.add(new MajorAccountingRow(
                    cashAccount.getAccountCode(),
                    cashAccount.getFullName(),
                    initialBalance,
                    "", "", "", "",
                    BigDecimal.ZERO, BigDecimal.ZERO, initialBalance));
        }

        return rows;
    }

    public void exportarPDFAgrupado(List<MajorAccountingRow> rows, CompanyConfiguration companyConfiguration) throws Exception {

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("documentTitle", messages.get("MajorAccounting.report.title"));
        parameters.put("companyName", companyConfiguration.getCompanyName());
        parameters.put("systemName", companyConfiguration.getSystemName());
        parameters.put("locationName", companyConfiguration.getLocationName());
        parameters.put("startDate", startDate);
        parameters.put("endDate", endDate);
        parameters.put("cashAccountType", cashAccountType != null ? messages.get(cashAccountType.getResourceKey()) : "");

        File jrxmlFile = new File(JSFUtil.getRealPath("/accounting/reports/majorAccountingGroupedReport.jrxml"));
        String jrxmlContent = new String(java.nio.file.Files.readAllBytes(jrxmlFile.toPath()), "UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(jrxmlContent.getBytes("UTF-8"));
        JasperReport jasperReport = JasperCompileManager.compileReport(bais);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(rows));

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=LibroMayorAgrupado.pdf");
        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void exportarExcelAgrupado(List<MajorAccountingRow> rows, CompanyConfiguration companyConfiguration) throws IOException {

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Mayor");

        HSSFCellStyle headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerStyle.setFont(headerFont);

        HSSFCellStyle numberStyle = workbook.createCellStyle();
        HSSFDataFormat numFormat = workbook.createDataFormat();
        numberStyle.setDataFormat(numFormat.getFormat("#,##0.00"));

        HSSFCellStyle numberBoldStyle = workbook.createCellStyle();
        numberBoldStyle.setDataFormat(numFormat.getFormat("#,##0.00"));
        numberBoldStyle.setFont(headerFont);

        int rowNum = 0;
        HSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getCompanyName());
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getLocationName());

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getSystemName());

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(messages.get("MajorAccounting.report.title"));
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Periodo:");
        row.createCell(1).setCellValue(sdf.format(startDate) + " - " + sdf.format(endDate));

        if (cashAccountType != null) {
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Tipo:");
            row.createCell(1).setCellValue(messages.get(cashAccountType.getResourceKey()));
        }

        rowNum++;

        String[] headers = {"Fecha", "Doc", "No.", "Glosa", "Debe", "Haber", "Saldo"};
        String currentAccount = null;
        BigDecimal groupDebit = BigDecimal.ZERO;
        BigDecimal groupCredit = BigDecimal.ZERO;
        BigDecimal groupFinalBalance = BigDecimal.ZERO;
        boolean hasOpenGroup = false;

        for (MajorAccountingRow r : rows) {

            if (!r.getAccountCode().equals(currentAccount)) {

                if (hasOpenGroup) {
                    row = sheet.createRow(rowNum++);
                    row.createCell(3).setCellValue("TOTALES");
                    row.getCell(3).setCellStyle(headerStyle);
                    HSSFCell td = row.createCell(4); td.setCellValue(groupDebit.doubleValue()); td.setCellStyle(numberBoldStyle);
                    HSSFCell tc = row.createCell(5); tc.setCellValue(groupCredit.doubleValue()); tc.setCellStyle(numberBoldStyle);
                    HSSFCell tb = row.createCell(6); tb.setCellValue(groupFinalBalance.doubleValue()); tb.setCellStyle(numberBoldStyle);
                    rowNum++;
                }

                currentAccount = r.getAccountCode();
                groupDebit = BigDecimal.ZERO;
                groupCredit = BigDecimal.ZERO;
                groupFinalBalance = r.getInitialBalance() != null ? r.getInitialBalance() : BigDecimal.ZERO;
                hasOpenGroup = true;

                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Cuenta:");
                row.getCell(0).setCellStyle(headerStyle);
                row.createCell(1).setCellValue(r.getAccountName());
                row.getCell(1).setCellStyle(headerStyle);

                row = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                row = sheet.createRow(rowNum++);
                row.createCell(3).setCellValue("Saldo inicial");
                row.getCell(3).setCellStyle(headerStyle);
                HSSFCell ib = row.createCell(6);
                ib.setCellValue(r.getInitialBalance() != null ? r.getInitialBalance().doubleValue() : 0);
                ib.setCellStyle(numberBoldStyle);
            }

            boolean emptyDate = r.getDate() == null || r.getDate().isEmpty();
            if (emptyDate && (r.getDebit() == null || r.getDebit().signum() == 0)
                    && (r.getCredit() == null || r.getCredit().signum() == 0)) {
                continue;
            }

            BigDecimal debit = r.getDebit() != null ? r.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = r.getCredit() != null ? r.getCredit() : BigDecimal.ZERO;
            groupDebit = groupDebit.add(debit);
            groupCredit = groupCredit.add(credit);
            groupFinalBalance = r.getBalance() != null ? r.getBalance() : groupFinalBalance;

            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getDate() != null ? r.getDate() : "");
            row.createCell(1).setCellValue(r.getDocumentType() != null ? r.getDocumentType() : "");
            row.createCell(2).setCellValue(r.getDocumentNumber() != null ? r.getDocumentNumber() : "");
            row.createCell(3).setCellValue(r.getGloss() != null ? r.getGloss() : "");

            HSSFCell dc = row.createCell(4); dc.setCellValue(debit.doubleValue()); dc.setCellStyle(numberStyle);
            HSSFCell cc = row.createCell(5); cc.setCellValue(credit.doubleValue()); cc.setCellStyle(numberStyle);
            HSSFCell bc = row.createCell(6); bc.setCellValue(r.getBalance() != null ? r.getBalance().doubleValue() : 0); bc.setCellStyle(numberStyle);
        }

        if (hasOpenGroup) {
            row = sheet.createRow(rowNum++);
            row.createCell(3).setCellValue("TOTALES");
            row.getCell(3).setCellStyle(headerStyle);
            HSSFCell td = row.createCell(4); td.setCellValue(groupDebit.doubleValue()); td.setCellStyle(numberBoldStyle);
            HSSFCell tc = row.createCell(5); tc.setCellValue(groupCredit.doubleValue()); tc.setCellStyle(numberBoldStyle);
            HSSFCell tb = row.createCell(6); tb.setCellValue(groupFinalBalance.doubleValue()); tb.setCellStyle(numberBoldStyle);
        }

        sheet.setColumnWidth(0, 90 * 256 / 7);
        sheet.setColumnWidth(1, 60 * 256 / 7);
        sheet.setColumnWidth(2, 60 * 256 / 7);
        sheet.setColumnWidth(3, 480 * 256 / 7);
        sheet.setColumnWidth(4, 90 * 256 / 7);
        sheet.setColumnWidth(5, 90 * 256 / 7);
        sheet.setColumnWidth(6, 90 * 256 / 7);

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-disposition", "attachment; filename=LibroMayorAgrupado.xls");
        ServletOutputStream stream = response.getOutputStream();
        workbook.write(stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void generateCSV(){
        final String fileName = "c:/TMP/Mayor-" + messages.get(this.cashAccountType.getResourceKey()) + ".csv";
        final String NEXT_LINE = "\n";
        String delim = "|";

        String start = DateUtils.format(startDate, "yyyy-MM-dd");
        String end = DateUtils.format(endDate, "yyyy-MM-dd");


        try {
            FileWriter fw = new FileWriter(fileName);

            //List<CashAccount> cashAccountList = cashAccountService.findCashAccountList();

            List<CashAccount> cashAccountList = cashAccountService.findCashAccountListByType(this.cashAccountType);

            boolean flag = false;
            fw.append("CUENTA").append(delim).append("FECHA").append(delim).append("TIPO").append(delim).append("NO_DOC").append(delim).append("GLOSA").append(delim).append("DEBE").append(delim).append("HABER").append(delim).append("SALDO").append(NEXT_LINE);
            for (CashAccount ca:cashAccountList){

                BigDecimal debit  = BigDecimal.ZERO;
                BigDecimal credit = BigDecimal.ZERO;
                // Double balance = voucherAccoutingService.getBalance(startDate, ca.getAccountCode());
                Double balance = 0.0;

                List<VoucherServiceBean.VoucherTransaction> voucherTransactionList = voucherService.getTransactionMajorAccounting(start, end, ca.getAccountCode());
                for(VoucherServiceBean.VoucherTransaction voucherTransaction:voucherTransactionList){
                    debit = voucherTransaction.getDebit();
                    credit = voucherTransaction.getCredit();
                    balance = balance.doubleValue() + debit.doubleValue() - credit.doubleValue();

                    //if (balance > 0){
                        flag = true;

                        String gloss = voucherTransaction.getGloss().replaceAll("[\n\r]", "");

                        fw.append(ca.getFullName()).append(delim);
                        fw.append(voucherTransaction.getDate()).append(delim);
                        fw.append(voucherTransaction.getDocumentType()).append(delim);
                        fw.append(voucherTransaction.getDocumentNumber()).append(delim);
                        fw.append(gloss).append(delim);
                        fw.append(voucherTransaction.getDebit().toString()).append(delim);
                        fw.append(voucherTransaction.getCredit().toString()).append(delim);
                        fw.append(balance.toString()).append(NEXT_LINE);
                    //}
                }
                fw.append(NEXT_LINE);
                /*if (flag){
                    fw.append(NEXT_LINE);
                    flag = false;
                }*/
            }

            fw.flush();
            fw.close();

        } catch (IOException e) { e.printStackTrace(); }

    }


    private static void crearArchivoCSV(String file, String delim) {
        final String NEXT_LINE = "\n";
        try {
            FileWriter fw = new FileWriter(file);

            fw.append("testing").append(delim);
            fw.append("123").append(NEXT_LINE);

            fw.append("value1");
            fw.append(delim);
            fw.append("312");
            fw.append(NEXT_LINE);

            fw.append("anotherthing,888\n");

            fw.flush();
            fw.close();
        } catch (IOException e) {
            // Error al crear el archivo, por ejemplo, el archivo
            // está actualmente abierto.
            e.printStackTrace();
        }
    }

    public void clearAccount() {
        setCashAccount(null);
    }

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

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public CashAccountType getCashAccountType() {
        return cashAccountType;
    }

    public void setCashAccountType(CashAccountType cashAccountType) {
        this.cashAccountType = cashAccountType;
    }

    public List<CashAccountType> getAllowedCashAccountTypes() {
        List<CashAccountType> list = new ArrayList<CashAccountType>();
        list.add(CashAccountType.A);
        list.add(CashAccountType.P);
        list.add(CashAccountType.C);
        list.add(CashAccountType.E);
        list.add(CashAccountType.I);
        return list;
    }

    public static class MajorAccountingRow {
        private String accountCode;
        private String accountName;
        private BigDecimal initialBalance;
        private String date;
        private String documentType;
        private String documentNumber;
        private String gloss;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal balance;

        public MajorAccountingRow(String accountCode, String accountName, BigDecimal initialBalance,
                                  String date, String documentType, String documentNumber, String gloss,
                                  BigDecimal debit, BigDecimal credit, BigDecimal balance) {
            this.accountCode = accountCode;
            this.accountName = accountName;
            this.initialBalance = initialBalance;
            this.date = date;
            this.documentType = documentType;
            this.documentNumber = documentNumber;
            this.gloss = gloss;
            this.debit = debit;
            this.credit = credit;
            this.balance = balance;
        }

        public String getAccountCode() { return accountCode; }
        public String getAccountName() { return accountName; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public String getDate() { return date; }
        public String getDocumentType() { return documentType; }
        public String getDocumentNumber() { return documentNumber; }
        public String getGloss() { return gloss; }
        public BigDecimal getDebit() { return debit; }
        public BigDecimal getCredit() { return credit; }
        public BigDecimal getBalance() { return balance; }
    }
}
