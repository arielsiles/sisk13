package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.DischargeDocumentNotFoundException;
import com.encens.khipus.exception.employees.DischargeDocumentStateException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.DischargeDocument;
import com.encens.khipus.model.employees.DischargeDocumentState;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.finances.FinanceAccountingDocumentService;
import com.encens.khipus.service.finances.FinancesPkGeneratorService;
import com.encens.khipus.util.purchases.PurchaseDocumentUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("dischargeDocumentService")
@AutoCreate
public class DischargeDocumentServiceBean extends GenericServiceBean implements DischargeDocumentService {
    @In(value = "#{listEntityManager}")
    private EntityManager eventEm;

    @In
    private FinanceAccountingDocumentService financeAccountingDocumentService;

    @In
    private FinancesPkGeneratorService financesPkGeneratorService;


    @In
    private JobContractService jobContractService;

    public DischargeDocument read(Long id) throws DischargeDocumentNotFoundException {
        if (null == getDischargeDocumentFromDatabase(id)) {
            throw new DischargeDocumentNotFoundException();
        }

        try {
            return findById(DischargeDocument.class, id, true);
        } catch (EntryNotFoundException e) {
            // this exception never happen because at the first checks if the element exists or not
        }

        return null;
    }

    public void createDocument(DischargeDocument document) {
        document.setNetAmount(document.getAmount());
        document.setType(CollectionDocumentType.INVOICE);
        setDefaultValues(document);
        getEntityManager().persist(document);
        getEntityManager().flush();
    }

    @TransactionAttribute(REQUIRES_NEW)
    public void updateDocument(DischargeDocument document)
            throws DischargeDocumentNotFoundException,
            DischargeDocumentStateException,
            ConcurrencyException {
        if (null == getDischargeDocumentFromDatabase(document.getId())) {
            throw new DischargeDocumentNotFoundException();
        }

        validateDischargeDocumentState(document);

        try {
            setDefaultValues(document);
            super.update(document);
        } catch (EntryDuplicatedException e) {
            //this exception never happen because the DischargeDocument entity not contains unique constraints
        }
    }

    public void approveDocument(DischargeDocument document)
            throws DuplicatedFinanceAccountingDocumentException,
            DischargeDocumentStateException,
            DischargeDocumentNotFoundException {
        if (null == getDischargeDocumentFromDatabase(document.getId())) {
            throw new DischargeDocumentNotFoundException();
        }

        validateDischargeDocumentState(document);

        financeAccountingDocumentService.validatePK(document);

        document.setTransactionNumber(financesPkGeneratorService.getNextPK());
        document.setState(DischargeDocumentState.APPROVED);
        setDefaultValues(document);
        getEntityManager().merge(document);

        financeAccountingDocumentService.createFinanceAccountingDocument(document);

        getEntityManager().flush();
    }

    public void nullifyDocument(DischargeDocument document)
            throws DischargeDocumentNotFoundException,
            DischargeDocumentStateException {
        if (null == getDischargeDocumentFromDatabase(document.getId())) {
            throw new DischargeDocumentNotFoundException();
        }

        validateDischargeDocumentState(document);

        document.setState(DischargeDocumentState.NULLIFIED);
        getEntityManager().merge(document);

        getEntityManager().flush();
    }


    @SuppressWarnings(value = "unchecked")
    public boolean isRegisteredDocumentForEmployee(Employee employee, GestionPayroll gestionPayroll) {


        List<JobContract> jobContracts = jobContractService.getJobContractList(employee);
        for (JobContract contract : jobContracts) {
            List<DischargeDocument> documents = getEntityManager().createNamedQuery("DischargeDocument.findByJobContractAndPayroll")
                    .setParameter("jobContract", contract)
                    .setParameter("gestionPayroll", gestionPayroll)
                    .getResultList();
            if (null != documents && !documents.isEmpty()) {

                return true;
            }
        }

        return false;
    }

    @SuppressWarnings(value = "unchecked")
    public List<DischargeDocument> getDocumentsByEmployee(Employee employee, GestionPayroll gestionPayroll) {
        return getEntityManager().createNamedQuery("DischargeDocument.findByEmployee")
                .setParameter("employee", employee)
                .setParameter("gestionPayroll", gestionPayroll).getResultList();
    }

    private void validateDischargeDocumentState(DischargeDocument document) throws DischargeDocumentStateException {
        DischargeDocument databaseDocument = getDischargeDocumentFromDatabase(document.getId());
        if (null != databaseDocument) {
            if (databaseDocument.isApproved()) {
                throw new DischargeDocumentStateException(DischargeDocumentState.APPROVED);
            }

            if (databaseDocument.isNullified()) {
                throw new DischargeDocumentStateException(DischargeDocumentState.NULLIFIED);
            }
        }
    }

    private DischargeDocument getDischargeDocumentFromDatabase(Long id) {
        return eventEm.find(DischargeDocument.class, id);
    }

    private void setDefaultValues(DischargeDocument document) {
        document.setIva(PurchaseDocumentUtil.i.calculateIVAAmount(document.getAmount()));
        document.setExempt(BigDecimal.ZERO);
        document.setIce(BigDecimal.ZERO);
    }
}
