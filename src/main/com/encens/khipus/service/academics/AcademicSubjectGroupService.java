package com.encens.khipus.service.academics;

import com.encens.khipus.model.academics.AcademicSubjectGroup;
import com.encens.khipus.model.academics.AcademicSubjectGroupPK;

import javax.ejb.Local;

/**
 * @author
 * @version 3.2.8
 */
@Local
public interface AcademicSubjectGroupService {

    AcademicSubjectGroup findByIdFields(AcademicSubjectGroupPK academicSubjectGroupPK);
}