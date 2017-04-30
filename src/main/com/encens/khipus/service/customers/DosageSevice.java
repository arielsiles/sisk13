package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.Dosage;

import javax.ejb.Local;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/12/14
 * Time: 2:40
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface DosageSevice extends GenericService {

    void find();
}
