package com.encens.khipus.service.admin;

import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.CompanyModule;
import com.encens.khipus.model.admin.CompanyModulePk;
import com.encens.khipus.model.admin.SystemModule;

import javax.ejb.Local;
import java.util.List;

/**
 * SystemModule bussines interface
 *
 * @author
 * @version 1.0.18
 */
@Local
public interface SystemModuleService {

    List<SystemModule> getModules();

    List<SystemModule> getCompanyModules(Company company, Boolean active);

    CompanyModule getCompanyModule(CompanyModulePk companyModulePk);
}
