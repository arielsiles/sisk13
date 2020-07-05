package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Dosage;

import javax.ejb.Local;

/**
 * Created with IntelliJ IDEA.
 */
@Local
public interface DosageService {

    Dosage findDosageByOffice(Long branchOfficeId);
    void increaseInvoiceNumber(Dosage dosage);

}
