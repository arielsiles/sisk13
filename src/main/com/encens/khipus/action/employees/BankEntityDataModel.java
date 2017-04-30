package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.employees.BankEntity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for BANK_ENTITY
 *
 * @author
 */

@Name("bankEntityDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('BANKENTITY','VIEW')}")
public class BankEntityDataModel extends QueryDataModel<Long, BankEntity> {
    private static final String[] RESTRICTIONS = {
            "lower(bankEntity.name) like concat('%', concat(lower(#{bankEntityDataModel.criteria.name}), '%'))",
            "lower(bankEntity.code) like concat(lower(#{bankEntityDataModel.criteria.code}), '%')"};

    @Create
    public void init() {
        sortProperty = "bankEntity.name";
    }

    @Override
    public String getEjbql() {
        return "select bankEntity from BankEntity bankEntity";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}