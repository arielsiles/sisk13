package com.encens.khipus.service.customers;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.customers.DocumentType;

import javax.ejb.Local;
import java.util.List;

/**
 * Extension service interface
 *
 * @author
 * @version 2.7
 */

@Local
public interface ExtensionService extends GenericService {

    Extension findExtension(Long id) throws EntryNotFoundException;

    void updateDocumentType(Extension extension)
            throws ConcurrencyException, EntryNotFoundException;

    void deleteDocumentType(Extension extension)
            throws ReferentialIntegrityException, EntryNotFoundException;

    List<Extension> findExtensionsByDocumentType(DocumentType documentType);
}