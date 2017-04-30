package com.encens.khipus.action.contacts;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Title;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * Created by IntelliJ IDEA.
 *
 * @author
 */
@Name("titleAction")
@Scope(ScopeType.CONVERSATION)
public class TitleAction extends GenericAction<Title> {

    @Factory(value = "title", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('TITLE','VIEW')}")
    public Title initTitle() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }
}
