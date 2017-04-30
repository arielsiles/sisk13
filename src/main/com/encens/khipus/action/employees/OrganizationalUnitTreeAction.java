package com.encens.khipus.action.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.service.employees.OrganizationalUnitService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * OrganizationalUnitTreeAction
 *
 * @author
 * @version 1.1.8
 */
@Name("organizationalUnitTreeAction")
@Scope(ScopeType.CONVERSATION)
public class OrganizationalUnitTreeAction implements Serializable {
    @In
    private OrganizationalUnitService organizationalUnitService;

    private BusinessUnit businessUnit;

    private OrganizationalUnit selectedOrganizationalUnit;

    private Boolean selectAnyElement = Boolean.TRUE;

    @In(value = "sectorQuery", create = true)
    private EntityQuery sectorQuery;

    public Boolean getSelectAnyElement() {
        return selectAnyElement;
    }

    public void setSelectAnyElement(Boolean selectAnyElement) {
        if (selectAnyElement == null) {
            selectAnyElement = Boolean.TRUE;
        }
        this.selectAnyElement = selectAnyElement;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public OrganizationalUnit getSelectedOrganizationalUnit() {
        return selectedOrganizationalUnit;
    }

    public void setSelectedOrganizationalUnit(OrganizationalUnit selectedOrganizationalUnit) {
        this.selectedOrganizationalUnit = selectedOrganizationalUnit;
    }

    public void clearSelectedOrganizationalUnit() {
        setSelectedOrganizationalUnit(null);
    }

    public List<OrganizationalUnit> getOrganizationalUnitsRoot(Sector sector) {
        return organizationalUnitService.getOrganizationalUnitRootByBusinessUnitAndSector(getBusinessUnit(), sector);
    }

    @SuppressWarnings("unchecked")
    public List<Sector> getSectorList() {
        if (getBusinessUnit() != null) {
            return sectorQuery.getResultList();
        }
        return new ArrayList<Sector>();
    }

    public List<OrganizationalUnit> getOrganizationalUnitsByRoot(OrganizationalUnit organizationalUnit) {
        return organizationalUnitService.getOrganizationalUnitByRoot(organizationalUnit);
    }

    public Boolean hasOrganizationalUnits(OrganizationalUnit organizationalUnit) {
        return organizationalUnitService.countOrganizationalUnitByRoot(organizationalUnit) > 0;
    }

    public void selectOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        if (getSelectAnyElement()) {
            setSelectedOrganizationalUnit(organizationalUnit);
        } else {
            if (!hasOrganizationalUnits(organizationalUnit)) {
                setSelectedOrganizationalUnit(organizationalUnit);
            } else {
                setSelectedOrganizationalUnit(null);
            }
        }
    }

    public String getSelectedOrganizationalunitFullName() {
        return getSelectedOrganizationalUnit() != null ? getSelectedOrganizationalUnit().getFullName() : "";
    }

}
