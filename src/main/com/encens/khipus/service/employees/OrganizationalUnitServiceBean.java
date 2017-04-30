package com.encens.khipus.service.employees;

import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Sector;
import com.encens.khipus.model.finances.OrganizationalLevel;
import com.encens.khipus.model.finances.OrganizationalUnit;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : OrganizationalUnitServiceBean, 02-12-2009 01:27:36 PM
 */

@Stateless
@Name("organizationalUnitService")
@AutoCreate
public class OrganizationalUnitServiceBean implements OrganizationalUnitService {
    @In("#{entityManager}")
    private EntityManager em;


    public List<OrganizationalUnit> getOrganizationalUnitByLevel(Long levelId) {
        try {
            return em.createNamedQuery("OrganizationalUnit.findByLevel").setParameter("levelId", levelId).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<OrganizationalUnit>(0);
    }

    public List<OrganizationalUnit> getOrganizationalUnitByRoot(OrganizationalUnit organizationalUnit) {
        if (organizationalUnit != null && organizationalUnit.getId() != null) {
            try {
                return em.createNamedQuery("OrganizationalUnit.findByRoot").setParameter("organizationalUnitRoot", organizationalUnit).getResultList();
            } catch (Exception e) {
            }
        }
        return new ArrayList<OrganizationalUnit>(0);
    }

    public List<OrganizationalUnit> getOrganizationalUnitRootByBusinessUnit(BusinessUnit businessUnit) {
        if (businessUnit != null && businessUnit.getId() != null) {
            try {
                return em.createNamedQuery("OrganizationalUnit.getOrganizationalUnitRootByBusinessUnit").setParameter("businessUnit", businessUnit).getResultList();
            } catch (Exception e) {
            }
        }
        return new ArrayList<OrganizationalUnit>(0);
    }

    public List<OrganizationalUnit> getOrganizationalUnitRootByBusinessUnitAndSector(BusinessUnit businessUnit, Sector sector) {
        if (businessUnit != null && businessUnit.getId() != null && sector != null && sector.getId() != null) {
            try {
                return em.createNamedQuery("OrganizationalUnit.getOrganizationalUnitRootByBusinessUnitAndSector")
                        .setParameter("businessUnit", businessUnit)
                        .setParameter("sector", sector).getResultList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<OrganizationalUnit>(0);
    }

    public List<OrganizationalUnit> getOrganizationalUnitByByLevelAndRoot(Long levelId, OrganizationalUnit organizationalUnit) {
        if (organizationalUnit != null && organizationalUnit.getId() != null) {
            try {
                return em.createNamedQuery("OrganizationalUnit.findByLevelAndRoot").setParameter("levelId", levelId).setParameter("organizationalUnitRoot", organizationalUnit).getResultList();
            } catch (Exception e) {
            }
        }
        return new ArrayList<OrganizationalUnit>(0);
    }

    public Long countOrganizationalUnitByLevel(Long levelId) {
        if (levelId != null) {
            try {
                return new Long(String.valueOf(em.createNamedQuery("OrganizationalUnit.countByLevel").setParameter("levelId", levelId).getSingleResult()));
            } catch (Exception e) {
            }
        }
        return new Long(0);
    }

    public Long countOrganizationalUnitByRoot(OrganizationalUnit organizationalUnit) {
        if (organizationalUnit != null && organizationalUnit.getId() != null) {
            try {
                return new Long(String.valueOf(em.createNamedQuery("OrganizationalUnit.countByRoot").setParameter("organizationalUnitRoot", organizationalUnit).getSingleResult()));
            } catch (Exception e) {
            }
        }
        return new Long(0);
    }

    public Long countOrganizationalUnitByByLevelAndRoot(Long levelId, OrganizationalUnit organizationalUnit) {
        if (organizationalUnit != null && organizationalUnit.getId() != null) {
            try {
                return new Long(String.valueOf(em.createNamedQuery("OrganizationalUnit.countByLevelAndRoot").setParameter("levelId", levelId).setParameter("organizationalUnitRoot", organizationalUnit).getSingleResult()));
            } catch (Exception e) {
            }
        }
        return new Long(0);
    }

    public OrganizationalUnit getOrganizationalUnitByCareer(String career) {
        OrganizationalUnit result = null;
        try {
            result = (OrganizationalUnit) em.createNamedQuery("OrganizationalUnit.findByCareer").setParameter("career", career).getSingleResult();
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * Find by business unit and organizational level, this to find all career in business unit
     *
     * @param businessUnit
     * @param organizationalLevel
     * @return List
     */
    public List<OrganizationalUnit> getOrganizationalUnitByBusinessUnitLevelName(BusinessUnit businessUnit, OrganizationalLevel organizationalLevel) {
        if (businessUnit != null && organizationalLevel != null) {
            return em.createNamedQuery("OrganizationalUnit.findByBusinessUnitOrganizationLevel")
                    .setParameter("businessUnit", businessUnit)
                    .setParameter("organizationalLevel", organizationalLevel)
                    .getResultList();
        }
        return new ArrayList<OrganizationalUnit>();
    }

}
