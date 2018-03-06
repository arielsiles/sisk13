package com.encens.khipus.service.warehouse;

import com.encens.khipus.action.warehouse.reports.ProductInventoryReportAction;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * @author
 * @version 2.0
 */
@Stateless
@Name("initialInventoryService")
@AutoCreate
public class InitialInventoryServiceBean extends GenericServiceBean implements InitialInventoryService {

    public void createInitialInventory(Collection<ProductInventoryReportAction.CollectionData> collectionDatas, String warehouseCode, Date startDate){

        String year = DateUtils.getCurrentYear(startDate)+1 + "";

        for (ProductInventoryReportAction.CollectionData data:collectionDatas){
            InitialInventory initialInventory = new InitialInventory();

            initialInventory.setProductItemCode(data.getCode());
            initialInventory.setProductItemName(data.getProductName());
            initialInventory.setQuantity(data.getBalance());
            initialInventory.setWarehouseCode(warehouseCode);
            initialInventory.setUnitCost(data.getUnitCost());
            initialInventory.setYear((DateUtils.getCurrentYear(startDate)+1)+"");
            initialInventory.setCompanyNumber(Constants.defaultCompanyNumber);

            getEntityManager().persist(initialInventory);
            getEntityManager().flush();

        }

    }

}
