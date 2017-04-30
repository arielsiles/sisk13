package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.production.DiscountProducer;
import com.encens.khipus.model.production.ProductiveZone;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 22-05-13
 * Time: 05:30 PM
 * To change this template use File | Settings | File Templates.
 */
@Name("discountProducerDataModel")
@Scope(ScopeType.PAGE)
public class DiscountProducerDataModel extends QueryDataModel<Long, DiscountProducer> {

    private static final String[] RESTRICTIONS = {
            "discountProducer.startDate = #{discountProducerDataModel.criteria.startDate}",
            "discountProducer.endDate = #{discountProducerDataModel.criteria.endDate}"
    };

    @Override
    public String getEjbql() {
        return "select discountProducer from DiscountProducer discountProducer";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
