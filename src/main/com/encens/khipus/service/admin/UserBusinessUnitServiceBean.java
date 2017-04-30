package com.encens.khipus.service.admin;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.admin.UserBusinessUnit;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.22
 */
@Stateless
@Name("userBusinessUnitService")
@AutoCreate
public class UserBusinessUnitServiceBean extends GenericServiceBean implements UserBusinessUnitService {

    @SuppressWarnings(value = "unchecked")
    public List<BusinessUnit> readBusinessUnits(User user) {
        List<BusinessUnit> result = new ArrayList<BusinessUnit>();

        List<UserBusinessUnit> assignedBusinessUnits =
                getEntityManager().createNamedQuery("UserBusinessUnit.findByUser")
                        .setParameter("user", user)
                        .getResultList();
        for (UserBusinessUnit userBusinessUnit : assignedBusinessUnits) {
            result.add(userBusinessUnit.getBusinessUnit());
        }

        return result;
    }

    @SuppressWarnings(value = "unchecked")
    public void manageBusinessUnits(User user,
                                    List<BusinessUnit> currentBusinessUnits) {
        List<UserBusinessUnit> assignedBusinessUnits =
                getEntityManager().createNamedQuery("UserBusinessUnit.findByUser")
                        .setParameter("user", user)
                        .getResultList();

        if (null != assignedBusinessUnits && !assignedBusinessUnits.isEmpty()) {
            List<UserBusinessUnit> businessUnitsToDelete = new ArrayList<UserBusinessUnit>();

            List<BusinessUnit> businessUnitsToKeep = new ArrayList<BusinessUnit>();

            for (UserBusinessUnit userBusinessUnit : assignedBusinessUnits) {
                if (!currentBusinessUnits.contains(userBusinessUnit.getBusinessUnit())) {
                    businessUnitsToDelete.add(userBusinessUnit);
                } else {
                    businessUnitsToKeep.add(userBusinessUnit.getBusinessUnit());
                }
            }

            for (BusinessUnit businessUnit : currentBusinessUnits) {
                if (!businessUnitsToKeep.contains(businessUnit)) {
                    createUserBusinessUnit(user, businessUnit);
                }
            }

            for (UserBusinessUnit userBusinessUnit : businessUnitsToDelete) {
                getEntityManager().remove(userBusinessUnit);
                getEntityManager().flush();
            }
        } else {
            for (BusinessUnit businessUnit : currentBusinessUnits) {
                createUserBusinessUnit(user, businessUnit);
            }
        }
    }

    private void createUserBusinessUnit(User user, BusinessUnit businessUnit) {
        UserBusinessUnit instance = new UserBusinessUnit();
        instance.setBusinessUnit(businessUnit);
        instance.setUser(user);

        getEntityManager().persist(instance);
        getEntityManager().flush();
    }
}
