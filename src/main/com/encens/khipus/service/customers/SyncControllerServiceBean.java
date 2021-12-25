package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.EconomicActivity;
import com.encens.khipus.model.customers.MeasureUnitSIN;
import com.encens.khipus.model.customers.ProductsServices;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by AS.
 */

@Stateless
@Name("syncControllerService")
@AutoCreate
public class SyncControllerServiceBean implements SyncControllerService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public void syncProductsAndServices(List<ProductsServices> productsServicesList) {

        System.out.println("..............syncProductsAndServices...");

        em.createQuery("delete from ProductsServices").executeUpdate();
        em.flush();

        for (ProductsServices item : productsServicesList){
            em.persist(item);
            em.flush();
        }
    }

    @Override
    public void syncActivities(List<EconomicActivity> economicActivityList) {

        em.createQuery("delete from EconomicActivity").executeUpdate();
        em.flush();

        for (EconomicActivity economicActivity : economicActivityList){
            em.persist(economicActivity);
            em.flush();
        }

    }

    @Override
    public void syncMeasureUnits(List<MeasureUnitSIN> measureUnitSINList) {

        em.createQuery("delete from MeasureUnitSIN").executeUpdate();
        em.flush();

        for (MeasureUnitSIN measureUnitSIN : measureUnitSINList){
            em.persist(measureUnitSIN);
            em.flush();
        }
    }
}
