package com.encens.khipus.service.customers;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.customers.DocumentType;
import com.encens.khipus.service.employees.DocumentTypeService;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Extension service implementation class
 *
 * @author
 * @version 2.7
 */
@Name("extensionService")
@Stateless
@AutoCreate
public class ExtensionServiceBean extends GenericServiceBean implements ExtensionService {
    @Logger
    private Log log;

    @In(value = "#{entityManager}")
    private EntityManager entityManager;

    @In(value = "#{listEntityManager}")
    private EntityManager listEm;

    @In
    DocumentTypeService documentTypeService;

    public void updateDocumentType(Extension extension)
            throws ConcurrencyException, EntryNotFoundException {
        getEntityManager().merge(extension);
        getEntityManager().flush();
    }

    public void deleteDocumentType(Extension extension)
            throws ReferentialIntegrityException, EntryNotFoundException {

        findInDataBase(extension.getId());
        DocumentType documentType = extension.getDocumentType();
        try {
            super.delete(extension);
        } catch (ConcurrencyException e) {
            throw new EntryNotFoundException(e);
        }
    }

    public Extension findExtension(Long id) throws EntryNotFoundException {
        findInDataBase(id);
        Extension extension = getEntityManager().find(Extension.class, id);
        getEntityManager().refresh(extension);
        return extension;
    }

    private Extension findInDataBase(Long id) throws EntryNotFoundException {
        Extension extension = listEm.find(Extension.class, id);
        if (null == extension) {
            throw new EntryNotFoundException("Cannot find the Extension entity for id=" + id);
        }
        return extension;
    }

    @SuppressWarnings(value = "unchecked")
    public List<Extension> findExtensionsByDocumentType(DocumentType documentType) {
        try {
            Query query = listEm.createNamedQuery("Extension.findExtensionsByDocumentType");
            query.setParameter("documentType", documentType);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }
}