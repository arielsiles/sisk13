package com.encens.khipus.service.employees;

import com.encens.khipus.model.academics.AcademicCareerManagerPlanning;

import javax.ejb.Local;
import java.util.List;

/**
 * AcademicCareerManagerPlanningService
 *
 * @author
 * @version 2.24
 */
@Local
public interface AcademicCareerManagerPlanningService {
    List<AcademicCareerManagerPlanning> getPlanning(Integer employeeCode,
                                                           Integer gestion,
                                                           Integer period);
    List<AcademicCareerManagerPlanning> getAcademicCareerManagerPlanning(
            String careerId,
            Integer facultyId,
            Integer seatId,
            Integer gestion,
            Integer period);
}
