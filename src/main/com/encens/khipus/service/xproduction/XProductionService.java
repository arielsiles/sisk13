package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.production.SupplyType;
import com.encens.khipus.model.xproduction.*;

import javax.ejb.Local;
import java.util.Date;
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

    /**
     * Persiste un nuevo producto terminado (id == null) inmediatamente, enlazandolo
     * a la produccion. Usado por "+ Producto Terminado". Las ediciones posteriores de
     * cantidad se persisten en el ciclo normal updateProduction via em.merge.
     */
    void addFinishedProductDirect(XProduction production, XProductionProduct product);

    List<XMaterialInput> getMaterialInput(String productItemCode);
    List<XMaterialInput> getIngredientOrMaterialInput(String productItemCode, SupplyType type);

    void assignMaterial(XProduction production, XSupply supply);
    void removeSupply(XSupply supply);
    List<Object[]> getAllProductionSuplies(XProduction production);

    List<XProductionLabor> getLaborList(XProduction instance);

    List<Object[]> findProductionInputsByDates(Date initDate, Date endDate);

    List<Object[]> getSumRawMaterialInProduction(Date initDate, Date endDate);

    List<XSupply> getRawMaterialInProduction(String productItemCode, Date initDate, Date endDate);

    List<XSupply> getAllRawMaterialInProduction(Date initDate, Date endDate);

    /**
     * Reproceso de ordenes de linea ULEXITA (tabla satelite xpr_produccion_ulexita) atribuido
     * a un articulo via cod_art_reproc_final, en el rango de fechas. Ordenes no anuladas.
     */
    List<XProductionUlexita> getUlexitaReprocessByArticle(String productItemCode, Date initDate, Date endDate);
}
