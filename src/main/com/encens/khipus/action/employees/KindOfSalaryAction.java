package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.finances.KindOfSalary;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

/**
 * KindOfSalary action class
 *
 * @author
 * @version 1.0
 */
@Name("kindOfSalaryAction")
@Scope(ScopeType.CONVERSATION)
public class KindOfSalaryAction extends GenericAction<KindOfSalary> {

    @Factory(value = "kindOfSalary", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('KINDOFSALARY','VIEW')}")
    public KindOfSalary initKindOfSalary() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "type";
    }
}