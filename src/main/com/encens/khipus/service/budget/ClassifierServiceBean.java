package com.encens.khipus.service.budget;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.budget.Classifier;
import com.encens.khipus.model.budget.ClassifierAccount;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * ClassifierServiceBean
 *
 * @author
 * @version 2.1
 */
@Name("classifierService")
@Stateless
@AutoCreate
public class ClassifierServiceBean extends GenericServiceBean implements ClassifierService {

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void create(Object entity) throws EntryDuplicatedException {
        super.create(entity);
        ClassifierAccount classifierAccount = new ClassifierAccount((Classifier) entity, ((Classifier) entity).getAccountCode());
        getEntityManager().persist(classifierAccount);
        getEntityManager().flush();
    }
}
