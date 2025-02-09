package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.xproduction.ProductionLine;
import com.encens.khipus.model.xproduction.XProcess;
import com.encens.khipus.service.xproduction.ProductionLineService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Data model for Customer category
 *
 * @author:
 */

@Name("productionProcessDataModel")
@Scope(ScopeType.PAGE)
public class ProductionProcessDataModel extends QueryDataModel<Long, XProcess> {

    @In
    private ProductionLineService productionLineService;
    private ProductionLine productionLine;

    private static final String[] RESTRICTIONS = {
            "lower(process.name) like concat('%', concat(lower(#{productionProcessDataModel.criteria.name}), '%'))",
            "process.productionLine = #{productionProcessDataModel.productionLine}"
    };

    @Create
    public void init() {
        sortProperty = "process.position";
        productionLine = productionLineService.getDefaultProductionLine();
    }

    @Override
    public String getEjbql() {
        return "select process from XProcess process";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public ProductionLine getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }
}
