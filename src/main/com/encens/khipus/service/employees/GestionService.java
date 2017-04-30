package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Gestion;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 24-06-2010
 * Time: 12:56:10 PM
 */
@Local
public interface GestionService {
    Gestion getGestion(Integer year);
    public Gestion getLastGestion();
}
