package com.encens.khipus.service.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.model.customers.Customer;
import com.encens.khipus.model.customers.Invoice;
import com.encens.khipus.model.customers.InvoiceDetail;
import com.encens.khipus.model.finances.TaxRule;

/**
 * Invoice services interface
 *
 * @author
 * @version $Id: InvoiceService.java 2008-9-11 15:56:24 $
 */
public interface InvoiceService {

    void create(Invoice invoice, Customer customer) throws EntryDuplicatedException;

    void update(Invoice invoice);

    void save(Invoice invoice, Customer customer);

    void remove(Invoice invoice);

    Invoice findById(Long id);

    InvoiceDetail findDetailById(Long id);

    TaxRule findTaxRuleByUserId(Long id);
}
