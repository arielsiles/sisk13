package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Tolerance;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 08:51:15 PM
 */
@Local
public interface ToleranceService {
    Tolerance getTolerance(Long id);
}
