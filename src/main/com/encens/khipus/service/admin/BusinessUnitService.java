package com.encens.khipus.service.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;

import javax.ejb.Local;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * BusinessUnit service interface
 *
 * @author
 * @version 2.26
 */

@Local
public interface BusinessUnitService extends GenericService {

    BusinessUnit findById(Long id);

    BusinessUnit findByUser(User user);

    BusinessUnit findBusinessUnitByExecutorUnitCode(String executorUnitCode);

    void delete(BusinessUnit businessUnit) throws EntryNotFoundException, ConcurrencyException, ReferentialIntegrityException;

    @SuppressWarnings(value = "unchecked")
    List<BusinessUnit> findAll(EntityManager entityManager);
}
