package com.encens.khipus.action.xproduction;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.MaterialInput;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Name("materialInputDataModel")
@Scope(ScopeType.PAGE)
public class MaterialInputDataModel extends QueryDataModel<Long, MaterialInput> {

    private String name;
    private String productItemCode;

    private static final String[] RESTRICTIONS = {
            "lower(materialInput.productItem.name) like concat('%', concat(lower(#{materialInputDataModel.name}), '%'))",
            "lower(materialInput.productItem.productItemCode) = #{materialInputDataModel.productItemCode}"
    };

    @Create
    public void init() {
        //sortProperty = "";
    }

    @Override
    public String getEjbql() {
        return "select materialInput from MaterialInput materialInput";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }
}