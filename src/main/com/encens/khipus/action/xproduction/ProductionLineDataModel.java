package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.ProductionLine;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

@Name("productionLineDataModel")
@Scope(ScopeType.PAGE)
public class ProductionLineDataModel extends QueryDataModel<Long, ProductionLine> {

    private static final String[] RESTRICTIONS =
            {"lower(productionLine.name) like concat('%', concat(lower(#{productionLineDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "productionLine.id";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select productionLine from ProductionLine productionLine";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}