package com.encens.khipus.exception.finances;

import com.encens.khipus.model.finances.FinanceAccountingDocumentPk;

/**
 * @author
 * @version 2.25
 */
public class DuplicatedFinanceAccountingDocumentException extends Exception {
    private FinanceAccountingDocumentPk duplicateId;

    public DuplicatedFinanceAccountingDocumentException(FinanceAccountingDocumentPk duplicateId) {
        this.duplicateId = duplicateId;
    }

    public FinanceAccountingDocumentPk getDuplicateId() {
        return duplicateId;
    }
}
