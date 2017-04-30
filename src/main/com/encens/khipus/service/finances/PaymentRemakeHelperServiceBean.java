package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.FinanceDocument;
import com.encens.khipus.model.finances.FinanceDocumentPk;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.service.accouting.VoucherAccoutingService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

/**
 * @author
 * @version 2.24
 */
@Stateless
@Name("paymentRemakeHelperService")
@AutoCreate
public class PaymentRemakeHelperServiceBean extends GenericServiceBean implements PaymentRemakeHelperService {

    @In
    private VoucherAccoutingService voucherAccoutingService;

    public FinanceDocument getFinanceDocument(String companyNumber, String transactionNumber) {
        return getEntityManager().find(FinanceDocument.class, new FinanceDocumentPk(companyNumber, transactionNumber));
    }

    public Voucher getVoucher(String transactionNumber) {
        return voucherAccoutingService.getVoucher(transactionNumber);
        //return getEntityManager().find(Voucher.class, transactionNumber);
    }

    public Boolean isStoredInAccountingMovementDetail(String companyNumber, String transactionNumber) {
        Long counter = (Long) getEntityManager().createNamedQuery("AccountingMovementDetail.countByTransactionNumber")
                .setParameter("companyNumber", companyNumber)
                .setParameter("transactionNumber", transactionNumber)
                .getSingleResult();
        return null != counter && counter > 0;
    }

    public void nullifyPaymentVoucher(String transactionNumber) {
        Voucher voucher = getVoucher(transactionNumber);
        if (null != voucher) {
            voucher.nullify();
            getEntityManager().merge(voucher);
        }
    }

    public String getOldDocumentNumber(String companyNumber, String transactionNumber) {
        FinanceDocument financeDocument = getFinanceDocument(
                companyNumber,
                transactionNumber);
        if (null != financeDocument) {
            return financeDocument.getDocumentNumber();
        }

        return null;
    }

    public void updateDocumentNumberInAccountEntry(String transactionNumber, String documentNumber) {
        Voucher voucher = getVoucher(transactionNumber);
        if (null != voucher) {
            voucher.setDocumentNumber(documentNumber);
            getEntityManager().merge(voucher);
        }
    }
}
