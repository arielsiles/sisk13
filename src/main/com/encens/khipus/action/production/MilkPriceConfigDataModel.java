package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.MilkPriceConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("milkPriceConfigDataModel")
@Scope(ScopeType.PAGE)
public class MilkPriceConfigDataModel extends QueryDataModel<Long, MilkPriceConfig> {

    private static final String[] RESTRICTIONS = {};

    @Override
    public String getEjbql() {
        return "select milkPriceConfig from MilkPriceConfig milkPriceConfig";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
