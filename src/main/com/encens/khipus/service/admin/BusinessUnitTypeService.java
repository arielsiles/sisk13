package com.encens.khipus.service.admin;

import com.encens.khipus.model.admin.BusinessUnitType;

import javax.ejb.Local;

/**
 * @author
 *         BusinessUnitTypeService interface
 * @version 1.1.6
 */
@Local
public interface BusinessUnitTypeService {

    Long countMainBusinessUnitType();

    BusinessUnitType findBusinessUnitType(Long id);
}
