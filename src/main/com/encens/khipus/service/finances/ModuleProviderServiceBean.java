package com.encens.khipus.service.finances;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.ModuleProvider;
import com.encens.khipus.model.finances.ModuleProviderType;
import com.encens.khipus.model.finances.Provider;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 1.0
 */
@Stateless
@Name("moduleProviderService")
@AutoCreate
public class ModuleProviderServiceBean extends GenericServiceBean implements ModuleProviderService {

    public List<ModuleProviderType> readModuleProviders(Provider provider) {
        List<ModuleProviderType> result = new ArrayList<ModuleProviderType>();
        List<ModuleProvider> moduleProviderList = getEntityManager().createNamedQuery("ModuleProvider.findByProvider")
                .setParameter("provider", provider)
                .getResultList();
        if (moduleProviderList != null && !moduleProviderList.isEmpty()) {
            for (ModuleProvider moduleProvider : moduleProviderList) {
                result.add(moduleProvider.getModuleProviderType());
            }
        }
        return result;
    }

    public void manageModuleProviders(Provider provider, List<ModuleProviderType> selectedModuleProviderTypes) throws EntryDuplicatedException, EntryNotFoundException {
        List<ModuleProviderType> storedmoduleProviderTypes = readModuleProviders(provider);
        List<ModuleProviderType> moduleProviderTypesToRemove = new ArrayList<ModuleProviderType>();
        List<ModuleProviderType> moduleProviderTypesToAdd = new ArrayList<ModuleProviderType>();
        if (selectedModuleProviderTypes != null && selectedModuleProviderTypes.isEmpty()) {
            if (storedmoduleProviderTypes != null && !storedmoduleProviderTypes.isEmpty()) {
                moduleProviderTypesToRemove.addAll(storedmoduleProviderTypes);
            }
        } else if (selectedModuleProviderTypes != null && !selectedModuleProviderTypes.isEmpty()) {
            if (storedmoduleProviderTypes != null && !storedmoduleProviderTypes.isEmpty()) {
                moduleProviderTypesToRemove = findModuleProviderTypes(selectedModuleProviderTypes, storedmoduleProviderTypes);
                moduleProviderTypesToAdd = findModuleProviderTypes(storedmoduleProviderTypes, selectedModuleProviderTypes);
            } else if (storedmoduleProviderTypes != null && storedmoduleProviderTypes.isEmpty()) {
                moduleProviderTypesToAdd.addAll(selectedModuleProviderTypes);
            }
        }

        removeModuleProviderTypesIfAny(provider, moduleProviderTypesToRemove);
        createModuleProviderTypesIfAny(provider, moduleProviderTypesToAdd);
    }

    private void createModuleProviderTypesIfAny(Provider provider, List<ModuleProviderType> moduleProviderTypesToAdd) throws EntryDuplicatedException {
        if (!moduleProviderTypesToAdd.isEmpty()) {
            for (ModuleProviderType moduleProviderType : moduleProviderTypesToAdd) {
                createModuleProvider(provider, moduleProviderType);
            }
        }
    }

    private void removeModuleProviderTypesIfAny(Provider provider, List<ModuleProviderType> moduleProviderTypesToRemove) throws EntryNotFoundException {
        if (!moduleProviderTypesToRemove.isEmpty()) {
            for (ModuleProviderType moduleProviderType : moduleProviderTypesToRemove) {
                removeModuleProvider(provider, moduleProviderType);
            }
        }
    }

    private List<ModuleProviderType> findModuleProviderTypes(List<ModuleProviderType> baseModuleProviderTypeList, List<ModuleProviderType> targetModuleProviderTypes) {
        List<ModuleProviderType> matchedModuleProviderTypes = new ArrayList<ModuleProviderType>();
        for (ModuleProviderType moduleProviderType : targetModuleProviderTypes) {
            if (!baseModuleProviderTypeList.contains(moduleProviderType)) {
                matchedModuleProviderTypes.add(moduleProviderType);
            }
        }
        return matchedModuleProviderTypes;
    }

    private void createModuleProvider(Provider provider, ModuleProviderType moduleProviderType) throws EntryDuplicatedException {
        ModuleProvider moduleProvider = new ModuleProvider();
        provider = getEntityManager().find(Provider.class, provider.getId());
        moduleProvider.setProvider(provider);
        moduleProvider.setModuleProviderType(moduleProviderType);
        getEntityManager().merge(moduleProvider);
        getEntityManager().flush();
    }

    private void removeModuleProvider(Provider provider, ModuleProviderType moduleProviderType) throws EntryNotFoundException {
        ModuleProvider moduleProvider = findModuleProviderByProviderAndType(provider, moduleProviderType);
        getEntityManager().remove(moduleProvider);
        getEntityManager().flush();

    }

    private ModuleProvider findModuleProviderByProviderAndType(Provider provider, ModuleProviderType moduleProviderType) throws EntryNotFoundException {
        return (ModuleProvider) getEntityManager().createNamedQuery("ModuleProvider.findByProviderAndType")
                .setParameter("provider", provider)
                .setParameter("moduleProviderType", moduleProviderType)
                .getSingleResult();
    }
}
