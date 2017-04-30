package com.encens.khipus.service.employees;

import com.encens.khipus.model.academics.AcademicStudentPlanning;

import javax.ejb.Local;
import java.util.List;

/**
 * AcademicStudentPlanningService
 *
 * @author
 * @version 2.24
 */
@Local
public interface AcademicStudentPlanningService {
    List<AcademicStudentPlanning> getPlanning(Integer studentCode, Integer gestion, Integer period);
}
