package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.*;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.24
 */
@Stateless
@Name("financeProviderService")
@AutoCreate
public class FinanceProviderServiceBean extends GenericServiceBean implements FinanceProviderService {

    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;


    public Provider findById(ProviderPk id) throws EntryNotFoundException {
        try {
            return (Provider) getEntityManager().createNamedQuery("Provider.findById")
                    .setParameter("providerId", id).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Boolean validateAcronym(FinancesEntity financesEntity) {
        Long count = (Long) (financesEntity.getId() == null ?
                eventEm.createNamedQuery("FinancesEntity.countByAcronym")
                        .setParameter("acronym", financesEntity.getAcronym()).getSingleResult() :
                eventEm.createNamedQuery("FinancesEntity.countByAcronymAndEntity")
                        .setParameter("financesEntityId", financesEntity.getId())
                        .setParameter("acronym", financesEntity.getAcronym()).getSingleResult());
        return count == null || count == 0;
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void createProvider(Provider provider, ModuleProviderType moduleProviderType) throws EntryDuplicatedException, ConcurrencyException {
        FinancesEntity financesEntity = provider.getEntity();
        super.create(financesEntity);
        provider.setEntity(financesEntity);
        provider.setId(new ProviderPk(Constants.defaultCompanyNumber, financesEntity.getId().toString()));
        super.create(provider);
        if (moduleProviderType != null) {
            getEntityManager().refresh(provider);
            provider.setModuleProviderList(new ArrayList<ModuleProvider>());
            ModuleProvider moduleProvider = new ModuleProvider(moduleProviderType);
            moduleProvider.setProvider(provider);
            provider.getModuleProviderList().add(moduleProvider);
            super.update(provider);
        }
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateProvider(Provider provider) throws ConcurrencyException, EntryDuplicatedException {
        super.update(provider.getEntity());
        getEntityManager().refresh(provider.getEntity());
        if (provider.getId() == null) {
            provider.setId(new ProviderPk(Constants.defaultCompanyNumber, provider.getEntity().getId().toString()));
            super.create(provider);
        } else {
            super.update(provider);
            getEntityManager().refresh(provider);
        }

    }
}
