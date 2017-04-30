package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.CycleType;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 24-06-2010
 * Time: 01:01:58 PM
 */
@Local
public interface CycleTypeService {
    CycleType getCycleType(Integer period);
}
