package com.encens.khipus.service.employees;

import com.encens.khipus.model.academics.AcademicCareerManagerPlanning;
import com.encens.khipus.model.academics.AcademicEmployeePlanning;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.24
 */
@Local
public interface AcademicEmployeePlanningService {
    List<AcademicEmployeePlanning> getPlanning(Integer employeeCode,
                                               Integer gestion,
                                               Integer period);

    List<AcademicEmployeePlanning> getPlanningByAcademicCareerManagerPlanning(List<AcademicCareerManagerPlanning> academicCareerManagerPlanningList);

    List<AcademicEmployeePlanning> getPlanningWithinSubject(Integer employeeCode,
                                                            Integer gestion,
                                                            Integer period);
}
