package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.Credit;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.model.customers.CreditType;
import com.encens.khipus.model.customers.Partner;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
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
    BigDecimal calculateTotalPaidCapital(Credit credit, Date date);
    List<Credit> getAllCredits();
    List<Credit> getUnfinishedCredits();
    List<Credit> getCredits(CreditState creditState, CreditType creditType);
    void changeCreditState(Credit credit, CreditState state);
    List<Credit> getCreditList(Partner partner);
    void updateCredit(Credit credit);
    Object[] getAmountNewCredits(Long productiveZoneId, Date startDate, Date endDate);
}
