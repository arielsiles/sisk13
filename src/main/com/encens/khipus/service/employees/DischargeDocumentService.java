package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.employees.DischargeDocumentNotFoundException;
import com.encens.khipus.exception.employees.DischargeDocumentStateException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.DischargeDocument;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Local
public interface DischargeDocumentService extends GenericService {

    DischargeDocument read(Long id) throws DischargeDocumentNotFoundException;

    void createDocument(DischargeDocument document);

    void updateDocument(DischargeDocument document)
            throws DischargeDocumentNotFoundException,
            DischargeDocumentStateException,
            ConcurrencyException;

    void approveDocument(DischargeDocument document)
            throws DuplicatedFinanceAccountingDocumentException,
            DischargeDocumentStateException,
            DischargeDocumentNotFoundException;

    void nullifyDocument(DischargeDocument document)
            throws DischargeDocumentNotFoundException,
            DischargeDocumentStateException;

    boolean isRegisteredDocumentForEmployee(Employee employee, GestionPayroll gestionPayroll);

    List<DischargeDocument> getDocumentsByEmployee(Employee employee, GestionPayroll gestionPayroll);
}
