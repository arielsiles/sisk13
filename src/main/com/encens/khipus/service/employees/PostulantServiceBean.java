package com.encens.khipus.service.employees;

import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.common.TextService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : PostulantServiceBean, 27-11-2009 01:50:08 AM
 */
@Stateless
@Name("postulantService")
@AutoCreate
public class PostulantServiceBean implements PostulantService {

    @In("#{entityManager}")
    private EntityManager em;
    @In
    private TextService textService;

    public void create(Postulant postulant, List<PostulantAcademicFormation> academicFormationList,
                       List<Experience> experienceList, List<HourAvailable> hourAvailableList,
                       List<Subject> subjectResultList, List<PostulantCharge> postulantChargeList) throws PersistenceException {

        postulant.setInternationalPrise(textService.handleText(postulant.getInternationalPrise()));
        postulant.setNationalPrise(textService.handleText(postulant.getNationalPrise()));
        postulant.setBooks(textService.handleText(postulant.getBooks()));
        postulant.setInternationalArticles(textService.handleText(postulant.getInternationalArticles()));
        postulant.setNationalArticles(textService.handleText(postulant.getNationalArticles()));

        em.persist(postulant);
        em.flush();

        postulant.setAcademicFormationList(academicFormationList);
        postulant.setExperienceList(experienceList);
        postulant.setHourAvailableList(hourAvailableList);
        postulant.setSubjectList(subjectResultList);
        postulant.setPostulantChargeList(postulantChargeList);

        for (PostulantAcademicFormation academicFormation : postulant.getAcademicFormationList()) {
            academicFormation.setPostulant(postulant);
        }
        for (Experience experience : postulant.getExperienceList()) {
            experience.setPostulant(postulant);
        }
        for (HourAvailable hourAvailable : postulant.getHourAvailableList()) {
            hourAvailable.setPostulant(postulant);
        }
        for (PostulantCharge postulantCharge : postulant.getPostulantChargeList()) {
            postulantCharge.setPostulant(postulant);
        }
        em.merge(postulant);
        em.flush();
    }
}
