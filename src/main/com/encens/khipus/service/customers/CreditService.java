package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.Credit;

import javax.ejb.Local;
import java.math.BigDecimal;

/**
 * Credit service interface
 *
 * @author
 */

@Local
public interface CreditService {

    Credit findByEntity(Entity entity);

    BigDecimal getActualCreditBalance(Credit credit);
}
