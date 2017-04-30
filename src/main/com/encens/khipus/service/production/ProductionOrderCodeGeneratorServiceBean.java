package com.encens.khipus.service.production;

import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.ProductionOrder;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Calendar;
import java.util.List;

@Name("productionOrderCodeGeneratorService")
@Stateless
@AutoCreate
public class ProductionOrderCodeGeneratorServiceBean implements ProductionOrderCodeGeneratorService {

    @In private EntityManager entityManager;

    @Override
    public int findLasCounter(String seed) {
        seed = seed + "%";
        List<String> codes = entityManager.createNamedQuery("ProductionOrder.findBySubDateOnCode")
                                          .setParameter("seed", seed)
                                          .getResultList();

        String greatest = seed + "0";
        for(String code : codes) {
            if (code.compareTo(greatest) > 0) {
                greatest = code;
            }
        }
        if(seed.length() == 0)
            return 0;

        String integerPart = greatest.substring(seed.length());
        return Integer.parseInt(integerPart);
    }

    @Override
    public int getCounterCode(){
        try{
        List<ProductionOrder> datas = entityManager.createQuery("SELECT productionOrder FROM ProductionOrder productionOrder ORDER BY productionOrder.id DESC ")
                                      .getResultList();
        List<BaseProduct> baseProducts = entityManager.createQuery("SELECT baseProduct FROM BaseProduct baseProduct ORDER BY baseProduct.id DESC ")
                                         .getResultList();
        if(datas.size() > 0)
        {
            int valProOrd = getCode(datas.get(0).getCode());
            int valBaseProd = 0;
            if(baseProducts.size() > 0)
                valBaseProd = getCode(baseProducts.get(0).getCode());

        return (valProOrd > valBaseProd)?valProOrd:valBaseProd;
        }else
        {
            return 0;
        }
        }catch (NoResultException e){
        return 0;
        }
    }

    public int getCode(String cod){

        String[] array = cod.split("\\-");
            String aux = array[0];
            String monthOrder = aux.substring(aux.length() - 2);
            int val = Integer.parseInt(array[1]);
            if(Calendar.getInstance().get(Calendar.MONTH)+1 > Integer.parseInt(monthOrder))
                val = 0;

            return val;

    }
}
