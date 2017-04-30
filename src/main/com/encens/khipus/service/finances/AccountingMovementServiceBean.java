package com.encens.khipus.service.finances;

/**
 * @author
 * @version 3.5
 */

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.AccountingMovement;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

@Name("accountingMovementService")
@Stateless
@AutoCreate
public class AccountingMovementServiceBean extends GenericServiceBean implements AccountingMovementService {

    public AccountingMovement findByMaximumTransactionNumber(String maximumTransactionNumber) {
        AccountingMovement accountingMovement = null;
        try {
            accountingMovement = (AccountingMovement) getEntityManager().createNamedQuery("AccountingMovement.findByTransactionNumber")
                    .setParameter("maximumTransactionNumber", maximumTransactionNumber)
                    .setFirstResult(0)
                    .setMaxResults(1).getSingleResult();
        } catch (NoResultException ignored) {
        }
        return accountingMovement;
    }
}
