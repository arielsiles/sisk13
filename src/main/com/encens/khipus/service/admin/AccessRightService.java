package com.encens.khipus.service.admin;

import com.encens.khipus.model.admin.*;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

/**
 * AccessRight services interface
 *
 * @author:
 */

@Local
public interface AccessRightService {

    List<SystemFunction> getFunctions();

    List<SystemFunction> getFunctions(SystemModule module);

    AccessRight getAccessRight(SystemFunction function, Role role);

    void update(Object entity);

    List<SystemFunction> getAllFunctions(Company company);

    Map<SystemFunction, AccessRight> getAccessRightMapByFunction(Role role);
}