package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.accounting.DocType;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.finances.FinanceUser;
import com.encens.khipus.model.finances.FinancesModule;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.finances.VoucherDetail;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 1.0
 */

@Local
public interface VoucherService {

    void create(Voucher voucher);

    Voucher createBody(Voucher voucher);

    /**
     * Creates the voucher detail
     *
     * @param voucher           The voucher for change if is necessary
     * @param voucherDetailList The detail list that will be persisted
     */
    void createDetail(Voucher voucher, List<VoucherDetail> voucherDetailList);

    void deleteVoucher(Voucher voucher);

    void approvedAllVoucherEntries(String defaultCompanyNumber, BusinessUnit businessUnit, Date startDate, Date endDate, String numberTransction, FinanceUser financeUser, FinancesModule financesModule) throws CompanyConfigurationNotFoundException;

    public List<VoucherServiceBean.ObsApprovedEntries> getInfoTrasaction(FinancesModule financesModule, Date startDate, Date endDate);

    public List<VoucherServiceBean.ObsApprovedEntries> getInfoTrasaction(String financesModule, String numberTransaction, Date startDate, Date endDate);

    List<VoucherServiceBean.ObsApprovedEntries> getInfoTrasaction( String numberTransction);

    public void approvedAllVoucherEntries(String defaultCompanyNumber,
                                          String businessUnit,
                                          Date startDate,
                                          Date endDate,
                                          String numberTransction,
                                          String financeUser,
                                          String financesModule) throws CompanyConfigurationNotFoundException;

    DocType getDocType(String name);

    Voucher findVoucherByNoTrans(String transactionNumber);

    List<VoucherServiceBean.VoucherTransaction>getTransactionMajorAccounting(String start, String end, String cashAccount);

    List<String> getMinMaxNumber(Date start, Date end, String documentType);
}
