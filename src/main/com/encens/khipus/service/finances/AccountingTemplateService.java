package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.AccountingTemplate;

import javax.ejb.Local;

/**
 * @author
 * @version 2.28
 */
@Local
public interface AccountingTemplateService extends GenericService {
    AccountingTemplate readFromDataBase(AccountingTemplate accountingTemplate);
}
