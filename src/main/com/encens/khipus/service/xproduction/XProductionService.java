package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.SupplyType;
import com.encens.khipus.model.xproduction.*;

import javax.ejb.Local;
import java.util.List;

/**
 * Product service interface
 *
 * @author
 * @version $Id: ProductService.java 2008-9-11 13:50:25 $
 */
@Local
public interface XProductionService {

    List<XSupply> getSupplyList(XProduction production, SupplyType type);
    void createProduction(XProduction production, List<XSupply> ingredientSupplyList, List<XSupply> materialSupplyList);
    void updateProduction(XProduction production, List<XSupply> ingredientSupplyList, List<XSupply> materialSupplyList, List<XProductionLabor> laborList);
    void deleteProduction(XProduction production);
    void assignProduct(XProduction production, XProductionProduct product);
    void removeProductionProduct(XProductionProduct product, XProduction production);

    List<XMaterialInput> getMaterialInput(String productItemCode);
    List<XMaterialInput> getIngredientOrMaterialInput(String productItemCode, SupplyType type);

    void assignMaterial(XProduction production, XSupply supply);
    void removeSupply(XSupply supply);
    List<Object[]> getAllProductionSuplies(XProduction production);

    List<XProductionLabor> getLaborList(XProduction instance);
}
