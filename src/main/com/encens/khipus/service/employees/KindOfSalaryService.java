package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.KindOfSalary;

import javax.ejb.Local;
import java.util.List;

/**
 * User: Ariel
 * Date: 22-03-2010
 * Time: 09:05:12 PM
 */
@Local
public interface KindOfSalaryService {

    KindOfSalary getKindOfSalaryById(Long id);

    List<KindOfSalary> findBySector(Sector sector);
}