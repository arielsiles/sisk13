package com.encens.khipus.service.admin;

import com.encens.khipus.model.admin.Role;

import javax.ejb.Local;
import java.util.List;

/**
 * Role manager
 *
 * @author
 * @version 1.0
 */
@Local
public interface RoleService {

    List<Role> getList(int firstRow, int maxResults, String sortProperty, boolean sortAsc, Role roleCriteria);

    Long getCount(String sortProperty, boolean sortAsc, Role roleCriteria);
}