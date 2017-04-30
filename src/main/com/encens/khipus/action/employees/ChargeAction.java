package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.interceptor.Assign;
import com.encens.khipus.interceptor.Assignments;
import com.encens.khipus.interceptor.PostCreateAssign;
import com.encens.khipus.model.employees.Charge;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.employees.ChargeService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Charge action class
 *
 * @author
 * @version 2.5
 */
@Name("chargeAction")
@Scope(ScopeType.CONVERSATION)
@PostCreateAssign
public class ChargeAction extends GenericAction<Charge> {

    @In
    private ChargeService chargeService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @Factory(value = "charge", scope = ScopeType.STATELESS)
    public Charge initCharge() {
        return getInstance();
    }

    @Override
    public String getDisplayNameMessage() {
        return FormatUtils.concatBySeparator(" - ", getInstance().getCode(), getInstance().getName());
    }

    @Override
    protected GenericService getService() {
        return chargeService;
    }

    @Override
    @Assignments({
            @Assign("#{jobContractAction.charge}")
    })
    public String create() {
        if (!chargeService.validateName(getInstance())) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
        getInstance().setCode(sequenceGeneratorService.nextValue(Constants.CHARGE_CODE_SEQUENCE));
        return super.create();
    }
}