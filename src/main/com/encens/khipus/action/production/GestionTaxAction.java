package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.GestionTax;
import com.encens.khipus.model.production.ProductiveZone;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 22-05-13
 * Time: 05:25 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("gestionTaxAction")
@Scope(ScopeType.CONVERSATION)
public class GestionTaxAction extends GenericAction<GestionTax> {

    //TODO change the name initContinent
    @Factory(value = "gestionTax", scope = ScopeType.STATELESS)
    public GestionTax initContinent() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "endDate";
    }

    @Override
    @End
    public String update() {
        if(getInstance().getProducerTaxes().size() > 0)
        {
         addInUsedMessage();
            return Outcome.REDISPLAY;
        }
    return super.update();
    }

    @Override
    @End
    public String delete() {
        if(getInstance().getProducerTaxes().size() > 0)
        {
            addInUsedDeleteMessage();
            return Outcome.REDISPLAY;
        }
     return super.delete();
    }

    private void addInUsedDeleteMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "GestionTax.InUsedMessageDelete");
    }

    private void addInUsedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "GestionTax.InUsedMessage");
    }


}
