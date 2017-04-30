package com.encens.khipus.service.admin;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.22
 */

@Local
public interface UserBusinessUnitService extends GenericService {
    List<BusinessUnit> readBusinessUnits(User user);

    void manageBusinessUnits(User user,
                             List<BusinessUnit> currentBusinessUnits);
}
