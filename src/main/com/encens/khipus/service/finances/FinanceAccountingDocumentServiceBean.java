package com.encens.khipus.service.finances;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.DischargeDocument;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.model.purchases.*;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.purchases.GlossGeneratorService;
import com.encens.khipus.util.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("financeAccountingDocumentService")
@AutoCreate
public class FinanceAccountingDocumentServiceBean extends GenericServiceBean implements FinanceAccountingDocumentService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @In
    private VoucherService voucherService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private GlossGeneratorService glossGeneratorService;

    public void validatePK(FinanceAccountingDocumentPk id) throws DuplicatedFinanceAccountingDocumentException {
        FinanceAccountingDocument element = eventEm.find(FinanceAccountingDocument.class, id);
        if (null != element) {
            throw new DuplicatedFinanceAccountingDocumentException(id);
        }
    }

    public void validatePK(PurchaseDocument document) throws DuplicatedFinanceAccountingDocumentException {
        if (document.isInvoiceDocument()) {
            validatePK(buildFinanceAccountingDocumentPk(document));
        }
    }

    public void validatePK(DischargeDocument document) throws DuplicatedFinanceAccountingDocumentException {
        validatePK(buildFinanceAccountingDocumentPk(document));
    }

    public void validatePK(CollectionDocument collectionDocument) throws DuplicatedFinanceAccountingDocumentException {
        if (collectionDocument != null && CollectionDocumentType.INVOICE.equals(collectionDocument.getCollectionDocumentType())) {
            validatePK(buildFinanceAccountingDocumentPk(collectionDocument));
        }
    }

    public void createFinanceAccountingDocument(PurchaseDocument document) {
        FinanceAccountingDocument financeDocument = new FinanceAccountingDocument();
        financeDocument.setId(buildFinanceAccountingDocumentPk(document));
        financeDocument.setAmount(document.getAmount());
        financeDocument.setControlCode(document.getControlCode());
        financeDocument.setDate(document.getDate());
        financeDocument.setExempt(document.getExempt());
        financeDocument.setIce(document.getIce());
        financeDocument.setNit(document.getNit());
        financeDocument.setSocialName(document.getName());
        financeDocument.setTax(document.getIva());
        financeDocument.setTransactionNumber(document.getTransactionNumber());

        getEntityManager().persist(financeDocument);
    }

    public void createAccountingVoucher(PurchaseDocument document)
            throws CompanyConfigurationNotFoundException {
        PurchaseOrder purchaseOrder = eventEm.find(PurchaseOrder.class, document.getPurchaseOrder().getId());
        if (PurchaseOrderState.LIQ.equals(purchaseOrder.getState())) {
            if(purchaseOrder.getWithBill() != null)
            {if(purchaseOrder.getWithBill().compareTo(Constants.WITH_BILL) != 0)
                createAccountingVoucher(purchaseOrder, document);
            }else{
                createAccountingVoucher(purchaseOrder, document);
            }
        }
    }

    private void createAccountingVoucher(PurchaseOrder purchaseOrder, PurchaseDocument document)
            throws CompanyConfigurationNotFoundException {
        if (!document.getHasVoucher()) {
            CompanyConfiguration companyConfiguration = companyConfigurationService.findCompanyConfiguration();

            if (ValidatorUtil.isBlankOrNull(companyConfiguration.getNationalCurrencyVATFiscalCreditAccountCode()) ||
                    ValidatorUtil.isBlankOrNull(companyConfiguration.getNationalCurrencyVATFiscalCreditTransientAccountCode())) {
                throw new CompanyConfigurationNotFoundException("The system configuration for current company haven't been configured");
            }

            String gloss = glossGeneratorService.generatePurchaseDocumentGloss(purchaseOrder, document, false);
            String form = PurchaseOrderType.WAREHOUSE.equals(purchaseOrder.getOrderType()) ? Constants.WAREHOUSE_VOUCHER_FORM : Constants.FIXEDASSET_VOUCHER_FORM;
            BigDecimal amount = BigDecimalUtil.multiply(document.getAmount(), Constants.VAT);

            Voucher voucher = VoucherBuilder.newGeneralVoucher(form, gloss);
            voucher.setUserNumber(companyConfiguration.getDefaultAccountancyUser().getId());
            voucher.setDate(document.getDate());
            voucher.setTransactionNumber(document.getTransactionNumber());

            if (document.isAdjustmentDocument()) {
                BigDecimal exchangeRateValue = document.getExchangeRate() != null ? document.getExchangeRate() : BigDecimal.ONE;

                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        purchaseOrder.getExecutorUnit().getExecutorUnitCode(),
                        purchaseOrder.getCostCenterCode(),
                        document.getCashAccountAdjustment(),
                        amount,
                        document.getCashAccountAdjustment().getCurrency(),
                        exchangeRateValue));
            } else {
                voucher.addVoucherDetail(VoucherDetailBuilder.newDebitVoucherDetail(
                        purchaseOrder.getExecutorUnit().getExecutorUnitCode(),
                        purchaseOrder.getCostCenterCode(),
                        companyConfiguration.getNationalCurrencyVATFiscalCreditAccount(),
                        amount,
                        FinancesCurrencyType.P,
                        BigDecimal.ONE));
            }
            voucher.addVoucherDetail(VoucherDetailBuilder.newCreditVoucherDetail(
                    purchaseOrder.getExecutorUnit().getExecutorUnitCode(),
                    purchaseOrder.getCostCenterCode(),
                    companyConfiguration.getNationalCurrencyVATFiscalCreditTransientAccount(),
                    amount,
                    FinancesCurrencyType.P,
                    BigDecimal.ONE));

            voucherService.create(voucher);

            document.setHasVoucher(true);
            getEntityManager().merge(document);
        }
    }

    public void createAccountingVoucherByPurchaseOrder(PurchaseOrder purchaseOrder) throws CompanyConfigurationNotFoundException {
        if (PurchaseOrderState.LIQ.equals(purchaseOrder.getState())) {
            List<PurchaseDocument> purchaseDocumentList = getEntityManager().createNamedQuery("PurchaseDocument.findByStateAndVoucherState")
                    .setParameter("purchaseOrderId", purchaseOrder.getId())
                    .setParameter("state", PurchaseDocumentState.APPROVED)
                    .setParameter("hasVoucher", Boolean.FALSE)
                    .getResultList();
            if (!ValidatorUtil.isEmptyOrNull(purchaseDocumentList)) {
                for (PurchaseDocument purchaseDocument : purchaseDocumentList) {
                    createAccountingVoucher(purchaseOrder, purchaseDocument);
                }
            }
        }
    }

    public List<PurchaseDocument> findByOrderVoucher(PurchaseOrder purchaseOrder) throws CompanyConfigurationNotFoundException {

        List<PurchaseDocument> purchaseDocumentList = new ArrayList<PurchaseDocument>();
        try {
            purchaseDocumentList = getEntityManager().createNamedQuery("PurchaseDocument.findByOrderVoucher")
                    .setParameter("purchaseOrderId", purchaseOrder.getId())
                    .getResultList();
        }catch(NoResultException e)
        {
            purchaseDocumentList = new ArrayList<PurchaseDocument>();
        }
       return purchaseDocumentList;
    }

    public void createFinanceAccountingDocument(DischargeDocument document) {
        FinanceAccountingDocument financeDocument = new FinanceAccountingDocument();
        financeDocument.setId(buildFinanceAccountingDocumentPk(document));
        financeDocument.setAmount(document.getAmount());
        financeDocument.setControlCode(document.getControlCode());
        financeDocument.setDate(document.getDate());
        financeDocument.setNit(document.getNit());
        financeDocument.setSocialName(document.getName());
        financeDocument.setAmount(document.getAmount());
        financeDocument.setIce(document.getIce());
        financeDocument.setExempt(document.getExempt());
        financeDocument.setTax(document.getIva());

        financeDocument.setTransactionNumber(document.getTransactionNumber());

        getEntityManager().persist(financeDocument);
    }

    public void createFinanceAccountingDocument(AccountingDocument document) {
        FinanceAccountingDocument financeAccountingDocument = new FinanceAccountingDocument();
        financeAccountingDocument.setId(buildFinanceAccountingDocumentPk((CollectionDocument) document));
        financeAccountingDocument.setAmount(document.getAmount());
        financeAccountingDocument.setControlCode(document.getControlCode());
        financeAccountingDocument.setDate(document.getDate());
        financeAccountingDocument.setExempt(document.getExempt());
        financeAccountingDocument.setIce(document.getIce());
        financeAccountingDocument.setNit(document.getNit());
        financeAccountingDocument.setSocialName(document.getName());
        financeAccountingDocument.setTax(document.getIva());
        financeAccountingDocument.setTransactionNumber(document.getTransactionNumber());
        getEntityManager().persist(financeAccountingDocument);
    }

    public FinanceAccountingDocumentPk buildFinanceAccountingDocumentPk(PurchaseDocument document) {
        FinanceAccountingDocumentPk id = new FinanceAccountingDocumentPk();
        id.setAuthorizationNumber(document.getAuthorizationNumber());
        id.setInvoiceNumber(document.getNumber());
        id.setCompanyNumber(Constants.defaultCompanyNumber);
        id.setEntityCode(document.getFinancesEntity().getId().toString());

        return id;
    }

    public FinanceAccountingDocumentPk buildFinanceAccountingDocumentPk(DischargeDocument document) {
        FinanceAccountingDocumentPk id = new FinanceAccountingDocumentPk();
        id.setAuthorizationNumber(document.getAuthorizationNumber());
        id.setInvoiceNumber(document.getNumber());
        id.setCompanyNumber(Constants.defaultCompanyNumber);
        id.setEntityCode(document.getFinancesEntity().getId().toString());

        return id;
    }

    public FinanceAccountingDocumentPk buildFinanceAccountingDocumentPk(CollectionDocument collectionDocument) {
        FinanceAccountingDocumentPk id = new FinanceAccountingDocumentPk();
        id.setAuthorizationNumber(collectionDocument.getAuthorizationNumber());
        id.setInvoiceNumber(collectionDocument.getNumber());
        id.setCompanyNumber(Constants.defaultCompanyNumber);
        id.setEntityCode(collectionDocument.getFinancesEntity().getId().toString());
        return id;
    }
}
