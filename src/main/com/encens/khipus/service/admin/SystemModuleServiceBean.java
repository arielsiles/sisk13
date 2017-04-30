package com.encens.khipus.service.admin;

import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.CompanyModule;
import com.encens.khipus.model.admin.CompanyModulePk;
import com.encens.khipus.model.admin.SystemModule;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * SysyemModuleServiceBean service bean
 *
 * @author
 * @version : SystemModuleServiceBean, 11-12-2009 10:20:31 AM
 */
@Stateless
@Name("systemModuleService")
@AutoCreate
public class SystemModuleServiceBean implements SystemModuleService {

    @In("#{entityManager}")
    private EntityManager em;

    public List<SystemModule> getModules() {
        try {
            return em.createNamedQuery("SystemModule.findAll").getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<SystemModule> getCompanyModules(Company company, Boolean active) {
        try {
            return em.createNamedQuery("SystemModule.findByCompanyAndActive")
                    .setParameter("company", company)
                    .setParameter("active", active).getResultList();
        } catch (Exception e) {
        }
        return new ArrayList<SystemModule>(0);
    }

    public CompanyModule getCompanyModule(CompanyModulePk companyModulePk) {
        try {
            return em.find(CompanyModule.class, companyModulePk);
        } catch (Exception e) {

        }
        return null;
    }
}
