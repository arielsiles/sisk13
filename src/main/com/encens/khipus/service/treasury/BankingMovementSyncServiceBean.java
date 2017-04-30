package com.encens.khipus.service.treasury;

import com.encens.khipus.model.finances.FinancesDocumentType;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.treasury.BankingMovementSync;
import com.encens.khipus.service.finances.VoucherService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.VoucherBuilder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.Map;

/**
 * BankingMovementSyncServiceBean
 *
 * @author
 * @version 2.10
 */
@Name("bankingMovementSyncService")
@Stateless
@AutoCreate
@TransactionManagement(TransactionManagementType.BEAN)
public class BankingMovementSyncServiceBean implements BankingMovementSyncService {
    @Resource
    private UserTransaction userTransaction;
    @PersistenceContext(unitName = "khipus")
    private EntityManager em;
    @In
    private VoucherService voucherService;
    @Logger
    protected Log log;

    public Boolean registerBankingMovementSync(BankingMovementSync bankingMovementSync, Map<Integer, Map<Integer, String>> mapDataContainer, Map<Integer, FinancesDocumentType> documentTypeMapping) {
        Boolean registrationResult = false;
        try {
            userTransaction.setTransactionTimeout(mapDataContainer.size() * 60);
            userTransaction.begin();
            for (Map<Integer, String> mapData : mapDataContainer.values()) {
                //sf_tmpenc (no_cia, no_trans, formulario, tipo_doc, no_doc, fecha, monto, estado, moneda, cta_bco, procedencia, no_usr)
                if (!ValidatorUtil.isEmptyOrNull(mapData)) {
                    FinancesDocumentType type = documentTypeMapping.get(mapData.get(bankingMovementSync.getDocumentTypePosition()));
                    Voucher voucher = VoucherBuilder.newGeneralVoucher("TESO_BCO", mapData.get(bankingMovementSync.getGlossPosition()));
                    voucher.setDocumentType(type.getDocumentType());
                    voucher.setDocumentNumber(mapData.get(bankingMovementSync.getDocumentNumberPosition()));
                    voucher.setDate(DateUtils.parse(mapData.get(bankingMovementSync.getDatePosition()), bankingMovementSync.getDatePattern()));
                    BigDecimal amount = BigDecimalUtil.toSimpleBigDecimal(mapData.get(bankingMovementSync.getAmountPosition())).abs();
                    voucher.setAmount(BigDecimalUtil.multiply(amount, BigDecimalUtil.toBigDecimal(Math.pow(10, bankingMovementSync.getMagnitude()))));
                    voucher.setCurrency(bankingMovementSync.getBankAccount().getCurrency());
                    voucher.setBankAccountCode(bankingMovementSync.getBankAccount().getAccountNumber());
                    voucher.setSource("B");
                    voucherService.createBody(voucher);
                }
            }
            userTransaction.commit();
            userTransaction.setTransactionTimeout(0);
            registrationResult = true;
        } catch (Exception e) {
            log.error(e, "Unexpected exception, rolling back");
            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
            }

        }
        return registrationResult;
    }
}
