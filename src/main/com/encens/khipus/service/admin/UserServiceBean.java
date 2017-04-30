package com.encens.khipus.service.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.*;
import com.encens.khipus.service.finances.FinancesUserService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * User manager.
 * All realated to user stuff goes here.
 *
 * @author
 * @version 1.0
 */
@Stateless
@Name("userService")
@AutoCreate
public class UserServiceBean extends GenericServiceBean implements UserService {

    @In
    private UserBusinessUnitService userBusinessUnitService;

    @In
    private FinancesUserService financesUserService;

    @In("#{entityManager}")
    private EntityManager em;

    @In(value = "listEntityManager")
    private EntityManager khipusListEm;

    public User findByUsernameAndPasswordAndCompany(String username, String password, String companyLogin) throws EntryNotFoundException {
        System.out.println("passwd: " + password);
        try {
            return (User) em.createNamedQuery("User.findByUsernameAndPasswordAndCompany")
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .setParameter("companyLogin", companyLogin).getSingleResult();
        } catch (NoResultException e) {
            throw new EntryNotFoundException();
        }
    }

    @SuppressWarnings({"unchecked"})
    public Map<String, Byte> getPermissions(User user) {
        try {
            Map<String, Byte> accessRights = new HashMap<String, Byte>();
            List<AccessRight> rights = em.createNamedQuery("AccessRight.findByUser")
                    .setParameter("user", user)
                    .setParameter("companyModuleActive", Boolean.TRUE).getResultList();

            for (AccessRight right : rights) {
                String code = right.getFunction().getCode();
                if (accessRights.containsKey(code)) {
                    Byte oldPermission = accessRights.get(code);
                    Byte newPermission = (new Integer(right.getPermission() | oldPermission)).byteValue();

                    accessRights.put(right.getFunction().getCode(), newPermission);
                    continue;
                }

                accessRights.put(code, right.getPermission());
            }

            return accessRights;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void create(Object entity) throws EntryDuplicatedException {
        super.create(entity);

        User user = (User) entity;
        if (null != user.getFinancesUser() && user.getFinancesUser()) {
            log.debug("Creating the new finance user with code=" + user.getFinancesCode());
            financesUserService.createFinanceUser(user);
        }
    }

    public void create(User user, List<BusinessUnit> currentBusinessUnits) throws EntryDuplicatedException {
        this.create(user);
        userBusinessUnitService.manageBusinessUnits(user, currentBusinessUnits);
    }


    @Override
    public void update(Object entity) throws ConcurrencyException, EntryDuplicatedException {
        User newUser = (User) entity;

        User oldUser = getKhipusUser(newUser.getId());
        Boolean oldFinancesUser = null;
        if (null != oldUser) {
            oldFinancesUser = oldUser.getFinancesUser();
        }

        super.update(entity);
        if ((null == oldFinancesUser || !oldFinancesUser) && null != newUser.getFinancesUser() && newUser.getFinancesUser()) {
            log.debug("Creating the new finance user with code=" + newUser.getFinancesCode());
            financesUserService.createFinanceUser(newUser);
        }
    }

    @Override
    public void delete(Object entity) throws ConcurrencyException, ReferentialIntegrityException {
        userBusinessUnitService.manageBusinessUnits((User) entity, new ArrayList<BusinessUnit>());
        super.delete(entity);
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void update(User user, List<BusinessUnit> currentBusinessUnits) throws ConcurrencyException, EntryDuplicatedException {
        this.update(user);
        userBusinessUnitService.manageBusinessUnits(user, currentBusinessUnits);
    }


    @SuppressWarnings(value = "unchecked")
    public boolean useFinancesAccessRights(User user, Company company) {
        List<Role> roles = user.getRoles();

        CompanyModulePk companyModulePk = new CompanyModulePk(company.getId(), Long.valueOf(5));
        CompanyModule financesModule = getCompanyModule(companyModulePk);
        if (null == financesModule) {
            log.debug("The company with id=" + user.getCompany().getId() + " is not assigned the finances module.");
            return false;
        }

        for (Role role : roles) {
            List<AccessRight> financesAccessRights = em.createNamedQuery("AccessRight.findByRoleAndCompanyModule")
                    .setParameter("role", role)
                    .setParameter("companyModule", financesModule).getResultList();

            if (null != financesAccessRights && !financesAccessRights.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private User getKhipusUser(Long userId) {
        User khipusUser = khipusListEm.find(User.class, userId);
        if (null == khipusUser) {
            log.debug("Cannot find User for id=" + userId);
        }

        return khipusUser;
    }

    private CompanyModule getCompanyModule(CompanyModulePk id) {
        CompanyModule companyModule = khipusListEm.find(CompanyModule.class, id);
        if (null == companyModule) {
            log.debug("Cannot find companyModule object for id=" + id);
        }

        return companyModule;
    }

    public User findByIdAndPassword(Long userId, String password) throws EntryNotFoundException {
        try {
            return (User) em.createNamedQuery("User.findByIdAndPassword")
                    .setParameter("id", userId)
                    .setParameter("password", password).getSingleResult();
        } catch (NoResultException e) {
            throw new EntryNotFoundException();
        }
    }
}
