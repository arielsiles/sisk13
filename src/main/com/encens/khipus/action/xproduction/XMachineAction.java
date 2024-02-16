package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.xproduction.XMachine;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("xmachineAction")
@Scope(ScopeType.CONVERSATION)
public class XMachineAction extends GenericAction<XMachine> {

    @Factory(value = "xmachine", scope = ScopeType.STATELESS)
    public XMachine initXMachine() {
        return getInstance();
    }

}
