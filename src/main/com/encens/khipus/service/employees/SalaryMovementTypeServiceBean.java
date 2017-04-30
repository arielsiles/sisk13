package com.encens.khipus.service.employees;

/**
 * @author
 * @version 3.4
 */

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.SalaryMovementTypeDuplicatedByDefaultException;
import com.encens.khipus.exception.employees.SalaryMovementTypeDuplicatedNameException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.MovementType;
import com.encens.khipus.model.employees.SalaryMovementType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

@Stateless
@Name("salaryMovementTypeService")
@AutoCreate
public class SalaryMovementTypeServiceBean extends GenericServiceBean implements SalaryMovementTypeService {

    public SalaryMovementType load(SalaryMovementType salaryMovementType) throws EntryNotFoundException {
        SalaryMovementType result = (SalaryMovementType) getEntityManager().createNamedQuery("SalaryMovementType.loadSalaryMovementType")
                .setParameter("id", salaryMovementType.getId()).getSingleResult();
        if (result == null) {
            throw new EntryNotFoundException();
        }
        return result;
    }

    public void createSalaryMovementType(SalaryMovementType salaryMovementType) throws EntryDuplicatedException, SalaryMovementTypeDuplicatedNameException, SalaryMovementTypeDuplicatedByDefaultException {
        validateName(salaryMovementType);
        validateByDefault(salaryMovementType);
        super.create(salaryMovementType);

    }

    public void updateSalaryMovementType(SalaryMovementType salaryMovementType) throws ConcurrencyException, EntryDuplicatedException, SalaryMovementTypeDuplicatedNameException, SalaryMovementTypeDuplicatedByDefaultException {
        validateName(salaryMovementType);
        validateByDefault(salaryMovementType);
        super.update(salaryMovementType);
    }

    private void validateName(SalaryMovementType salaryMovementType) throws SalaryMovementTypeDuplicatedNameException {
        Long id = salaryMovementType.getId() == null ? -1l : salaryMovementType.getId();
        Long countResult = (Long) getEventEntityManager().createNamedQuery("SalaryMovementType.countByName")
                .setParameter("id", id)
                .setParameter("name", salaryMovementType.getName())
                .getSingleResult();
        if (countResult != null && countResult > 0) {
            throw new SalaryMovementTypeDuplicatedNameException(salaryMovementType.getName());
        }
    }

    private void validateByDefault(SalaryMovementType salaryMovementType) throws SalaryMovementTypeDuplicatedByDefaultException {
        Long id = salaryMovementType.getId() == null ? -1l : salaryMovementType.getId();
        Long countResult = (Long) getEventEntityManager().createNamedQuery("SalaryMovementType.countByMovementTypeAndByDefault")
                .setParameter("id", id)
                .setParameter("movementType", salaryMovementType.getMovementType())
                .setParameter("byDefault", true)
                .getSingleResult();
        if (salaryMovementType.getId() == null && countResult != null && countResult == 0) {
            salaryMovementType.setByDefault(true);
        } else if (salaryMovementType.getByDefault() && countResult != null && countResult > 0) {
            throw new SalaryMovementTypeDuplicatedByDefaultException();
        }
    }

    public SalaryMovementType findDefaultByMovementType(MovementType movementType) {
        SalaryMovementType salaryMovementType = null;
        try {
            salaryMovementType = (SalaryMovementType) getEntityManager()
                    .createNamedQuery("SalaryMovementType.findDefaultByMovementType")
                    .setParameter("movementType", movementType)
                    .setParameter("byDefault", true).getSingleResult();
        } catch (NoResultException ignore) {
        }
        return salaryMovementType;
    }
}
