package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.employees.SocialWelfareEntityDuplicatedIdNumberException;
import com.encens.khipus.exception.employees.SocialWelfareEntityDuplicatedNameException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.SocialWelfareEntity;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

/**
 * @author
 * @version 3.4
 */
@Name("socialWelfareEntityService")
@Stateless
@AutoCreate
public class SocialWelfareEntityServiceBean extends GenericServiceBean implements SocialWelfareEntityService {

    @SuppressWarnings({"UnusedAssignment", "UnnecessaryUnboxing"})
    public void validateDuplicatedIdNumber(SocialWelfareEntity entity) throws SocialWelfareEntityDuplicatedIdNumberException {
        Long idForValidation = entity.getId() != null ? entity.getId() : -1l;
        Long countByIdNumber = 0l;
        try {
            countByIdNumber = (Long) getEventEntityManager()
                    .createNamedQuery("SocialWelfareEntity.countByIdNumber")
                    .setParameter("entityId", idForValidation)
                    .setParameter("idNumber", entity.getIdNumber()).getSingleResult();
        } catch (NoResultException ignored) {

        }
        if (!(countByIdNumber == null || countByIdNumber.longValue() == 0)) {
            throw new SocialWelfareEntityDuplicatedIdNumberException();
        }
    }

    @SuppressWarnings({"UnusedAssignment", "UnnecessaryUnboxing"})
    public void validateDuplicatedName(SocialWelfareEntity entity) throws SocialWelfareEntityDuplicatedNameException {
        Long idForValidation = entity.getId() != null ? entity.getId() : -1l;
        Long countByName = 0l;
        try {
            countByName = (Long) getEventEntityManager()
                    .createNamedQuery("SocialWelfareEntity.countByName")
                    .setParameter("entityId", idForValidation)
                    .setParameter("name", entity.getName()).getSingleResult();
        } catch (NoResultException ignored) {

        }
        if (!(countByName == null || countByName.longValue() == 0)) {
            throw new SocialWelfareEntityDuplicatedNameException();
        }
    }

    public void createEntity(SocialWelfareEntity entity) throws EntryDuplicatedException, SocialWelfareEntityDuplicatedIdNumberException, SocialWelfareEntityDuplicatedNameException {
        validateDuplicatedIdNumber(entity);
        validateDuplicatedName(entity);
        super.create(entity);
    }

    public void updateEntity(SocialWelfareEntity entity) throws ConcurrencyException, EntryDuplicatedException, SocialWelfareEntityDuplicatedIdNumberException, SocialWelfareEntityDuplicatedNameException {
        validateDuplicatedIdNumber(entity);
        validateDuplicatedName(entity);
        super.update(entity);
    }
}