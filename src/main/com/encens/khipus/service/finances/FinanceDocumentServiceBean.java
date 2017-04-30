package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.AccountingMovement;
import com.encens.khipus.model.finances.AccountingMovementDetail;
import com.encens.khipus.model.finances.FinanceDocument;
import com.encens.khipus.model.finances.FinanceMovementType;
import com.encens.khipus.util.BigDecimalUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 3.0
 */
@Name("financeDocumentService")
@AutoCreate
@Stateless
public class FinanceDocumentServiceBean extends GenericServiceBean implements FinanceDocumentService {

    public BigDecimal sumDetail(String transactionNumber, AccountingMovement accountingMovement, FinanceMovementType type) {
        BigDecimal sumResult = (BigDecimal) getEntityManager().createNamedQuery("AccountingMovementDetail.sumDetail")
                .setParameter("transactionNumber", transactionNumber)
                .setParameter("accountingMovement", accountingMovement)
                .setParameter("type", type)
                .getSingleResult();
        return sumResult == null ? BigDecimal.ZERO : FinanceMovementType.C.equals(type) ? BigDecimalUtil.changeSign(sumResult) : sumResult;
    }

    public List<AccountingMovementDetail> findDetail(String transactionNumber, AccountingMovement accountingMovement, FinanceMovementType type) {
        return getEntityManager().createNamedQuery("AccountingMovementDetail.findDetail")
                .setParameter("transactionNumber", transactionNumber)
                .setParameter("accountingMovement", accountingMovement)
                .setParameter("type", type)
                .getResultList();
    }

    public FinanceDocument findByTransactionNumber(String transactionNumber) {
        FinanceDocument financeDocument = null;
        try {
            financeDocument = (FinanceDocument) getEntityManager().createNamedQuery("FinanceDocument.findByTransactionNumber")
                    .setParameter("transactionNumber", transactionNumber)
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException ignored) {
        }
        return financeDocument;
    }
}
