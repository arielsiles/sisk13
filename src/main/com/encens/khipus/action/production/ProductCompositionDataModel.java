package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.ProcessedProduct;
import com.encens.khipus.model.production.ProductComposition;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/17/13
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("productCompositionDataModel")
@Scope(ScopeType.PAGE)
public class ProductCompositionDataModel extends QueryDataModel<Long, ProductComposition> {

    private static final String[] RESTRICTIONS = {
            "productComposition.active = #{true}",
            "upper(productComposition.processedProduct.name) like concat(concat('%',upper(#{productCompositionDataModel.criteria.processedProduct.name})), '%')",
            "upper(productComposition.processedProduct.code) like concat(concat('%',upper(#{productCompositionDataModel.criteria.processedProduct.code})), '%')"
    };

    @Override
    public ProductComposition createInstance() {
        ProductComposition pc = super.createInstance();
        if (pc.getProcessedProduct() == null) {
            pc.setProcessedProduct(new ProcessedProduct());
        }
        return pc;
    }

    @Create
    public void init() {
        sortProperty = "productComposition.processedProduct.name";
    }

    @Override
    public String getEjbql() {
        return  "select productComposition " +
                "from ProductComposition productComposition " +
                "left join fetch productComposition.processedProduct ";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
