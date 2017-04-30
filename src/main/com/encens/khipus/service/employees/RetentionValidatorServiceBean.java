package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.DischargeDocument;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.finances.JobContract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("retentionValidatorService")
@AutoCreate
public class RetentionValidatorServiceBean extends GenericServiceBean implements RetentionValidatorService {
    @In
    private JobContractService jobContractService;


    @SuppressWarnings(value = "unchecked")
    public boolean applyRetention(Employee employee,
                                  GestionPayroll gestionPayroll,
                                  BigDecimal amount) {
        List<JobContract> jobContracts = jobContractService.getJobContractList(employee);
        for (JobContract contract : jobContracts) {
            List<DischargeDocument> documents = getEntityManager().createNamedQuery("DischargeDocument.findByJobContractAndPayroll")
                    .setParameter("jobContract", contract)
                    .setParameter("gestionPayroll", gestionPayroll)
                    .getResultList();
            if (null != documents && !documents.isEmpty() && verifyDocumentAmountValue(documents, amount)) {
                return false;
            }
        }

        return true;
    }

    private boolean verifyDocumentAmountValue(List<DischargeDocument> documents, BigDecimal amount) {
        for (DischargeDocument document : documents) {
            if (document.getAmount().compareTo(amount) == 0) {
                return true;
            }
        }

        return true;
    }
}
