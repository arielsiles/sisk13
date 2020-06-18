package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.CustomerCategory;
import com.encens.khipus.model.customers.PriceItem;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@Name("priceItemService")
@AutoCreate
public class PriceItemServiceBean extends GenericServiceBean implements PriceItemService {

    @In(value = "#{entityManager}")
    private EntityManager em;


    @Override
    public List<PriceItem> getPriceItems(CustomerCategory customerCategory) {

        List<PriceItem> resultList = em.createQuery("select p from PriceItem p " +
                " where p.customerCategory =:customerCategory")
                .setParameter("customerCategory", customerCategory)
                .getResultList();

        return resultList;
    }

    @Override
    public Map<String, BigDecimal> getPriceItemsMap(CustomerCategory customerCategory){

        Map<String, BigDecimal> resultListMap = new HashMap<String, BigDecimal>();
        List<PriceItem> priceItemList = getPriceItems(customerCategory);

        for (PriceItem priceItem : priceItemList){
            resultListMap.put(priceItem.getProductItemCode(), priceItem.getSalePrice());
        }

        return resultListMap;
    }
}
