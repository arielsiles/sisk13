package com.encens.khipus.service.academics;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.academics.*;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.employees.EmployeeService;
import com.encens.khipus.service.employees.SalutationService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.academic.AcademicStructure;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * AcademicStructureServiceBean
 *
 * @author
 * @version 2.24
 */
@Name("academicStructureService")
@Stateless
@AutoCreate
public class AcademicStructureServiceBean implements AcademicStructureService {
    @In(value = "#{entityManager}")
    private EntityManager em;

    @Logger
    protected Log log;

    @In
    private EmployeeService employeeService;

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In
    private SalutationService salutationService;


    public List<AcademicStructure> syncAcademicCareerStructure(List<AcademicCareerManagerPlanning> plannings) {
        List<AcademicStructure> structures = new ArrayList<AcademicStructure>();

        for (AcademicCareerManagerPlanning planning : plannings) {
            AcademicStructure structure = new AcademicStructure();
            structure.setEmployee(synchronizeEmployee(planning));
            structure.setCareer(synchronizeCareer(planning));
            structure.setFaculty(structure.getCareer().getFaculty());

            structures.add(structure);
        }

        return structures;
    }

    public AcademicStructure syncAcademicEmployeeStructure(AcademicEmployeePlanning academicEmployeePlanning) {
        AcademicStructure structure = new AcademicStructure();

        structure.setEmployee(synchronizeEmployee(academicEmployeePlanning));
        structure.setSubject(synchronizeSubject(academicEmployeePlanning));
        structure.setCareer(synchronizeCareer(academicEmployeePlanning));
        structure.setFaculty(structure.getCareer().getFaculty());
        return structure;
    }

    public AcademicStructure syncAcademicStudentStructure(AcademicStudentPlanning academicStudentPlanning) {
        AcademicStructure academicStructure = new AcademicStructure();
        Subject subject = synchronizeSubject(academicStudentPlanning);
        em.merge(subject);
        academicStructure.setEmployee(synchronizeEmployee(academicStudentPlanning));
        academicStructure.setStudent(synchronizeStudent(academicStudentPlanning));
        academicStructure.setSubject(subject);
        academicStructure.setCareer(subject.getCareer());
        academicStructure.setFaculty(subject.getCareer().getFaculty());
        academicStructure.setLocation(subject.getCareer().getFaculty().getLocation());
        return academicStructure;
    }

    public Subject synchronizeSubject(AcademicGeneralPlanning academicGeneralPlanning) {
        if (academicGeneralPlanning instanceof AcademicStudentPlanning) {
            return synchronizeSubject(((AcademicStudentPlanning) academicGeneralPlanning).getSubjectId(), ((AcademicStudentPlanning) academicGeneralPlanning).getSubjectName(), academicGeneralPlanning.getCareerId(), academicGeneralPlanning.getCareerName(), academicGeneralPlanning.getFacultyId(), academicGeneralPlanning.getFacultyName(), academicGeneralPlanning.getSeatId(), academicGeneralPlanning.getSeatName());
        } else if (academicGeneralPlanning instanceof AcademicEmployeePlanning) {
            return synchronizeSubject(((AcademicEmployeePlanning) academicGeneralPlanning).getSubjectId(), ((AcademicEmployeePlanning) academicGeneralPlanning).getSubjectName(), academicGeneralPlanning.getCareerId(), academicGeneralPlanning.getCareerName(), academicGeneralPlanning.getFacultyId(), academicGeneralPlanning.getFacultyName(), academicGeneralPlanning.getSeatId(), academicGeneralPlanning.getSeatName());
        }
        return null;
    }

    public Subject synchronizeSubject(String subjectId, String subjectName, String careerId, String careerName, Integer facultyId, String facultyName, Integer seatId, String seatName) {

        if (ValidatorUtil.isBlankOrNull(subjectId) || ValidatorUtil.isBlankOrNull(subjectName)) {
            return null;
        }

        Subject subject = null;
        try {
            List<Subject> subjectList = em.createNamedQuery("Subject.findByReferenceIds")
                    .setParameter("subjectRefId", subjectId)
                    .setParameter("careerRefId", careerId)
                    .setParameter("facultyRefId", String.valueOf(facultyId))
                    .setParameter("locationRefId", String.valueOf(seatId)).getResultList();
            if (!ValidatorUtil.isEmptyOrNull(subjectList)) {
                subject = subjectList.get(0);
            }
        } catch (NoResultException noResultException) {

        }

        if (subject == null) {
            subject = new Subject();
            subject.setReferenceId(subjectId);
            subject.setName(subjectName);
            subject.setCareer(synchronizeCareer(careerId, careerName, facultyId, facultyName, seatId, seatName));
            em.persist(subject);
            em.flush();
        }

        return subject;
    }

    public Career synchronizeCareer(AcademicGeneralPlanning academicGeneralPlanning) {
        return synchronizeCareer(academicGeneralPlanning.getCareerId(), academicGeneralPlanning.getCareerName(), academicGeneralPlanning.getFacultyId(), academicGeneralPlanning.getFacultyName(), academicGeneralPlanning.getSeatId(), academicGeneralPlanning.getSeatName());
    }

    public Career synchronizeCareer(String careerId, String careerName, Integer facultyId, String facultyName, Integer seatId, String seatName) {

        if (ValidatorUtil.isBlankOrNull(careerId) || ValidatorUtil.isBlankOrNull(careerName)) {
            return null;
        }

        Career career = null;
        try {
            List<Career> careerList = em.createNamedQuery("Career.findByReferenceIds")
                    .setParameter("careerRefId", careerId)
                    .setParameter("facultyRefId", String.valueOf(facultyId))
                    .setParameter("locationRefId", String.valueOf(seatId)).getResultList();
            if (!ValidatorUtil.isEmptyOrNull(careerList)) {
                career = careerList.get(0);
            }

        } catch (NoResultException noResultException) {

        }

        if (career == null) {
            career = new Career();
            career.setReferenceId(careerId);
            career.setName(careerName);
            career.setFaculty(synchronizeFaculty(facultyId, facultyName, seatId, seatName));
            em.persist(career);
            em.flush();
        }

        return career;
    }

    public Faculty synchronizeFaculty(AcademicGeneralPlanning academicGeneralPlanning) {
        return synchronizeFaculty(academicGeneralPlanning.getFacultyId(), academicGeneralPlanning.getFacultyName(), academicGeneralPlanning.getSeatId(), academicGeneralPlanning.getSeatName());
    }

    public Faculty synchronizeFaculty(Integer facultyId, String facultyName, Integer seatId, String seatName) {

        if (facultyId == null || ValidatorUtil.isBlankOrNull(facultyName)) {
            return null;
        }

        Faculty faculty = null;
        try {
            List<Faculty> facultyList = em.createNamedQuery("Faculty.findByReferenceIds")
                    .setParameter("facultyRefId", String.valueOf(facultyId))
                    .setParameter("locationRefId", String.valueOf(seatId)).getResultList();
            if (!ValidatorUtil.isEmptyOrNull(facultyList)) {
                faculty = facultyList.get(0);
            }
        } catch (NoResultException noResultException) {

        }

        if (faculty == null) {
            faculty = new Faculty();
            faculty.setReferenceId(String.valueOf(facultyId));
            faculty.setName(facultyName);
            faculty.setLocation(synchronizeLocation(seatId, seatName));
            em.persist(faculty);
            em.flush();
        }

        return faculty;
    }

    public Location synchronizeLocation(AcademicGeneralPlanning academicGeneralPlanning) {
        return synchronizeLocation(academicGeneralPlanning.getSeatId(), academicGeneralPlanning.getSeatName());
    }

    public Location synchronizeLocation(Integer seatId, String seatName) {

        if (seatId == null || ValidatorUtil.isBlankOrNull(seatName)) {
            return null;
        }

        Location location = null;
        try {
            List<Location> locationList = em.createNamedQuery("Location.findByReferenceId")
                    .setParameter("locationRefId", String.valueOf(seatId)).getResultList();
            if (!ValidatorUtil.isEmptyOrNull(locationList)) {
                location = locationList.get(0);
            }
        } catch (NoResultException noResultException) {

        }

        if (location == null) {
            location = new Location();
            location.setReferenceId(String.valueOf(seatId));
            location.setName(seatName);
            em.persist(location);
            em.flush();
        }

        return location;
    }

    public Employee synchronizeEmployee(AcademicGeneralPlanning academicGeneralPlanning) {
        Employee employee = employeeService.getEmployeeByCode(String.valueOf(academicGeneralPlanning.getEmployeeCode()));
        if (employee == null) {
            try {
                employee = new Employee();
                employee.setIdNumber(FormatUtils.evaluateValue(academicGeneralPlanning.getEmployeeIdNumber()));
                employee.setDocumentType(companyConfigurationService.findDefaultDocumentType());
                employee.setControlFlag(true);
                employee.setAfpFlag(false);
                employee.setRetentionFlag(true);
                employee.setPaymentType(PaymentType.PAYMENT_BANK_ACCOUNT);
                employee.setEmployeeCode(String.valueOf(academicGeneralPlanning.getEmployeeCode()));
                employee.setMarkCode(FormatUtils.evaluateValue(academicGeneralPlanning.getEmployeeIdNumber()));
                employee.setLastName(academicGeneralPlanning.getEmployeeLastName());
                employee.setMaidenName(academicGeneralPlanning.getEmployeeMaidenName());
                employee.setFirstName(academicGeneralPlanning.getEmployeeFirstName());
                employee.setSalutation(salutationService.getDefaultSalutation(academicGeneralPlanning.getEmployeeGender()));
                em.persist(employee);
                em.flush();
            } catch (CompanyConfigurationNotFoundException e) {
            }
        }

        return employee;
    }


    public Student synchronizeStudent(AcademicStudentPlanning academicStudentPlanning) {
        Student student = null;

        String studentCode = String.valueOf(academicStudentPlanning.getStudentCode());
        try {
            List<Student> studentList = em.createNamedQuery("Student.findByCode")
                    .setParameter("studentCode", studentCode).getResultList();
            if (!ValidatorUtil.isEmptyOrNull(studentList)) {
                student = studentList.get(0);
            }
        } catch (NoResultException noResultException) {

        }

        if (student == null) {
            try {
                student = new Student();
                student.setStudentCode(studentCode);
                student.setIdNumber(FormatUtils.evaluateValue(academicStudentPlanning.getStudentIdNumber()));
                student.setDocumentType(companyConfigurationService.findDefaultDocumentType());
                student.setLastName(academicStudentPlanning.getStudentLastName());
                student.setMaidenName(academicStudentPlanning.getStudentMaidenName());
                student.setFirstName(academicStudentPlanning.getStudentFirstName());
                student.setSalutation(salutationService.getDefaultSalutation(academicStudentPlanning.getStudentGender()));
                em.persist(student);
                em.flush();
            } catch (CompanyConfigurationNotFoundException e) {

            }

        }
        return student;
    }

    public AcademicPeriod synchronizeAcademicPeriod(AcademicStudentPlanning academicStudentPlanning) {
        return synchronizeAcademicPeriod(academicStudentPlanning.getCareerId(), academicStudentPlanning.getSubjectId(), academicStudentPlanning.getGestion(), academicStudentPlanning.getPeriod());
    }

    public AcademicPeriod synchronizeAcademicPeriod(String careerId, String subjectId, Integer gestion, Integer period) {
        AcademicPeriod academicPeriod = null;
        if (!ValidatorUtil.isBlankOrNull(subjectId) && gestion != null && period != null) {
            AsignatureLevel asignatureLevel = null;
            try {
                List<AsignatureLevel> asignatureLevelList = (List<AsignatureLevel>) em.createNamedQuery("AsignatureLevel.findByGestionAndPerido")
                        .setParameter("career", careerId)
                        .setParameter("asignature", subjectId)
                        .setParameter("gestion", gestion)
                        .setParameter("period", period)
                        .setParameter("active", Boolean.TRUE).getResultList();
                if (!ValidatorUtil.isEmptyOrNull(asignatureLevelList)) {
                    asignatureLevel = asignatureLevelList.get(0);
                }
            } catch (NoResultException noResultException) {

            }

            if (asignatureLevel != null) {
                try {
                    academicPeriod = (AcademicPeriod) em.createNamedQuery("AcademicPeriod.findByReferenceId")
                            .setParameter("referenceId", asignatureLevel.getId()).getSingleResult();
                } catch (NoResultException noResultException) {

                }
                if (academicPeriod == null) {
                    academicPeriod = new AcademicPeriod();
                    academicPeriod.setName(asignatureLevel.getName());
                    academicPeriod.setSequence(asignatureLevel.getPriority());
                    academicPeriod.setReferenceId(asignatureLevel.getId());
                    em.persist(academicPeriod);
                    em.flush();
                }

            }
        }

        return academicPeriod;
    }

}
