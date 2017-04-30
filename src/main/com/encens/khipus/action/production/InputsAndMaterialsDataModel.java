package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.MetaProduct;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/19/13
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("inputsAndMaterialsDataModel")
@Scope(ScopeType.PAGE)
public class InputsAndMaterialsDataModel extends QueryDataModel<Long, MetaProduct> {
    private static final String[] RESTRICTIONS = {
            "upper(meta.name) like concat(concat('%',upper(#{inputsAndMaterialsDataModel.criteria.name})), '%')",
            "upper(meta.code) like concat(concat('%',upper(#{inputsAndMaterialsDataModel.criteria.code})), '%')"
    };

    @Create
    public void init() {
        sortProperty = "meta.name";
    }

    @Override
    public String getEjbql() {
       /* return "select meta " +
               "from MetaProduct meta " +
               "where meta not in (select processedProduct from ProcessedProduct processedProduct)";  */
        return "SELECT meta FROM MetaProduct meta";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
