package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.Partner;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

/**
 * Credit service interface
 *
 * @author
 */

@Local
public interface CreditService {

    Credit findByEntity(Entity entity);
    Credit findCreditById(Long creditId);
    BigDecimal getActualCreditBalance(Credit credit);
    BigDecimal getTotalPaidCapital(Credit credit);
    List<Credit> getAllCredits();
    void changeCreditState(Credit credit, CreditState state);
    List<Credit> getCreditList(Partner partner);
}
