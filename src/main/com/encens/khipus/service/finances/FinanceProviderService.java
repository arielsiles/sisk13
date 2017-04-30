package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.FinancesEntity;
import com.encens.khipus.model.finances.ModuleProviderType;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.finances.ProviderPk;

import javax.ejb.Local;

/**
 * @author
 * @version 2.24
 */
@Local
public interface FinanceProviderService extends GenericService {
    void createProvider(Provider provider, ModuleProviderType moduleProviderType) throws EntryDuplicatedException, ConcurrencyException;

    void updateProvider(Provider provider) throws ConcurrencyException, EntryDuplicatedException;

    Boolean validateAcronym(FinancesEntity financesEntity);

    Provider findById(ProviderPk id) throws EntryNotFoundException;
}
