package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.*;
import com.encens.khipus.model.production.DiscountProducer;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.service.production.RawMaterialPayRollService;
import com.encens.khipus.service.production.RawMaterialPayRollServiceBean;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.Outcome;
import org.jboss.seam.international.StatusMessage;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 22-05-13
 * Time: 05:25 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("discountProducerAction")
@Scope(ScopeType.CONVERSATION)
public class DiscountProducerAction extends GenericAction<DiscountProducer> {
    private Boolean state = false;

    @In
    private RawMaterialPayRollService rawMaterialPayRollService;

    //TODO change the name initContinent
    @Factory(value = "discountProducer", scope = ScopeType.STATELESS)
    public DiscountProducer initContinent() {
        getInstance().setAmountME(0.0);
        getInstance().setAmountMN(0.0);
        getInstance().setAverage(0.0);
        getInstance().setReserveFortnight(0.0);
        getInstance().setTc(0.0);
        getInstance().setState("ENABLE");
        return getInstance();
    }

    @End
    @Override
    public String create() {
        if(rawMaterialPayRollService.findDiscountProducerByDate(getInstance().getStartDate(),getInstance().getEndDate()).size() > 0)
        {
            addDatesDuplicatesMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        return super.create();
    }

    private void addDatesDuplicatesMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "DiscountProducer.DatesDuplicatesMessage", getDisplayPropertyValue());
    }

    @End
    @Override
    public String update() {
        if(rawMaterialPayRollService.findDiscountProducerByDate(getInstance().getStartDate(),getInstance().getEndDate()).size() > 1)
        {
            addDatesDuplicatesMessage();
            return com.encens.khipus.framework.action.Outcome.REDISPLAY;
        }
        return super.update();
    }


    public Boolean getState() {
        if(getInstance().getState()!=null)
            this.state = getInstance().getState().equals("ENABLE") ? true:false ;

        if(state)
            getInstance().setState("ENABLE");
        else
            getInstance().setState("DISABLE");
        return state;
    }

    public void setState(Boolean state) {
        if(getInstance().getState()!=null)
            this.state = getInstance().getState().equals("ENABLE") ? true:false ;

        if(state)
            getInstance().setState("ENABLE");
        else
            getInstance().setState("DISABLE");
        this.state = state;
    }

}
