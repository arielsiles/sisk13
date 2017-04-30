package com.encens.khipus.service.finances;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.ModuleProviderType;
import com.encens.khipus.model.finances.Provider;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 1.0
 */
@Local
public interface ModuleProviderService extends GenericService {

    List<ModuleProviderType> readModuleProviders(Provider provider);

    void manageModuleProviders(Provider provider, List<ModuleProviderType> selectedModuleProviderTypes) throws EntryDuplicatedException, EntryNotFoundException;
}
