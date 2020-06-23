package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Person;

import javax.ejb.Local;

/**
 * @author
 *
 */

@Local
public interface DistributorService {

    void addDistributor(Person person);

}
