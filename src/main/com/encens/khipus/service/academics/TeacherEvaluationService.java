package com.encens.khipus.service.academics;

import com.encens.khipus.model.employees.TeacherEvaluation;

import javax.ejb.Local;

/**
 * User: Ariel
 * Date: 22-06-2010
 * Time: 06:50:32 PM
 */
@Local
public interface TeacherEvaluationService {
    TeacherEvaluation getTeacherEvaluation(Long employeeCode, Integer period, Integer gestion);
}
