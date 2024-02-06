package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.RawMaterialDiscount;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("rawMaterialDiscountDataModel")
@Scope(ScopeType.PAGE)
public class RawMaterialDiscountDataModel extends QueryDataModel<Long, RawMaterialDiscount> {

    private static final String[] RESTRICTIONS = {""};

    @Create
    public void init() {
        //sortProperty = "";
    }

    @Override
    public String getEjbql() {
        return "select rawMaterialDiscount from RawMaterialDiscount rawMaterialDiscount";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

}
