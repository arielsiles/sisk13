package com.encens.khipus.service.academics;

import com.encens.khipus.model.employees.TeachersForCareer;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 22-06-2010
 * Time: 06:50:32 PM
 */
@Local
public interface TeachersForCareerService {
    TeachersForCareer getCareer(Integer administrativeAcademicUnit, String studyPlan, Integer period, Integer gestion);
}