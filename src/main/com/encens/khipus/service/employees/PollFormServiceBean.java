package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version 1.1.7
 */
@Stateless
@Name("pollFormService")
@AutoCreate
public class PollFormServiceBean implements PollFormService {

    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager em;

    public Long calculateCareerTotal(Long personId, Long pollFormId, PollFormGrouppingType pollFormGrouppingType) {
        if (personId != null && pollFormId != null) {
            if (PollFormGrouppingType.CAREER.equals(pollFormGrouppingType)) {
                try {
                    return (Long) em.createNamedQuery("PollForm.countByCareerGrouppingByCareer").
                            setParameter("personId", personId).
                            setParameter("pollFormId", pollFormId).
                            getSingleResult();
                } catch (Exception e) {
                    log.debug(e.getMessage());
                }
            } else if (PollFormGrouppingType.SUBJECT.equals(pollFormGrouppingType)) {
                try {
                    return (Long) em.createNamedQuery("PollForm.countByCareerGrouppingBySubject").
                            setParameter("personId", personId).
                            setParameter("pollFormId", pollFormId).
                            getSingleResult();
                } catch (Exception e) {
                    log.debug(e.getMessage());
                }
            }
        }
        return null;
    }

    public Long calculateSubjectTotal(Long personId, Long pollFormId) {
        try {
            return (Long) em.createNamedQuery("PollForm.countBySubject").
                    setParameter("personId", personId).
                    setParameter("pollFormId", pollFormId).
                    getSingleResult();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return null;
    }

    public Long calculateCopyTotal(Long personId, Long pollFormId) {
        try {
            return (Long) em.createNamedQuery("PollForm.countByPollCopy").
                    setParameter("personId", personId).
                    setParameter("pollFormId", pollFormId).
                    getSingleResult();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return null;
    }

    public Long calculatePollCopyByFacultyCareer(Long personId, Long pollFormId, Long facultyId, Long careerId) {
        try {
            return (Long) em.createNamedQuery("PollForm.countPollCopyByPersonFacultyCareer").
                    setParameter("personId", personId).
                    setParameter("pollFormId", pollFormId).
                    setParameter("facultyId", facultyId).
                    setParameter("careerId", careerId).
                    getSingleResult();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return null;
    }

    public BigDecimal calculateEvaluationValue(Long personId, Long pollFormId, Long sectionId) {
        try {
            return new BigDecimal(String.valueOf(em.createNamedQuery("PollForm.avgBySection").
                    setParameter("personId", personId).
                    setParameter("pollFormId", pollFormId).
                    setParameter("sectionId", sectionId).
                    getSingleResult()));
        } catch (Exception e) {
            log.debug("Error in calculate evaluation value:" + e);
        }
        return null;
    }

    public Long countAssertQuestionEvaluationCriteriaValueByPerson(Long personId, Long pollFormId, Long questionId, Long evaluationCriteriaValueId, Long facultyId, Long careerId) {
        try {
            return (Long) em.createNamedQuery("PollForm.countAssertQuestionEvaluationCriteriaValueByPerson").
                    setParameter("personId", personId).
                    setParameter("pollFormId", pollFormId).
                    setParameter("questionId", questionId).
                    setParameter("evaluationCriteriaValueId", evaluationCriteriaValueId).
                    setParameter("facultyId", facultyId).
                    setParameter("careerId", careerId).
                    getSingleResult();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return null;
    }

    /**
     * Find poll forms by cycle
     *
     * @param cycle
     * @return List
     */
    public List<PollForm> getPollFormByCycle(Cycle cycle) {
        try {
            return em.createNamedQuery("PollForm.findByCycle")
                    .setParameter("cycle", cycle)
                    .getResultList();
        } catch (Exception e) {
            log.debug("Not found poll forms by cycle.." + e);
        }
        return new ArrayList<PollForm>();
    }


    public List<Location> getLocationList() {
        try {
            return em.createNamedQuery("Location.findAll").getResultList();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return new ArrayList<Location>();
    }

    public List<Faculty> getFacultyList(Location location) {
        if (location != null && location.getId() != null) {
            try {
                return em.createNamedQuery("Faculty.findByLocation").setParameter("location", location).getResultList();
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
        return new ArrayList<Faculty>();
    }

    public List<Career> getCareerList(Faculty faculty) {
        if (faculty != null && faculty.getId() != null) {
            try {
                return em.createNamedQuery("Career.findByFaculty").setParameter("faculty", faculty).getResultList();
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
        return new ArrayList<Career>();
    }

    public List<Subject> getSubjectList(Career career) {
        if (career != null && career.getId() != null) {
            try {
                return em.createNamedQuery("Subject.findByCareer").setParameter("career", career).getResultList();
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
        return new ArrayList<Subject>();
    }

    public String readQuestionContent(Long questionId) {
        return (String) em.createNamedQuery("Question.readContent").setParameter("questionId", questionId).getSingleResult();
    }

    public List<Question> getQuestionListByPollForm(PollForm pollForm) {
        return em.createNamedQuery("Question.findByPollForm").setParameter("pollForm", pollForm).getResultList();
    }
}
