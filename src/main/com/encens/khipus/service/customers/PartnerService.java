package com.encens.khipus.service.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.customers.Partner;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version $Id: PartnerService.java $
 */
@Local
public interface PartnerService {

    @TransactionAttribute(REQUIRES_NEW)
    void createPartner(Partner partner) throws EntryDuplicatedException;

}
