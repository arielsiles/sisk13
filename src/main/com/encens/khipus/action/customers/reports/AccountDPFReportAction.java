package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.customers.AccountAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.customers.Account;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.MoneyUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("accountDPFReportAction")
@Scope(ScopeType.PAGE)
public class AccountDPFReportAction extends GenericReportAction {

    private Long accountId;

    @In(create = true)
    private AccountAction accountAction;

    @Create
    public void init() {
        //restrictions = new String[]{"account.id = #{accountDPFReportAction.accountId}"};
        restrictions = new String[]{};
        //sortProperty = "purchaseOrder.id, purchaseOrderDetail.detailNumber";
    }


    protected String getEjbql() {
        //return "select account from Account account";
        return "";
    }

    public void generateReport(Account account) {

        accountId = account.getId();

        String ci           = account.getPartner().getIdNumber();
        String cert         = account.getCode();
        Date openDate       = account.getOpeningDate();
        Date expirationDate = account.getExpirationDate();
        String name         = account.getPartner().getFullName();
        String beneficiary1 = account.getBeneficiary1();
        String beneficiary2 = account.getBeneficiary2();
        String address      = account.getPartner().getHomeAddress();

        MoneyUtil money = new MoneyUtil();
        String literalCapital   = money.Convertir(account.getCapital().toString(), true);
        BigDecimal capital      = account.getCapital();
        BigDecimal interest     = accountAction.calculateInterestForDays(account.getAccountType().getDays(), capital, account.getAccountType().getInta());
        BigDecimal rciva        = BigDecimalUtil.multiply(interest, Constants.VAT);
        String term             = account.getAccountType().getDays().toString();
        String rate             = (new Integer(account.getAccountType().getInta().intValue())).toString();

        BigDecimal totalAmount  = BigDecimalUtil.sum(capital, interest);
                   totalAmount  = BigDecimalUtil.subtract(totalAmount, rciva);

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("ci", ci);
        reportParameters.put("cert", cert);
        reportParameters.put("openDate", openDate);
        reportParameters.put("expirationDate", expirationDate);
        reportParameters.put("name", name);
        reportParameters.put("beneficiary1", beneficiary1);
        reportParameters.put("beneficiary2", beneficiary2);
        reportParameters.put("address", address);
        reportParameters.put("literalCapital", literalCapital);
        reportParameters.put("capital", capital);
        reportParameters.put("interest", interest);
        reportParameters.put("rciva", rciva);
        reportParameters.put("term", term);
        reportParameters.put("rate", rate);
        reportParameters.put("totalAmount", totalAmount);

        setReportFormat(ReportFormat.PDF);

        super.generateReport(
                "accountDPFReport",
                "/customers/reports/accountDPFReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Account.report.accountDPF.title"),
                reportParameters);
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
