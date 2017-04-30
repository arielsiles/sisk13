package com.encens.khipus.service.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

/**
 * User manager
 *
 * @author
 * @version 1.0
 */
@Local
public interface UserService extends GenericService {

    User findByUsernameAndPasswordAndCompany(String username, String password, String companyLogin) throws EntryNotFoundException;

    Map<String, Byte> getPermissions(User user);

    boolean useFinancesAccessRights(User user, Company company);

    User findByIdAndPassword(Long userId, String password) throws EntryNotFoundException;

    void create(User user, List<BusinessUnit> currentBusinessUnits) throws EntryDuplicatedException;

    void update(User user, List<BusinessUnit> currentBusinessUnits) throws ConcurrencyException, EntryDuplicatedException;
}
