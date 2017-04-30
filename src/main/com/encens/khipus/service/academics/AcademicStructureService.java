package com.encens.khipus.service.academics;

import com.encens.khipus.model.academics.*;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.util.academic.AcademicStructure;

import javax.ejb.Local;
import java.util.List;

/**
 * AcademicStructureService
 *
 * @author
 * @version 2.24
 */
@Local
public interface AcademicStructureService {

    List<AcademicStructure> syncAcademicCareerStructure(List<AcademicCareerManagerPlanning> plannings);

    AcademicStructure syncAcademicEmployeeStructure(AcademicEmployeePlanning academicEmployeePlanning);

    AcademicStructure syncAcademicStudentStructure(AcademicStudentPlanning academicStudentPlanning);

    Subject synchronizeSubject(AcademicGeneralPlanning academicGeneralPlanning);

    Subject synchronizeSubject(String subjectId, String subjectName, String careerId, String careerName, Integer facultyId, String facultyName, Integer seatId, String seatName);

    Career synchronizeCareer(AcademicGeneralPlanning academicGeneralPlanning);

    Career synchronizeCareer(String careerId, String careerName, Integer facultyId, String facultyName, Integer seatId, String seatName);

    Faculty synchronizeFaculty(AcademicGeneralPlanning academicGeneralPlanning);

    Faculty synchronizeFaculty(Integer facultyId, String facultyName, Integer seatId, String seatName);

    Location synchronizeLocation(AcademicGeneralPlanning academicGeneralPlanning);

    Location synchronizeLocation(Integer seatId, String seatName);

    Employee synchronizeEmployee(AcademicGeneralPlanning academicGeneralPlanning);

    Student synchronizeStudent(AcademicStudentPlanning academicStudentPlanning);

    AcademicPeriod synchronizeAcademicPeriod(AcademicStudentPlanning academicStudentPlanning);

    AcademicPeriod synchronizeAcademicPeriod(String careerId, String subjectId, Integer gestion, Integer period);

}
