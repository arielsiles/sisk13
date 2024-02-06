package com.encens.khipus.service.xproduction;

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
public interface ProductionService {

    List<Supply> getSupplyList(Production production, SupplyType type);
    void createProduction(Production production, List<Supply> ingredientSupplyList, List<Supply> materialSupplyList);
    void updateProduction(Production production, List<Supply> ingredientSupplyList, List<Supply> materialSupplyList);
    void deleteProduction(Production production);
    void assignProduct(Production production, ProductionProduct product);
    void removeProductionProduct(ProductionProduct product, Production production);

    List<MaterialInput> getMaterialInput(String productItemCode);
    List<MaterialInput> getIngredientOrMaterialInput(String productItemCode, SupplyType type);

    void assignMaterial(Production production, Supply supply);
    void removeSupply(Supply supply);
    List<Object[]> getAllProductionSuplies(Production production);
}
