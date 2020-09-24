package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Guarantor;

import javax.ejb.Local;

/**
 * @author
 * @version $Id: ClientService.java $
 */
@Local
public interface GuarantorService {

    void createCreditGuarantor(Guarantor guarantor);

}
