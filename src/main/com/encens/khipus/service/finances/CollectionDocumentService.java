package com.encens.khipus.service.finances;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.finances.CollectionDocument;
import com.encens.khipus.model.finances.RotatoryFundCollection;

import javax.ejb.Local;

/**
 * @author
 * @version 2.23
 */
@Local
public interface CollectionDocumentService extends GenericService {
    @SuppressWarnings(value = "unchecked")
    CollectionDocument getCollectionDocument(RotatoryFundCollection rotatoryFundCollection);
}