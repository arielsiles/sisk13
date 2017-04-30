package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.WarehouseVoucher;
import com.encens.khipus.model.warehouse.WarehouseVoucherPK;
import com.encens.khipus.model.warehouse.WarehouseVoucherState;
import com.encens.khipus.util.ListEntityManagerName;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Name("accountEntriesDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('APPROVEDALLACCOUNTENTRIES','VIEW')}")
public class AccountEntriesDataModel extends QueryDataModel<String,Voucher> {
    private String transactionNumber;
    private WarehouseVoucherState state;
    private Date startDate;
    private Date endDate;
    private Provider provider;
    private List<String> transactionsNumbers;

    private static final String[] RESTRICTIONS = {
            "voucher.transactionNumber in (#{accountEntriesDataModel.transactionsNumbers})",
            //"voucher.state = #{accountEntriesDataModel.state}",
            //"voucher.provider = #{accountEntriesDataModel.provider}",
            "voucher.date >= #{accountEntriesDataModel.startDate}",
            "voucher.date <= #{accountEntriesDataModel.endDate}"
    };

    @Create
    public void init() {
        transactionsNumbers = new ArrayList<String>();
        transactionsNumbers.add("0");
        sortProperty = "voucher.transactionNumber";
    }

    @Override
    public String getEjbql() {
        return "select voucher" +
                " from Voucher voucher";
                //+ " left join fetch voucher.provider provider";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public WarehouseVoucherState getState() {
        return state;
    }

    public void setState(WarehouseVoucherState state) {
        this.state = state;
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

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public void clear() {
        setTransactionNumber(null);
        setState(null);
        setProvider(null);
        setStartDate(null);
        setEndDate(null);
        transactionsNumbers.clear();
        transactionsNumbers.add("0");
        super.clear();
    }

    public List<String> getTransactionsNumbers() {
        return transactionsNumbers;
    }

    public void setTransactionsNumbers(List<String> transactionsNumbers) {
        this.transactionsNumbers = transactionsNumbers;
    }
}
