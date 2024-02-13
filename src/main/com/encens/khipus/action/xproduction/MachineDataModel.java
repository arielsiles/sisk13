package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.XMachine;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * CashAccountDataModel
 *
 * @author
 * @version 2.0
 */
@Name("machineDataModel")
@Scope(ScopeType.PAGE)
public class MachineDataModel extends QueryDataModel<Long,XMachine> {

    private static final String[] RESTRICTIONS =
            {
             "lower(machine.name) like  concat('%',concat(lower(#{machineDataModel.criteria.name}), '%'))",
            };

    @Create
    public void init() {
        sortProperty = "machine.name";
    }

    @Override
    public String getEjbql() {
        return "select machine from XMachine machine";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }


}
