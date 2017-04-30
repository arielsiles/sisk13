package com.encens.khipus.service.finances;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.exception.finances.CollectionSumExceedsRotatoryFundAmountException;
import com.encens.khipus.exception.finances.RotatoryFundApprovedException;
import com.encens.khipus.exception.finances.RotatoryFundLiquidatedException;
import com.encens.khipus.exception.finances.RotatoryFundNullifiedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.finances.CollectionDocument;
import com.encens.khipus.model.finances.RotatoryFundCollection;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.21
 */
@Stateless
@Name("collectionDocumentService")
@AutoCreate
public class CollectionDocumentServiceBean extends GenericServiceBean implements CollectionDocumentService {
    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @TransactionAttribute(REQUIRES_NEW)
    public void createCollectionDocument(CollectionDocument collectionDocument)
            throws RotatoryFundLiquidatedException, CollectionSumExceedsRotatoryFundAmountException, RotatoryFundNullifiedException {
        try {
            super.create(collectionDocument);
        } catch (EntryDuplicatedException e) {
            throw new RuntimeException("An Unexpected error has happened ", e);
        }
    }


    public void deleteRotatoryFund(CollectionDocument entity)
            throws RotatoryFundLiquidatedException,
            RotatoryFundApprovedException,
            ReferentialIntegrityException,
            EntryNotFoundException {

        findInDataBase(entity.getId());
        try {
            super.delete(entity);
        } catch (ConcurrencyException e) {
            throw new EntryNotFoundException(e);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public CollectionDocument getCollectionDocument(RotatoryFundCollection rotatoryFundCollection) {
        CollectionDocument collectionDocument;
        collectionDocument = (CollectionDocument) getEntityManager()
                .createNamedQuery("CollectionDocument.findByRotatoryFundCollection")
                .setParameter("rotatoryFundCollection", rotatoryFundCollection).getSingleResult();
        return collectionDocument;
    }

    private CollectionDocument findInDataBase(Long id) throws EntryNotFoundException {
        CollectionDocument collectionDocument = listEm.find(CollectionDocument.class, id);
        if (null == collectionDocument) {
            throw new EntryNotFoundException("Cannot find the CollectionDocument entity for id=" + id);
        }
        return collectionDocument;
    }
}