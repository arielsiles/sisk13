package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.PayableDocumentType;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author
 * @version 3.5
 */
@Local
public interface PayrollGenerationInvestmentRegistrationService extends GenericService {
    void createInvestmentRegistrations(PayrollGenerationCycle payrollGenerationCycle,
                                       Map<Long, BigDecimal> socialWelfareEntityGeneralValues,
                                       PayableDocumentType payableDocumentType) throws EntryDuplicatedException, EntryNotFoundException;
}
