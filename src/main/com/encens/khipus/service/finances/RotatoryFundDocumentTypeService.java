package com.encens.khipus.service.finances;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.RotatoryFundDocumentType;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.40
 */
@Local
public interface RotatoryFundDocumentTypeService extends GenericService {
    RotatoryFundDocumentType load(RotatoryFundDocumentType documentType) throws EntryNotFoundException;

    @TransactionAttribute(REQUIRES_NEW)
    void createDocumentType(RotatoryFundDocumentType rotatoryFundDocumentType) throws EntryDuplicatedException;
}
