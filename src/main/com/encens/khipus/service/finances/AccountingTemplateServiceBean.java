package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.AccountingTemplate;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * @author
 * @version 2.28
 */
@Name("accountingTemplateService")
@AutoCreate
@Stateless
public class AccountingTemplateServiceBean extends GenericServiceBean implements AccountingTemplateService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    public AccountingTemplate readFromDataBase(AccountingTemplate accountingTemplate) {
        return listEm.find(AccountingTemplate.class, accountingTemplate.getId());
    }

}
