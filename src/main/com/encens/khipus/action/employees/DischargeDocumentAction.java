package com.encens.khipus.action.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.employees.DischargeDocumentNotFoundException;
import com.encens.khipus.exception.employees.DischargeDocumentStateException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.DischargeDocument;
import com.encens.khipus.model.employees.DischargeDocumentState;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.finances.FinancesEntity;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.service.employees.DischargeDocumentService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Name("dischargeDocumentAction")
@Scope(ScopeType.CONVERSATION)
public class DischargeDocumentAction extends GenericAction<DischargeDocument> {

    private boolean createdAndNewOption;
    @In
    private DischargeDocumentService dischargeDocumentService;


    @Factory(value = "dischargeDocument", scope = ScopeType.STATELESS)
    public DischargeDocument initialize() {
        if (!isManaged()) {
            getInstance().setState(DischargeDocumentState.PENDING);
        }

        return getInstance();
    }

    @Override
    public DischargeDocument createInstance() {
        createdAndNewOption = false;
        return super.createInstance();
    }

    @Override
    @Begin(flushMode = FlushModeType.MANUAL)
    @Restrict(value = "#{s:hasPermission('DISCHARGEDOCUMENT','VIEW')}")
    public String select(DischargeDocument instance) {
        setOp(OP_UPDATE);
        try {
            setInstance(dischargeDocumentService.read(instance.getId()));

            return Outcome.SUCCESS;
        } catch (DischargeDocumentNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        }
    }

    @Override
    @End
    @Restrict(value = "#{s:hasPermission('DISCHARGEDOCUMENT','CREATE')}")
    public String create() {
        String validationOutcome = validateEmployee(getInstance().getJobContract(), getInstance().getGestionPayroll());
        if (!Outcome.SUCCESS.equals(validationOutcome)) {
            return Outcome.REDISPLAY;
        }

        dischargeDocumentService.createDocument(getInstance());

        return Outcome.SUCCESS;
    }

    @Override
    @Restrict(value = "#{s:hasPermission('DISCHARGEDOCUMENT','CREATE')}")
    public void createAndNew() {
        String validationOutcome = validateEmployee(getInstance().getJobContract(), getInstance().getGestionPayroll());
        if (Outcome.SUCCESS.equals(validationOutcome)) {
            dischargeDocumentService.createDocument(getInstance());

            addCreatedMessage();
            GestionPayroll lastGestionPayrollUsed = getInstance().getGestionPayroll();
            createInstance();
            setCreatedAndNewOption(true);
            getInstance().setGestionPayroll(lastGestionPayrollUsed);
        }
    }

    @Override
    @End
    @Restrict(value = "#{s:hasPermission('DISCHARGEDOCUMENT','UPDATE')}")
    public String update() {
        try {
            dischargeDocumentService.updateDocument(getInstance());

            addUpdatedMessage();

            return Outcome.SUCCESS;
        } catch (DischargeDocumentNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        } catch (DischargeDocumentStateException e) {
            addDischargeDocumentStateErrorMessage(e.getCurrentState());

            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            try {
                setInstance(dischargeDocumentService.read(getInstance().getId()));
            } catch (DischargeDocumentNotFoundException e1) {
                addNotFoundMessage();

                return Outcome.FAIL;
            }

            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
    }

    @End
    @Restrict(value = "#{s:hasPermission('DISCHARGEDOCUMENT','UPDATE')}")
    public String approve() {
        try {
            dischargeDocumentService.approveDocument(getInstance());

            return Outcome.SUCCESS;
        } catch (DuplicatedFinanceAccountingDocumentException e) {
            addDuplicatedFinanceAccountingDocumentErrorMessage(e);

            return Outcome.FAIL;
        } catch (DischargeDocumentStateException e) {
            addDischargeDocumentStateErrorMessage(e.getCurrentState());

            return Outcome.FAIL;
        } catch (DischargeDocumentNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        }
    }

    @End
    @Restrict(value = "#{s:hasPermission('DISCHARGEDOCUMENT','UPDATE')}")
    public String nullify() {
        try {
            dischargeDocumentService.nullifyDocument(getInstance());

            return Outcome.SUCCESS;
        } catch (DischargeDocumentNotFoundException e) {
            addNotFoundMessage();

            return Outcome.FAIL;
        } catch (DischargeDocumentStateException e) {
            addDischargeDocumentStateErrorMessage(e.getCurrentState());

            return Outcome.FAIL;
        }
    }

    public void assignFinancesEntity(FinancesEntity financesEntity) {
        getInstance().setFinancesEntity(financesEntity);
        if (financesEntity != null) {
            getInstance().setNit(financesEntity.getNitNumber());
        }
    }

    public void cleanFinancesEntity() {
        getInstance().setFinancesEntity(null);
    }

    public void assignGestionPayroll(GestionPayroll gestionPayroll) {
        getInstance().setGestionPayroll(gestionPayroll);
        cleanJobContract();
    }

    public void cleanGestionPayroll() {
        getInstance().setGestionPayroll(null);
        cleanJobContract();
    }

    public void assignJobContract(JobContract jobContract) {
        try {
            JobContract databaseJobContract = dischargeDocumentService.findById(JobContract.class, jobContract.getId());

            getInstance().setJobContract(databaseJobContract);
            getInstance().setName(databaseJobContract.getContract().getEmployee().getFullName());
        } catch (EntryNotFoundException e) {
            //
        }
    }

    private String validateEmployee(JobContract jobContract, GestionPayroll gestionPayroll) {
        String validationOutcome = Outcome.SUCCESS;
        List<DischargeDocument> documents = dischargeDocumentService
                .getDocumentsByEmployee(jobContract.getContract().getEmployee(), gestionPayroll);

        List<Long> availableDocuments = new ArrayList<Long>();

        for (DischargeDocument document : documents) {
            if (document.isNullified()) {
                continue;
            }

            if (document.isApproved()) {
                availableDocuments.add(document.getId());
            }

            if (document.isPending()) {
                availableDocuments.add(document.getId());
            }
        }

        if (isManaged()) {
            if (!availableDocuments.contains(getInstance().getId())) {
                validationOutcome = Outcome.FAIL;

                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "DischargeDocument.error.registeredEmployee",
                        getInstance().getJobContract().getContract().getEmployee().getFullName());
            }
        } else {
            if (!availableDocuments.isEmpty()) {
                validationOutcome = Outcome.FAIL;

                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "DischargeDocument.error.registeredEmployee",
                        getInstance().getJobContract().getContract().getEmployee().getFullName());
            }
        }

        return validationOutcome;
    }


    public void cleanJobContract() {
        getInstance().setJobContract(null);
    }

    @Override
    protected GenericService getService() {
        return dischargeDocumentService;
    }

    private void addDischargeDocumentStateErrorMessage(DischargeDocumentState state) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "DischargeDocument.error.stateIsUnEditable", MessageUtils.getMessage(state.getResourceKey()));
    }

    private void addDuplicatedFinanceAccountingDocumentErrorMessage(DuplicatedFinanceAccountingDocumentException e) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "FinanceAccountDocument.error.duplicatedFinanceAccountingDocument",
                e.getDuplicateId().getEntityCode(),
                e.getDuplicateId().getInvoiceNumber(),
                e.getDuplicateId().getAuthorizationNumber());
    }

    public boolean isCreatedAndNewOption() {
        return createdAndNewOption;
    }

    public void setCreatedAndNewOption(boolean createdAndNewOption) {
        this.createdAndNewOption = createdAndNewOption;
    }
}
