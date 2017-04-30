package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.DismissalCause;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * @author
 * @version 3.4
 */
@Stateless
@Name("dismissalCauseService")
@AutoCreate
public class DismissalCauseServiceBean extends GenericServiceBean implements DismissalCauseService {
    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @TransactionAttribute(REQUIRES_NEW)
    public void createDismissalCause(DismissalCause dismissalCause) throws EntryDuplicatedException {
        dismissalCause.setCode(sequenceGeneratorService.nextValue(Constants.DISMISSALCAUSE_CODE_SEQUENCE));
        super.create(dismissalCause);
    }
}
