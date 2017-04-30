package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.employees.DismissalDetail;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author
 * @version 3.4
 */

@Name("dismissalDetailAction")
@Scope(ScopeType.CONVERSATION)
public class DismissalDetailAction extends GenericAction<DismissalDetail> {
}