package com.encens.khipus.action.accounting;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * OrganizationAction
 *
 * @author
 * @version 2.26
 */
@Name("diaryMajorAction")
@Scope(ScopeType.CONVERSATION)
public class DiaryMajorAction extends GenericAction {

    private Date startDate;
    private Date endDate;
    private BigDecimal previosBalance = BigDecimal.ZERO;
    private BigDecimal totalDebit = BigDecimal.ZERO;
    private BigDecimal totalCredit = BigDecimal.ZERO;

    private CashAccount account;
    private List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();

    @In
    private VoucherAccoutingService voucherAccoutingService;

    public void searchMajor(){
        List<VoucherDetail> details = voucherAccoutingService.getDetailsByDates(account.getAccountCode(), startDate, endDate);
        Double prevBalance = voucherAccoutingService.getBalance(startDate, account.getAccountCode());

        setPreviosBalance(BigDecimalUtil.toBigDecimal(prevBalance));
        setVoucherDetails(details);

        /** **/
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;

        for (VoucherDetail detail : details) {
            debit = BigDecimalUtil.sum(debit, detail.getDebit());
            credit = BigDecimalUtil.sum(credit, detail.getCredit());
        }

        setTotalDebit(debit);
        setTotalCredit(credit);

    }

    public BigDecimal getUpdateBalance(VoucherDetail voucherDetail){

        BigDecimal diference = BigDecimalUtil.subtract(voucherDetail.getDebit(), voucherDetail.getCredit());
        BigDecimal balance = BigDecimalUtil.sum(previosBalance, diference);

        setPreviosBalance(balance);

        return balance;
    }

    public void clearAccount() {
        setAccount(null);
        setVoucherDetails(new ArrayList<VoucherDetail>());
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

    public CashAccount getAccount() {
        return account;
    }

    public void setAccount(CashAccount account) {
        this.account = account;
    }

    public List<VoucherDetail> getVoucherDetails() {
        return voucherDetails;
    }

    public void setVoucherDetails(List<VoucherDetail> voucherDetails) {
        this.voucherDetails = voucherDetails;
    }

    public BigDecimal getPreviosBalance() {
        return previosBalance;
    }

    public void setPreviosBalance(BigDecimal previosBalance) {
        this.previosBalance = previosBalance;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }
}
