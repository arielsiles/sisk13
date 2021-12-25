package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Dosage;
import com.encens.khipus.model.customers.EconomicActivity;
import com.encens.khipus.model.customers.MeasureUnitSIN;
import com.encens.khipus.model.customers.ProductsServices;

import javax.ejb.Local;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 */
@Local
public interface SyncControllerService {

    void syncProductsAndServices(List<ProductsServices> productsServicesList);
    void syncActivities(List<EconomicActivity> economicActivityList);
    void syncMeasureUnits(List<MeasureUnitSIN> measureUnitSINList);

}
