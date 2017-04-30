package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.employees.SocialWelfareEntityDuplicatedIdNumberException;
import com.encens.khipus.exception.employees.SocialWelfareEntityDuplicatedNameException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.SocialWelfareEntity;

import javax.ejb.Local;

/**
 * @author
 * @version 3.5
 */
@Local
public interface SocialWelfareEntityService extends GenericService {

    void createEntity(SocialWelfareEntity entity) throws EntryDuplicatedException, SocialWelfareEntityDuplicatedIdNumberException, SocialWelfareEntityDuplicatedNameException;

    void updateEntity(SocialWelfareEntity entity) throws ConcurrencyException, EntryDuplicatedException, SocialWelfareEntityDuplicatedIdNumberException, SocialWelfareEntityDuplicatedNameException;
}
