package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.contacts.MaritalStatus;

import javax.ejb.Local;

/**
 * @author
 * @version 3.4
 */
@Local
public interface MaritalStatusService extends GenericService {

    MaritalStatus findByCode(String code);
}