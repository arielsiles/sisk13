package com.encens.khipus.service.employees;

import com.encens.khipus.model.contacts.Salutation;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 09:05:12 PM
 */
@Local
public interface SalutationService {
    Salutation getDefaultSalutation(String gender);
}