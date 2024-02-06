package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.production.ProducerTax;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 22-05-13
 * Time: 05:25 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("producerTaxAction")
@Scope(ScopeType.CONVERSATION)
public class ProducerTaxAction extends GenericAction<ProducerTax> {

    //TODO change the name initContinent
    @Factory(value = "producerTax", scope = ScopeType.STATELESS)
    public ProducerTax initContinent() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "formNumber";
    }

}
