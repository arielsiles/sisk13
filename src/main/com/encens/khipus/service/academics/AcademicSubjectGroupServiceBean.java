package com.encens.khipus.service.academics;

import com.encens.khipus.model.academics.AcademicSubjectGroup;
import com.encens.khipus.model.academics.AcademicSubjectGroupPK;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;


/**
 * @author
 * @version 3.4
 */

@Stateless
@Name("academicSubjectGroupService")
@AutoCreate
public class AcademicSubjectGroupServiceBean implements AcademicSubjectGroupService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    public AcademicSubjectGroup findByIdFields(AcademicSubjectGroupPK academicSubjectGroupPK) {
        try {
//            systemNumber is a constant so is not been taken into account
            return (AcademicSubjectGroup) em.createNamedQuery("AcademicSubjectGroup.findById")
                    .setParameter("asignature", academicSubjectGroupPK.getAsignature())
                    .setParameter("curricula", academicSubjectGroupPK.getCurricula())
                    .setParameter("gestion", academicSubjectGroupPK.getGestion())
                    .setParameter("groupType", academicSubjectGroupPK.getGroupType())
                    .setParameter("subjectGroup", academicSubjectGroupPK.getSubjectGroup())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}