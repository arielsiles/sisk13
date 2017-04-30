package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Dismissal;
import com.encens.khipus.model.employees.DismissalState;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import java.math.BigDecimal;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;


/**
 * @author
 * @version 3.4
 */

@Stateless
@Name("dismissalService")
@AutoCreate
public class DismissalServiceBean extends GenericServiceBean implements DismissalService {
    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @TransactionAttribute(REQUIRES_NEW)
    public void createDismissal(Dismissal dismissal) throws EntryDuplicatedException {
        dismissal.setCode(sequenceGeneratorService.nextValue(Constants.DISMISSAL_CODE_SEQUENCE));
        dismissal.setState(DismissalState.PENDING);
        dismissal.setAmount(BigDecimal.ZERO);
        dismissal.setCurrency(FinancesCurrencyType.P);
        super.create(dismissal);
    }

}