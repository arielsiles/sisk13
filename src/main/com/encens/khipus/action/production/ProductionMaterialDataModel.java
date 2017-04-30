package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProductionMaterial;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/5/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("productionMaterialDataModel")
@Scope(ScopeType.PAGE)
public class ProductionMaterialDataModel extends QueryDataModel<Long, ProductionMaterial> {

    private static final String[] RESTRICTIONS = {
            "upper(productionMaterial.name) like concat(concat('%',upper(#{productionMaterialDataModel.criteria.name})), '%')",
            "upper(productionMaterial.code) like concat(concat('%',upper(#{productionMaterialDataModel.criteria.code})), '%')",
            "upper(productionMaterial.description) like concat(concat('%',upper(#{productionMaterialDataModel.criteria.description})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "productionMaterial.name";
    }

    @Override
    public String getEjbql() {
        return "select productionMaterial " +
                "from ProductionMaterial productionMaterial " +
                "left join fetch productionMaterial.measureUnitProduction";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
