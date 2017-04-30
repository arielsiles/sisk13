package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.OrganizationalLevel;
import com.encens.khipus.model.finances.OrganizationalUnit;

import javax.ejb.Local;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : OrganizationalUnitService, 02-12-2009 01:28:31 PM
 */
@Local
public interface OrganizationalUnitService {
    List<OrganizationalUnit> getOrganizationalUnitByLevel(Long levelId);

    List<OrganizationalUnit> getOrganizationalUnitByRoot(OrganizationalUnit organizationalUnit);

    List<OrganizationalUnit> getOrganizationalUnitByByLevelAndRoot(Long levelId, OrganizationalUnit organizationalUnit);

    Long countOrganizationalUnitByLevel(Long levelId);

    Long countOrganizationalUnitByRoot(OrganizationalUnit organizationalUnit);

    Long countOrganizationalUnitByByLevelAndRoot(Long levelId, OrganizationalUnit organizationalUnit);

    List<OrganizationalUnit> getOrganizationalUnitRootByBusinessUnit(BusinessUnit businessUnit);

    List<OrganizationalUnit> getOrganizationalUnitRootByBusinessUnitAndSector(BusinessUnit businessUnit, Sector sector);

    OrganizationalUnit getOrganizationalUnitByCareer(String career);

    List<OrganizationalUnit> getOrganizationalUnitByBusinessUnitLevelName(BusinessUnit businessUnit, OrganizationalLevel organizationalLevel);
}
