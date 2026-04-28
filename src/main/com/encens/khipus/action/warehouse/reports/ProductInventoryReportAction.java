package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.model.xproduction.XProductionProduct;
import com.encens.khipus.service.customers.ArticleOrderService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.CollectMaterialService;
import com.encens.khipus.service.production.ProductionOrderService;
import com.encens.khipus.service.warehouse.InitialInventoryService;
import com.encens.khipus.service.warehouse.MovementDetailService;
import com.encens.khipus.service.warehouse.ProductInventoryService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.xproduction.XProductionService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.hssf.usermodel.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("productInventoryReportAction")
@Scope(ScopeType.PAGE)
public class ProductInventoryReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private ProductItem productItem;
    private Warehouse warehouse;
    private Boolean articlesWithMovement = true;

    private Group group;
    private SubGroup subGroup;

    @In
    private MovementDetailService movementDetailService;
    @In
    private ArticleOrderService articleOrderService;
    @In
    private ProductItemService productItemService;
    @In
    private ProductInventoryService productInventoryService;
    @In
    private ProductionOrderService productionOrderService;
    @In
    private CollectMaterialService collectMaterialService;
    @In
    private XProductionService xproductionService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    @In(create = true)
    KardexProductMovementAction kardexProductMovementAction;

    @In
    private InitialInventoryService initialInventoryService;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {
        return "";
    }

    public void startInventoryAnnual(){

        Collection<CollectionData> beanCollection = calculateCollectionData2();

        for (CollectionData data : beanCollection){
            System.out.println("|"+ data.getCode() +"|"+ data.getProductName() +"|"+ data.getInitialAmount() +"|"+ data.getUnitCost() +"|"+ data.getBalance());
        }

        initialInventoryService.createInitialInventory(beanCollection, warehouse.getWarehouseCode(), startDate);
    }

    public void generateReport() {

        log.debug("generating Product Inventory Report................................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        Collection<CollectionData> beanCollection = calculateCollectionData2();

        String groupName = "";
        if (group != null) {
            beanCollection = filterByGroup(beanCollection, group);
            groupName = " - " + group.getName();
        }

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE GENERAL DE INVENTARIO");
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("warehouse", warehouse.getFullName() + groupName);

        parameters.putAll(paramMap);

        System.out.println("|Codigo|Articulo|Unidad|Inv Inicial|Entradas|Salidas|Saldo");
        for (CollectionData data : beanCollection){
            System.out.println("|"+ data.getCode() +"|"+ data.getProductName() +"|"+ data.getUnit() +"|"+ data.getInitialAmount() +"|"+ data.getEntryAmount() +"|"+ data.getOutputAmount() +"|"+ data.getBalance());
        }

        try{
            if (getReportFormat() != null && (getReportFormat().name().equals("XLS") || getReportFormat().name().equals("XLSX"))) {
                exportarExcel(beanCollection, companyConfiguration, groupName);
            } else {
                File jrxmlFile = new File(JSFUtil.getRealPath("/warehouse/reports/productInventoryReport.jrxml"));
                String jrxmlContent = new String(java.nio.file.Files.readAllBytes(jrxmlFile.toPath()), "UTF-8");
                jrxmlContent = preprocessJrxml(jrxmlContent);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(jrxmlContent.getBytes("UTF-8"));
                net.sf.jasperreports.engine.JasperReport jasperReport = net.sf.jasperreports.engine.JasperCompileManager.compileReport(bais);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(beanCollection));
                exportarPDF(jasperPrint);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void generateGroupedReport() {

        log.debug("generating Product Inventory Report................................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        Collection<CollectionData> beanCollection = calculateCollectionData2();

        if (group != null) {
            beanCollection = filterByGroup(beanCollection, group);
        }
        if (subGroup != null) {
            beanCollection = filterBySubGroup(beanCollection, subGroup);
        }

        String filterLabel = warehouse.getName();
        if (group != null) filterLabel += " | " + group.getName();

        String period = "Del " + DateUtils.format(startDate, "dd/MM/yyyy") + " al " + DateUtils.format(endDate, "dd/MM/yyyy");
        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE GENERAL DE INVENTARIO");
        paramMap.put("filterLabel", filterLabel);
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("period", period);
        //paramMap.put("warehouse", warehouse.getFullName() + groupName);

        parameters.putAll(paramMap);

        System.out.println("|Codigo|Articulo|Unidad|Inv Inicial|Entradas|Salidas|Saldo");
        for (CollectionData data : beanCollection){
            System.out.println("|"+ data.getCode() +"|"+ data.getProductName() +"|"+ data.getUnit() +"|"+ data.getInitialAmount() +"|"+ data.getEntryAmount() +"|"+ data.getOutputAmount() +"|"+ data.getBalance());
        }

        try{
            if (getReportFormat() != null && (getReportFormat().name().equals("XLS") || getReportFormat().name().equals("XLSX"))) {
                exportarExcelAgrupado(beanCollection, companyConfiguration, period, filterLabel);
            } else {
                File jrxmlFile = new File(JSFUtil.getRealPath("/warehouse/reports/productInventoryGroupedReport.jrxml"));
                String jrxmlContent = new String(java.nio.file.Files.readAllBytes(jrxmlFile.toPath()), "UTF-8");
                jrxmlContent = preprocessJrxml(jrxmlContent);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(jrxmlContent.getBytes("UTF-8"));
                net.sf.jasperreports.engine.JasperReport jasperReport = net.sf.jasperreports.engine.JasperCompileManager.compileReport(bais);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(beanCollection));
                exportarPDF(jasperPrint);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void generateSubGroupReport() {

        log.debug("generating Product Inventory Report................................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        Collection<CollectionData> beanCollection = calculateCollectionData2();

        String subGroupName = "";
        if (getSubGroup() != null) {
            beanCollection = filterBySubGroup(beanCollection, getSubGroup());
            subGroupName = " - " + subGroup.getName();
        }

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE DE INVENTARIO POR SUBGRUPO");
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("warehouse", warehouse.getFullName() + subGroupName);

        parameters.putAll(paramMap);

        System.out.println("|Codigo|Articulo|Unidad|Inv Inicial|Entradas|Salidas|Saldo");
        for (CollectionData data : beanCollection){
            System.out.println("|"+ data.getCode() +"|"+ data.getProductName() +"|"+ data.getUnit() +"|"+ data.getInitialAmount() +"|"+ data.getEntryAmount() +"|"+ data.getOutputAmount() +"|"+ data.getBalance());
        }

        try{
            if (getReportFormat() != null && (getReportFormat().name().equals("XLS") || getReportFormat().name().equals("XLSX"))) {
                exportarExcel(beanCollection, companyConfiguration, subGroupName);
            } else {
                File jrxmlFile = new File(JSFUtil.getRealPath("/warehouse/reports/productInventoryReport.jrxml"));
                String jrxmlContent = new String(java.nio.file.Files.readAllBytes(jrxmlFile.toPath()), "UTF-8");
                jrxmlContent = preprocessJrxml(jrxmlContent);
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(jrxmlContent.getBytes("UTF-8"));
                net.sf.jasperreports.engine.JasperReport jasperReport = net.sf.jasperreports.engine.JasperCompileManager.compileReport(bais);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(beanCollection));
                exportarPDF(jasperPrint);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Collection<CollectionData> filterByGroup(Collection<CollectionData> beanCollection, Group group){
        Collection<CollectionData> result = new ArrayList();
        List<ProductItem> productItemGroupList = productItemService.findByGroupCode(group.getGroupCode());

        for (CollectionData collectionData : beanCollection) {
            for (ProductItem item : productItemGroupList) {
                if (item.getProductItemCode().equals(collectionData.getCode())) {
                    result.add(collectionData);
                }
            }
        }

        return result;
    }

    public Collection<CollectionData> filterBySubGroup(Collection<CollectionData> beanCollection, SubGroup subGroup){
        Collection<CollectionData> result = new ArrayList();
        List<ProductItem> productItemSubGroupList = productItemService.findBySubGroupCode(subGroup.getGroupCode(), subGroup.getSubGroupCode());

        for (CollectionData collectionData : beanCollection) {
            for (ProductItem item : productItemSubGroupList) {
                if (item.getProductItemCode().equals(collectionData.getCode())) {
                    result.add(collectionData);
                }
            }
        }

        return result;
    }

    /**
     * 1. Calcula inventario de articulos por almacen dadas Fecha inicio y Fecha fin
     * 2. Calcula Costo unitario promedio segun el movimiento entre las fechas dadas (Inicio y Fin)
     *
     *
     * @return
     */
    public Collection<CollectionData> calculateCollectionData2(){

        List<CollectionData> beanCollection2 = new ArrayList();

        /** 1.- Listado de articulos con saldos iniciales de gestion **/
        List<InventoryPeriod> InventoryPeriodList = productInventoryService.findInitialInventoryAll(warehouse.getWarehouseCode(), DateUtils.getCurrentYear(startDate).toString());

        /** 2.- Obtiene en listas entradas y salidas de articulos **/
        List<MovementDetail> movementDetailList;
        if (warehouse.getWarehouseCode().equals(WarehouseType.DAIRY))
            movementDetailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouse.getWarehouseCode(), startDate, endDate, null);
        else
            movementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), startDate, endDate, null);

        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrders(startDate, endDate);
        List<BaseProduct> baseProductList         = productionOrderService.findBaseProductByDate(startDate, endDate);
        List<ProductionProduct> productionProductList = productionOrderService.findProductionByDate(startDate, endDate);
        List<XProductionProduct> xproductionProductList = productionOrderService.findXProductionByDate(startDate, endDate);
        List<CollectMaterial> collectMaterialList = collectMaterialService.findApprovedCollectMaterial(startDate, endDate);
        List cashSaleDetailList = articleOrderService.findCashSaleDetailListGroupBy(startDate, endDate);
        List orderDetailList    = articleOrderService.findCustomerOrderDetailListGroupBy(startDate, endDate);

        /** 3.- Pre-calcular HashMaps para lookups O(1) en vez de O(N) **/
        Map<String, BigDecimal> initialBalanceMap = new HashMap<String, BigDecimal>();
        Collection<CollectionData> initialArticleList = calculateInitialArticle(warehouse.getWarehouseCode(), startDate);
        for (CollectionData article : initialArticleList) {
            initialBalanceMap.put(article.getCode(), article.getBalance());
        }

        // Acopio MP por codigo
        Map<String, BigDecimal> collectMaterialMap = new HashMap<String, BigDecimal>();
        for (CollectMaterial cm : collectMaterialList) {
            String code = cm.getMetaProduct().getProductItemCode();
            BigDecimal prev = collectMaterialMap.get(code);
            collectMaterialMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, cm.getBalanceWeight(), 6));
        }

        // Produccion por codigo
        Map<String, BigDecimal> productionMap = new HashMap<String, BigDecimal>();
        for (ProductionProduct product : productionProductList) {
            String code = product.getProductItemCode();
            BigDecimal prev = productionMap.get(code);
            productionMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, product.getQuantity(), 6));
        }

        // XProduccion por codigo
        Map<String, BigDecimal> xproductionMap = new HashMap<String, BigDecimal>();
        for (XProductionProduct product : xproductionProductList) {
            String code = product.getProductItemCode();
            BigDecimal prev = xproductionMap.get(code);
            xproductionMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, product.getQuantity(), 6));
        }

        // Ordenes de produccion por codigo
        Map<String, BigDecimal> productionOrderMap = new HashMap<String, BigDecimal>();
        for (ProductionOrder po : productionOrderList) {
            String code = po.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode();
            BigDecimal prev = productionOrderMap.get(code);
            productionOrderMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(po.getProducedAmount()), 6));
        }

        // Reprocesos por codigo
        Map<String, BigDecimal> reprocessMap = new HashMap<String, BigDecimal>();
        for (BaseProduct baseProduct : baseProductList) {
            for (SingleProduct singleProduct : baseProduct.getSingleProducts()) {
                String code = singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode();
                BigDecimal prev = reprocessMap.get(code);
                reprocessMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(singleProduct.getAmount()), 6));
            }
        }

        // Movimientos entrada/salida por codigo
        Map<String, BigDecimal> movEntryMap = new HashMap<String, BigDecimal>();
        Map<String, BigDecimal> movOutputMap = new HashMap<String, BigDecimal>();
        for (MovementDetail detail : movementDetailList) {
            String code = detail.getProductItemCode();
            if (detail.getMovementType().equals(MovementDetailType.E)) {
                BigDecimal prev = movEntryMap.get(code);
                movEntryMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, detail.getQuantity(), 6));
            }
            if (detail.getMovementType().equals(MovementDetailType.S)) {
                BigDecimal prev = movOutputMap.get(code);
                movOutputMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, detail.getQuantity(), 6));
            }
        }

        // Materia prima en XProduccion - FUERA del loop
        Map<String, BigDecimal> rawMaterialMap = new HashMap<String, BigDecimal>();
        if (warehouse.getWarehouseType().equals(WarehouseType.RAW_MATERIAL)) {
            List rawMaterialProductionList = xproductionService.getSumRawMaterialInProduction(startDate, endDate);
            for (int i = 0; i < rawMaterialProductionList.size(); i++) {
                Object[] row = (Object[]) rawMaterialProductionList.get(i);
                rawMaterialMap.put((String) row[0], (BigDecimal) row[1]);
            }
        }

        // Ventas al contado por codigo
        Map<String, BigDecimal> cashSaleMap = new HashMap<String, BigDecimal>();
        for (int i = 0; i < cashSaleDetailList.size(); i++) {
            Object[] row = (Object[]) cashSaleDetailList.get(i);
            cashSaleMap.put((String) row[0], BigDecimalUtil.toBigDecimal((Long) row[1]));
        }

        // Pedidos por codigo
        Map<String, BigDecimal> orderMap = new HashMap<String, BigDecimal>();
        for (int i = 0; i < orderDetailList.size(); i++) {
            Object[] row = (Object[]) orderDetailList.get(i);
            orderMap.put((String) row[0], BigDecimalUtil.toBigDecimal((Long) row[1]));
        }

        /** 4.- Iterar articulos usando lookups O(1) **/
        for (InventoryPeriod inventoryPeriod : InventoryPeriodList) {

            String code = inventoryPeriod.getProductItemCode();

            BigDecimal initQuantity = initialBalanceMap.containsKey(code) ? initialBalanceMap.get(code) : BigDecimal.ZERO;

            CollectionData data = new CollectionData(
                    inventoryPeriod.getProductItem().getSubGroup().getName(),
                    code,
                    inventoryPeriod.getProductItem().getName(),
                    inventoryPeriod.getProductItem().getUsageMeasureCode(),
                    initQuantity,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );

            // Sumar entradas
            if (collectMaterialMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), collectMaterialMap.get(code), 6));
            if (productionMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), productionMap.get(code), 6));
            if (xproductionMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), xproductionMap.get(code), 6));
            if (productionOrderMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), productionOrderMap.get(code), 6));
            if (reprocessMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), reprocessMap.get(code), 6));
            if (movEntryMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), movEntryMap.get(code), 6));

            // Sumar salidas
            if (movOutputMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), movOutputMap.get(code), 6));
            if (rawMaterialMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), rawMaterialMap.get(code), 6));
            if (cashSaleMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), cashSaleMap.get(code), 6));
            if (orderMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), orderMap.get(code), 6));

            if (articlesWithMovement) {
                if (data.getInitialAmount().compareTo(BigDecimal.ZERO) > 0 ||
                        data.getEntryAmount().compareTo(BigDecimal.ZERO)   > 0 ||
                        data.getOutputAmount().compareTo(BigDecimal.ZERO)  > 0) {
                    beanCollection2.add(data);
                }
            } else {
                beanCollection2.add(data);
            }
        }

        for (CollectionData data : beanCollection2) {
            data.setBalance(BigDecimalUtil.sum(data.getInitialAmount(), data.getEntryAmount(), 6));
            data.setBalance(BigDecimalUtil.subtract(data.getBalance(), data.getOutputAmount(), 6));
        }

        Collections.sort(beanCollection2, new Comparator<CollectionData>() {
            @Override
            public int compare(CollectionData o1, CollectionData o2) {
                int subgroupComparison = o1.getSubgroupName().compareTo(o2.getSubgroupName());
                if (subgroupComparison == 0) {
                    return o1.getProductName().compareTo(o2.getProductName());
                }
                return subgroupComparison;
            }
        });

        return beanCollection2;
    }

    /**
     * 1. Calcula inventario de articulos por almacen dadas Fecha inicio y Fecha fin
     * 2. Calcula Costo unitario promedio segun el movimiento entre las fechas dadas (Inicio y Fin)
     *
     *
     * @return
     */
    public Collection<CollectionData> calculateCollectionData(){

        Collection<CollectionData> beanCollection = new ArrayList();


        /** 1.- Listado de articulos con saldos iniciales de gestion **/
        // Inventario inicio gestion
        List<InitialInventory> initialInventoryList   = productInventoryService.findInitialInventory(warehouse.getWarehouseCode(), DateUtils.getCurrentYear(startDate).toString());

        /** 2.- Obtiene en listas entradas y salidas de articulos: vales, ordenes de produccion, ventas, pedidos, etc. **/
        // Vales de movimiento
        List<MovementDetail> movementDetailList;
        if (warehouse.getWarehouseCode().equals("2"))
            movementDetailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouse.getWarehouseCode(), startDate, endDate, null);
        else
            movementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), startDate, endDate, null);

        // Ordenes de produccion
        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrders(startDate, endDate);
        List<BaseProduct> baseProductList         = productionOrderService.findBaseProductByDate(startDate, endDate);

        // Ventas al contado y pedidos
        List cashSaleDetailList = articleOrderService.findCashSaleDetailListGroupBy(startDate, endDate);
        List orderDetailList    = articleOrderService.findCustomerOrderDetailListGroupBy(startDate, endDate);

        /** 3.-  Calcula (x almacen) en la lista los saldos de los articulos, desde el 1er dia de la gestion hasta la fecha de inicio seleccionada **/
        // solo calcula correctamente saldos fisicos ?
        Collection<CollectionData> initialArticleList = calculateInitialArticle(warehouse.getWarehouseCode(), startDate);

        /** Se va llenando la lista final, con saldos fisicos a la fecha de inicio seleccionada **/
        /**  **/
        //* Inventario inicial inv_inicio **/
        for (InitialInventory initialInventory:initialInventoryList){

            /** Fijando el saldo inicial de un articulo **/
            BigDecimal initQuantity = BigDecimal.ZERO;
            for (CollectionData article:initialArticleList){
                if (initialInventory.getProductItemCode().equals(article.getCode())){
                    initQuantity = article.getBalance(); // Fijando solo el saldo fisico inicial
                    break;
                }
            }

            CollectionData data = new CollectionData(   initialInventory.getProductItem().getSubGroup().getName(),
                                                        initialInventory.getProductItemCode(),
                                                        initialInventory.getProductItem().getName(),
                                                        initialInventory.getProductItem().getUsageMeasureCode(),
                                                        initQuantity,
                                                        BigDecimal.ZERO,
                                                        BigDecimal.ZERO,
                                                        BigDecimal.ZERO,
                                                        //initialInventory.getUnitCost()
                                                        BigDecimal.ZERO
                                                    );
            /** Ordenes de produccion **/
            for (ProductionOrder productionOrder:productionOrderList){
                if (initialInventory.getProductItemCode().equals(productionOrder.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode())){
                    data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(productionOrder.getProducedAmount()), 6));
                }
            }
            /** Reprocesos **/
            for (BaseProduct baseProduct:baseProductList){
                for (SingleProduct singleProduct:baseProduct.getSingleProducts()){
                    if (initialInventory.getProductItemCode().equals(singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode())){
                        data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(singleProduct.getAmount()), 6));
                    }
                }

            }

            /** Sum Entry an Output **/
            for (MovementDetail detail:movementDetailList){
                if (initialInventory.getProductItemCode().equals(detail.getProductItemCode())) {
                    if (detail.getMovementType().equals(MovementDetailType.E)) {
                        data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), detail.getQuantity(), 6));
                    }
                    if (detail.getMovementType().equals(MovementDetailType.S))
                        data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), detail.getQuantity(), 6));
                }
            }

            /** Ventas al contado **/
            for (int i = 0; i < cashSaleDetailList.size(); i++) {
                Object[] row = (Object[]) cashSaleDetailList.get(i);
                String codart = (String)row[0];
                Long total = (Long) row[1];
                if (initialInventory.getProductItemCode().equals(codart)) {
                    data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), BigDecimalUtil.toBigDecimal(total), 6));
                }
            }

            /** Pedidos **/
            for (int i = 0; i < orderDetailList.size(); i++) {
                Object[] row = (Object[]) orderDetailList.get(i);
                String codart = (String)row[0];
                Long total = (Long) row[1];
                if (initialInventory.getProductItemCode().equals(codart)) {
                    data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), BigDecimalUtil.toBigDecimal(total), 6));
                }
            }

            /** Añade a la lista Todos los articulos ó solo los con movimiento, segun se elija en la vista**/
            if (articlesWithMovement){ // Articulos solo con Movimiento o inventario inicial
                if (data.getInitialAmount().compareTo(BigDecimal.ZERO) > 0 ||
                        data.getEntryAmount().compareTo(BigDecimal.ZERO)   > 0 ||
                        data.getOutputAmount().compareTo(BigDecimal.ZERO)  > 0){
                    beanCollection.add(data);
                }
            }else {
                beanCollection.add(data);
            }
        }

        /** ---------------------------------------------------------------------------------------------------- **/
        /** ---------------------------------------------------------------------------------------------------- **/
        /** Se crea una lista con todos los articulos como Entrada (E) ó Salida (S) considerando Cantidad, Costo Unit, Costo Total**/
        List<ArticleMovement> resultList = new ArrayList<ArticleMovement>();

        for (InitialInventory initialInventory:initialInventoryList){
                ArticleMovement articleMovement = new ArticleMovement(
                        DateUtils.firstDayOfYear(DateUtils.getCurrentYear(startDate)),
                        initialInventory.getProductItemCode(),
                        MovementDetailType.E,
                        initialInventory.getQuantity(),
                        initialInventory.getUnitCost(),
                        BigDecimalUtil.multiply(initialInventory.getQuantity(), initialInventory.getUnitCost(), 6));
                resultList.add(articleMovement);
        }

        /** Vales de movimiento **/
        List<MovementDetail> detailList;
        if (warehouse.getWarehouseCode().equals("2"))
            detailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouse.getWarehouseCode(), DateUtils.firstDayOfYear(DateUtils.getCurrentYear(startDate)), endDate, null);
        else
            detailList = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), DateUtils.firstDayOfYear(DateUtils.getCurrentYear(startDate)), endDate, null);

        for (MovementDetail detail:detailList){
            ArticleMovement articleMovement = new ArticleMovement(
                    //detail.getMovementDetailDate(),
                    DateUtils.parse(DateUtils.format(detail.getMovementDetailDate(), "yyyy-MM-dd"), "yyyy-MM-dd"),
                    detail.getProductItemCode(),
                    detail.getMovementType(),
                    detail.getQuantity(),
                    //detail.getUnitPurchasePrice(),
                    detail.getUnitCost(),
                    //detail.getPurchasePrice()
                    detail.getAmount()
            );
            resultList.add(articleMovement);
        }

        /** Ordenes de produccion **/
        for (ProductionOrder productionOrder:productionOrderList){
            //data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(productionOrder.getProducedAmount()), 2));
            ArticleMovement articleMovement = new ArticleMovement(
                    //productionOrder.getProductionPlanning().getDate(),
                    DateUtils.parse(DateUtils.format(productionOrder.getProductionPlanning().getDate(), "yyyy-MM-dd"), "yyyy-MM-dd"),
                    productionOrder.getProductComposition().getProcessedProduct().getProductItemCode(),
                    MovementDetailType.E,
                    BigDecimalUtil.toBigDecimal(productionOrder.getProducedAmount()),
                    BigDecimalUtil.toBigDecimal(productionOrder.getUnitCost()),
                    BigDecimalUtil.toBigDecimal(productionOrder.getTotalCostProduction())
            );
            resultList.add(articleMovement);

        }
        /** Reprocesos **/
        for (BaseProduct baseProduct:baseProductList){
            for (SingleProduct singleProduct:baseProduct.getSingleProducts()){
                //data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), BigDecimalUtil.toBigDecimal(singleProduct.getAmount()), 2));
                ArticleMovement articleMovement = new ArticleMovement(
                        //singleProduct.getBaseProduct().getProductionPlanningBase().getDate(),
                        DateUtils.parse(DateUtils.format(singleProduct.getBaseProduct().getProductionPlanningBase().getDate(), "yyyy-MM-dd"), "yyyy-MM-dd"),
                        singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode(),
                        MovementDetailType.E,
                        BigDecimalUtil.toBigDecimal(singleProduct.getAmount()),
                        BigDecimalUtil.toBigDecimal(singleProduct.getUnitCost()),
                        BigDecimalUtil.toBigDecimal(singleProduct.getTotalCostProduction())
                );
                resultList.add(articleMovement);
            }
        }

        /** Ventas al contado **/
        List<ArticleOrder> cashSaleArticleOrderList = articleOrderService.findCashSaleDetailList(startDate, endDate);

        for (ArticleOrder articleOrder:cashSaleArticleOrderList){
            ArticleMovement articleMovement = new ArticleMovement(
                    articleOrder.getVentaDirecta().getFechaPedido(),
                    articleOrder.getCodArt(),
                    MovementDetailType.S,
                    BigDecimalUtil.toBigDecimal(articleOrder.getTotal()),
                    BigDecimalUtil.toBigDecimal(articleOrder.getCu()),
                    BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(articleOrder.getTotal()), BigDecimalUtil.toBigDecimal(articleOrder.getCu()), 6)
                    );
            resultList.add(articleMovement);
        }

        /** Pedidos **/
        List<ArticleOrder> orderCustomerDetailList = articleOrderService.findCustomerOrderDetailList(startDate, endDate);
        for (ArticleOrder articleOrder:orderCustomerDetailList){
            ArticleMovement articleMovement = new ArticleMovement(
                    articleOrder.getCustomerOrder().getOrderDate(),
                    articleOrder.getCodArt(),
                    MovementDetailType.S,
                    BigDecimalUtil.toBigDecimal(articleOrder.getTotal()),
                    BigDecimalUtil.toBigDecimal(articleOrder.getCu()),
                    BigDecimalUtil.multiply(BigDecimalUtil.toBigDecimal(articleOrder.getTotal()), BigDecimalUtil.toBigDecimal(articleOrder.getCu()), 6)
            );
            resultList.add(articleMovement);
        }
        /** ---------------------------------------------------------------------------------------------------- **/
        //----------------------------------------------------------------
        // Ordenamiento de la Lista por fecha
        Collections.sort(resultList, new Comparator<ArticleMovement>() {
            @Override
            public int compare(ArticleMovement o1, ArticleMovement o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        /*
        System.out.println("------- RESULT LIST  -------- ");
        for (ArticleMovement article: resultList){
            if (article.getProductCode().equals("118")){
                System.out.println(article.getDate() + "\t" + article.getMovementType() + "\t" + article.getUnitCost() + "\t\t" + article.getQuantity() + "\t" + article.getTotalCost());
            }
        }
        */

        //----------------------------------------------------------------
        /** -------------------------------------------------------------------------------------- **/
        /** Calculo del COSTO UNITARIO segun Costo Promedio **/
        /** Si hay irregularidad en el calculo, valores negativos, toma como costo unitario la ultima Entrada **/
        BigDecimal quantity  = BigDecimal.ZERO;
        BigDecimal unitCost  = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        boolean band = false;

        for (CollectionData data:beanCollection){

            for (ArticleMovement art:resultList){
                if (data.getCode().equals(art.getProductCode())){

                    if (art.getMovementType().equals(MovementDetailType.E)){
                        quantity  = BigDecimalUtil.sum(quantity, art.getQuantity(), 6);
                        totalCost = BigDecimalUtil.sum(totalCost, art.getTotalCost(), 6);
                        if (quantity.doubleValue() > 0) unitCost = BigDecimalUtil.divide(totalCost, quantity, 6);

                        if (quantity.doubleValue() < 0) band = true;

                    }

                    if (art.getMovementType().equals(MovementDetailType.S)){
                        quantity  = BigDecimalUtil.subtract(quantity, art.getQuantity(), 6);
                        totalCost = BigDecimalUtil.subtract(totalCost,
                                                            BigDecimalUtil.multiply(art.getQuantity(), unitCost, 6),
                                                            6);
                        if (quantity.doubleValue() < 0) band = true;
                    }
                    /** Si hay irregularidad en el calculo, valores negativos, toma como costo unitario la ultima Entrada **/
                    if (!band)
                        data.setUnitCost(unitCost);
                    else {
                        if (art.getMovementType().equals(MovementDetailType.E))
                            data.setUnitCost(art.getUnitCost());

                        /** Si no hay ultima entrada del producto, asigna el costo unitario de inv_inicio **/
                        if (BigDecimalUtil.compareTo(data.getUnitCost(), BigDecimal.ZERO) == 0) {
                            //System.out.println("====> COSTO UNIT ZERO: " + data.getCode() + " - " + data.getProductName());
                            data.setUnitCost(productInventoryService.findUnitCostbyCode(art.getProductCode(), DateUtils.getCurrentYear(startDate).toString()));
                        }
                    }
                }
            }

            quantity  = BigDecimal.ZERO;
            unitCost  = BigDecimal.ZERO;
            totalCost = BigDecimal.ZERO;
            band = false;
        }

        /** ------------------------------------------------------------------------------ **/

        for (CollectionData data:beanCollection){
            data.setBalance(BigDecimalUtil.sum(data.getInitialAmount(), data.getEntryAmount(), 6));
            data.setBalance(BigDecimalUtil.subtract(data.getBalance(), data.getOutputAmount(), 6));
            data.setValuedBalance(BigDecimalUtil.multiply(data.getBalance(), data.getUnitCost(), 6));
        }

        return beanCollection;
    }


    /** Calcula Saldos Iniciales **/
    public Collection<CollectionData> calculateInitialArticle(String warehouseCode, Date initDate){

        Calendar calendar = Calendar.getInstance();

        /** 1er dia del año **/
        Date firstDate = DateUtils.firstDayOfYear(DateUtils.getCurrentYear(initDate));
        /** Restando un dia a la fecha **/
        calendar.setTime(initDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        initDate = calendar.getTime();

        Collection<CollectionData> beanCollection = new ArrayList();
        List<InitialInventory> initialInventoryList = productInventoryService.findInitialInventory(warehouseCode, DateUtils.getCurrentYear(startDate).toString());

        List<MovementDetail> movementDetailList;
        if (warehouse.getWarehouseCode().equals("2"))
            movementDetailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouseCode, firstDate, initDate, null);
        else
            movementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouseCode, firstDate, initDate, null);

        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrders(firstDate, initDate);
        List<BaseProduct> baseProductList         = productionOrderService.findBaseProductByDate(firstDate, initDate);
        List<ProductionProduct> productionProductList = productionOrderService.findProductionByDate(firstDate, initDate);
        List<XProductionProduct> xproductionProductList = productionOrderService.findXProductionByDate(firstDate, initDate);
        List<CollectMaterial> collectMaterialList = collectMaterialService.findApprovedCollectMaterial(firstDate, initDate);
        List cashSaleDetailList = articleOrderService.findCashSaleDetailListGroupBy(firstDate, initDate);
        List orderDetailList    = articleOrderService.findCustomerOrderDetailListGroupBy(firstDate, initDate);

        /** Pre-calcular HashMaps para lookups O(1) **/
        // Acopio MP por codigo
        Map<String, BigDecimal> collectMaterialMap = new HashMap<String, BigDecimal>();
        for (CollectMaterial cm : collectMaterialList) {
            String code = cm.getMetaProduct().getProductItemCode();
            BigDecimal prev = collectMaterialMap.get(code);
            collectMaterialMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, cm.getBalanceWeight(), 2));
        }

        // Produccion por codigo
        Map<String, BigDecimal> productionMap = new HashMap<String, BigDecimal>();
        for (ProductionProduct product : productionProductList) {
            String code = product.getProductItemCode();
            BigDecimal prev = productionMap.get(code);
            productionMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, product.getQuantity(), 2));
        }

        // XProduccion por codigo
        Map<String, BigDecimal> xproductionMap = new HashMap<String, BigDecimal>();
        for (XProductionProduct product : xproductionProductList) {
            String code = product.getProductItemCode();
            BigDecimal prev = xproductionMap.get(code);
            xproductionMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, product.getQuantity(), 2));
        }

        // Materia prima en XProduccion (salida)
        Map<String, BigDecimal> rawMaterialMap = new HashMap<String, BigDecimal>();
        if (warehouse.getWarehouseType().equals(WarehouseType.RAW_MATERIAL)) {
            List rawMaterialProductionList = xproductionService.getSumRawMaterialInProduction(firstDate, initDate);
            for (int i = 0; i < rawMaterialProductionList.size(); i++) {
                Object[] row = (Object[]) rawMaterialProductionList.get(i);
                rawMaterialMap.put((String) row[0], (BigDecimal) row[1]);
            }
        }

        // Ordenes de produccion por codigo
        Map<String, BigDecimal> prodOrderEntryMap = new HashMap<String, BigDecimal>();
        Map<String, BigDecimal> prodOrderQtyMap = new HashMap<String, BigDecimal>();
        Map<String, BigDecimal> prodOrderCostMap = new HashMap<String, BigDecimal>();
        for (ProductionOrder po : productionOrderList) {
            String code = po.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode();
            BigDecimal prev = prodOrderEntryMap.get(code);
            prodOrderEntryMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(po.getProducedAmount()), 2));
            BigDecimal prevQty = prodOrderQtyMap.get(code);
            prodOrderQtyMap.put(code, BigDecimalUtil.sum(prevQty != null ? prevQty : BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(po.getProducedAmount()), 6));
            BigDecimal prevCost = prodOrderCostMap.get(code);
            prodOrderCostMap.put(code, BigDecimalUtil.sum(prevCost != null ? prevCost : BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(po.getTotalCostProduction()), 6));
        }

        // Reprocesos por codigo
        Map<String, BigDecimal> reprocessMap = new HashMap<String, BigDecimal>();
        for (BaseProduct baseProduct : baseProductList) {
            for (SingleProduct singleProduct : baseProduct.getSingleProducts()) {
                String code = singleProduct.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode();
                BigDecimal prev = reprocessMap.get(code);
                reprocessMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(singleProduct.getAmount()), 2));
            }
        }

        // Movimientos entrada/salida, cantidad y monto por codigo
        Map<String, BigDecimal> movEntryMap = new HashMap<String, BigDecimal>();
        Map<String, BigDecimal> movOutputMap = new HashMap<String, BigDecimal>();
        Map<String, BigDecimal> movEntryQtyMap = new HashMap<String, BigDecimal>();
        Map<String, BigDecimal> movEntryCostMap = new HashMap<String, BigDecimal>();
        for (MovementDetail detail : movementDetailList) {
            String code = detail.getProductItemCode();
            if (detail.getMovementType().equals(MovementDetailType.E)) {
                BigDecimal prev = movEntryMap.get(code);
                movEntryMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, detail.getQuantity(), 2));
                BigDecimal prevQty = movEntryQtyMap.get(code);
                movEntryQtyMap.put(code, BigDecimalUtil.sum(prevQty != null ? prevQty : BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(detail.getQuantity()), 6));
                BigDecimal prevCost = movEntryCostMap.get(code);
                movEntryCostMap.put(code, BigDecimalUtil.sum(prevCost != null ? prevCost : BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(detail.getAmount()), 6));
            }
            if (detail.getMovementType().equals(MovementDetailType.S)) {
                BigDecimal prev = movOutputMap.get(code);
                movOutputMap.put(code, BigDecimalUtil.sum(prev != null ? prev : BigDecimal.ZERO, detail.getQuantity(), 2));
            }
        }

        // Ventas al contado por codigo
        Map<String, BigDecimal> cashSaleMap = new HashMap<String, BigDecimal>();
        for (int i = 0; i < cashSaleDetailList.size(); i++) {
            Object[] row = (Object[]) cashSaleDetailList.get(i);
            cashSaleMap.put((String) row[0], BigDecimalUtil.toBigDecimal((Long) row[1]));
        }

        // Pedidos por codigo
        Map<String, BigDecimal> orderMap = new HashMap<String, BigDecimal>();
        for (int i = 0; i < orderDetailList.size(); i++) {
            Object[] row = (Object[]) orderDetailList.get(i);
            orderMap.put((String) row[0], BigDecimalUtil.toBigDecimal((Long) row[1]));
        }

        /** Iterar inventario inicial con lookups O(1) **/
        Set<String> processedCodes = new HashSet<String>();
        for (InitialInventory initialInventory : initialInventoryList) {

            String code = initialInventory.getProductItemCode();
            processedCodes.add(code);

            CollectionData data = new CollectionData(
                    initialInventory.getProductItem().getSubGroup().getName(),
                    code,
                    initialInventory.getProductItem().getName(),
                    initialInventory.getProductItem().getUsageMeasureCode(),
                    initialInventory.getQuantity(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    initialInventory.getUnitCost());

            // Entradas: acopio MP
            if (collectMaterialMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), collectMaterialMap.get(code), 2));

            // Entradas: produccion
            if (productionMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), productionMap.get(code), 2));

            // Entradas: xproduccion
            if (xproductionMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), xproductionMap.get(code), 2));

            // Entradas: ordenes de produccion
            if (prodOrderEntryMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), prodOrderEntryMap.get(code), 2));

            // Entradas: reprocesos
            if (reprocessMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), reprocessMap.get(code), 2));

            // Entradas: movimientos
            if (movEntryMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), movEntryMap.get(code), 2));

            // Unit cost por movimientos de entrada
            if (movEntryQtyMap.containsKey(code)) {
                BigDecimal qty = movEntryQtyMap.get(code);
                BigDecimal cost = movEntryCostMap.get(code);
                if (qty.doubleValue() > 0) data.setUnitCost(BigDecimalUtil.divide(cost, qty, 2));
            }

            // Salidas: movimientos
            if (movOutputMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), movOutputMap.get(code), 2));

            // Salidas: materia prima en XProduccion
            if (rawMaterialMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), rawMaterialMap.get(code), 2));

            // Salidas: ventas al contado
            if (cashSaleMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), cashSaleMap.get(code), 2));

            // Salidas: pedidos
            if (orderMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), orderMap.get(code), 2));

            // Unit cost por ordenes de produccion (sobreescribe si hay datos)
            if (prodOrderQtyMap.containsKey(code)) {
                BigDecimal qty = prodOrderQtyMap.get(code);
                BigDecimal cost = prodOrderCostMap.get(code);
                if (qty.doubleValue() > 0) data.setUnitCost(BigDecimalUtil.divide(cost, qty, 2));
            }

            beanCollection.add(data);
        }

        /** Articulos con movimientos que NO estan en inv_inicio **/
        List<ProductItem> allItems = productItemService.findByWarehouseCode(warehouseCode);
        for (ProductItem item : allItems) {
            String code = item.getProductItemCode();
            if (processedCodes.contains(code)) continue;

            // Verificar si tiene algun movimiento en el periodo
            boolean hasMovement = movEntryMap.containsKey(code) || movOutputMap.containsKey(code)
                    || collectMaterialMap.containsKey(code) || productionMap.containsKey(code)
                    || xproductionMap.containsKey(code) || prodOrderEntryMap.containsKey(code)
                    || reprocessMap.containsKey(code) || rawMaterialMap.containsKey(code)
                    || cashSaleMap.containsKey(code) || orderMap.containsKey(code);

            if (!hasMovement) continue;

            CollectionData data = new CollectionData(
                    item.getSubGroup() != null ? item.getSubGroup().getName() : "",
                    code,
                    item.getName(),
                    item.getUsageMeasureCode(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO);

            if (collectMaterialMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), collectMaterialMap.get(code), 2));
            if (productionMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), productionMap.get(code), 2));
            if (xproductionMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), xproductionMap.get(code), 2));
            if (prodOrderEntryMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), prodOrderEntryMap.get(code), 2));
            if (reprocessMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), reprocessMap.get(code), 2));
            if (movEntryMap.containsKey(code))
                data.setEntryAmount(BigDecimalUtil.sum(data.getEntryAmount(), movEntryMap.get(code), 2));
            if (movOutputMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), movOutputMap.get(code), 2));
            if (rawMaterialMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), rawMaterialMap.get(code), 2));
            if (cashSaleMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), cashSaleMap.get(code), 2));
            if (orderMap.containsKey(code))
                data.setOutputAmount(BigDecimalUtil.sum(data.getOutputAmount(), orderMap.get(code), 2));

            beanCollection.add(data);
        }

        for (CollectionData data : beanCollection) {
            data.setBalance(BigDecimalUtil.sum(data.getInitialAmount(), data.getEntryAmount(), 2));
            data.setBalance(BigDecimalUtil.subtract(data.getBalance(), data.getOutputAmount(), 2));
            data.setValuedBalance(BigDecimalUtil.multiply(data.getBalance(), data.getUnitCost(), 2));
        }

        return beanCollection;
    }


    /**
     * Preprocesa jrxml para compatibilidad entre iReport 5.6 y JasperReports 3.7:
     * - Elimina uuid (no soportado en JR 3.7)
     * - Agrega class a textFieldExpression (requerido en JR 3.7)
     */
    private String preprocessJrxml(String jrxmlContent) {
        jrxmlContent = jrxmlContent.replaceAll(" uuid=\"[^\"]*\"", "");
        // BigDecimal fields
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{initialAmount}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{initialAmount}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{entryAmount}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{entryAmount}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{outputAmount}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{outputAmount}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{balance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{balance}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{valuedBalance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{valuedBalance}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{initialBalance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{initialBalance}");
        // BigDecimal variables
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{totalEntry}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{totalEntry}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{totalOutput}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{totalOutput}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{totalBalance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{totalBalance}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{totalInitial}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{totalInitial}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{totalValuedBalance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{totalValuedBalance}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{groupInitial}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{groupInitial}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{groupEntry}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{groupEntry}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{groupOutput}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{groupOutput}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{groupBalance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{groupBalance}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{grandTotalEntry}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{grandTotalEntry}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{grandTotalOutput}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{grandTotalOutput}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$P{grandTotalBalance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$P{grandTotalBalance}");
        // Date fields
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$P{startDate}", "<textFieldExpression class=\"java.util.Date\"><![CDATA[$P{startDate}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$P{endDate}", "<textFieldExpression class=\"java.util.Date\"><![CDATA[$P{endDate}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{date}", "<textFieldExpression class=\"java.util.Date\"><![CDATA[$F{date}");
        // All remaining without class are String
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[", "<textFieldExpression class=\"java.lang.String\"><![CDATA[");
        return jrxmlContent;
    }

    public void exportarExcel(Collection<CollectionData> beanCollection, CompanyConfiguration companyConfiguration, String groupName) throws IOException {

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Inventario");

        // Estilos
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerStyle.setFont(headerFont);

        HSSFCellStyle numberStyle = workbook.createCellStyle();
        HSSFDataFormat numFormat = workbook.createDataFormat();
        numberStyle.setDataFormat(numFormat.getFormat("#,##0.00"));

        // Encabezado
        int rowNum = 0;
        HSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getCompanyName());
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("REPORTE GENERAL DE INVENTARIO");
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Almacen:");
        row.createCell(1).setCellValue(warehouse.getFullName() + groupName);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Periodo:");
        row.createCell(1).setCellValue(sdf.format(startDate) + " - " + sdf.format(endDate));

        rowNum++; // fila vacia

        // Cabecera de tabla
        row = sheet.createRow(rowNum++);
        String[] headers = {"Codigo", "Articulo", "Unidad", "Inv. Inicial", "Entradas", "Salidas", "Saldo"};
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        BigDecimal totalInicial = BigDecimal.ZERO;
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSalidas = BigDecimal.ZERO;
        BigDecimal totalSaldo = BigDecimal.ZERO;

        for (CollectionData data : beanCollection) {
            row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(data.getCode());
            row.createCell(1).setCellValue(data.getProductName());
            row.createCell(2).setCellValue(data.getUnit());

            HSSFCell inicialCell = row.createCell(3);
            inicialCell.setCellValue(data.getInitialAmount() != null ? data.getInitialAmount().doubleValue() : 0);
            inicialCell.setCellStyle(numberStyle);

            HSSFCell entryCell = row.createCell(4);
            entryCell.setCellValue(data.getEntryAmount() != null ? data.getEntryAmount().doubleValue() : 0);
            entryCell.setCellStyle(numberStyle);

            HSSFCell outputCell = row.createCell(5);
            outputCell.setCellValue(data.getOutputAmount() != null ? data.getOutputAmount().doubleValue() : 0);
            outputCell.setCellStyle(numberStyle);

            HSSFCell balanceCell = row.createCell(6);
            balanceCell.setCellValue(data.getBalance() != null ? data.getBalance().doubleValue() : 0);
            balanceCell.setCellStyle(numberStyle);

            totalInicial = BigDecimalUtil.sum(totalInicial, data.getInitialAmount() != null ? data.getInitialAmount() : BigDecimal.ZERO, 2);
            totalEntradas = BigDecimalUtil.sum(totalEntradas, data.getEntryAmount() != null ? data.getEntryAmount() : BigDecimal.ZERO, 2);
            totalSalidas = BigDecimalUtil.sum(totalSalidas, data.getOutputAmount() != null ? data.getOutputAmount() : BigDecimal.ZERO, 2);
            totalSaldo = BigDecimalUtil.sum(totalSaldo, data.getBalance() != null ? data.getBalance() : BigDecimal.ZERO, 2);
        }

        // Fila de totales
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("TOTALES");
        row.getCell(0).setCellStyle(headerStyle);

        HSSFCell tInicialCell = row.createCell(3);
        tInicialCell.setCellValue(totalInicial.doubleValue());
        tInicialCell.setCellStyle(numberStyle);

        HSSFCell tEntryCell = row.createCell(4);
        tEntryCell.setCellValue(totalEntradas.doubleValue());
        tEntryCell.setCellStyle(numberStyle);

        HSSFCell tOutputCell = row.createCell(5);
        tOutputCell.setCellValue(totalSalidas.doubleValue());
        tOutputCell.setCellStyle(numberStyle);

        HSSFCell tBalanceCell = row.createCell(6);
        tBalanceCell.setCellValue(totalSaldo.doubleValue());
        tBalanceCell.setCellStyle(numberStyle);

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Enviar respuesta
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-disposition", "attachment; filename=ReporteGeneralInv.xls");
        markReportReady();
        ServletOutputStream stream = response.getOutputStream();
        workbook.write(stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void exportarExcelAgrupado(Collection<CollectionData> beanCollection, CompanyConfiguration companyConfiguration, String period, String filterName) throws IOException {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Inventario Agrupado");

        // Estilos
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerStyle.setFont(headerFont);

        HSSFCellStyle groupStyle = workbook.createCellStyle();
        HSSFFont groupFont = workbook.createFont();
        groupFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        groupStyle.setFont(groupFont);

        HSSFCellStyle numberStyle = workbook.createCellStyle();
        HSSFDataFormat numFormat = workbook.createDataFormat();
        numberStyle.setDataFormat(numFormat.getFormat("#,##0.00"));

        // Encabezado
        int rowNum = 0;
        HSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getCompanyName());
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getLocationName());

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getSystemName());

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("REPORTE GENERAL DE INVENTARIO");
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(period);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("ALMACEN:");
        row.getCell(0).setCellStyle(headerStyle);
        row.createCell(1).setCellValue(filterName);
        row.getCell(1).setCellStyle(headerStyle);

        rowNum++; // fila vacia

        // Datos agrupados por subgroupName
        String currentGroup = null;
        BigDecimal totalInicial = BigDecimal.ZERO;
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSalidas = BigDecimal.ZERO;
        BigDecimal totalSaldo = BigDecimal.ZERO;

        BigDecimal grpInicial = BigDecimal.ZERO;
        BigDecimal grpEntradas = BigDecimal.ZERO;
        BigDecimal grpSalidas = BigDecimal.ZERO;
        BigDecimal grpSaldo = BigDecimal.ZERO;

        HSSFCellStyle numberBoldStyle = workbook.createCellStyle();
        numberBoldStyle.setDataFormat(numFormat.getFormat("#,##0.00"));
        numberBoldStyle.setFont(headerFont);

        for (CollectionData data : beanCollection) {

            // Cabecera de grupo
            String subgroup = data.getSubgroupName() != null ? data.getSubgroupName() : "";
            if (!subgroup.equals(currentGroup)) {
                // Subtotal del grupo anterior
                if (currentGroup != null) {
                    row = sheet.createRow(rowNum++);
                    HSSFCell sgInicialCell = row.createCell(3);
                    sgInicialCell.setCellValue(grpInicial.doubleValue());
                    sgInicialCell.setCellStyle(numberBoldStyle);
                    HSSFCell sgEntryCell = row.createCell(4);
                    sgEntryCell.setCellValue(grpEntradas.doubleValue());
                    sgEntryCell.setCellStyle(numberBoldStyle);
                    HSSFCell sgOutputCell = row.createCell(5);
                    sgOutputCell.setCellValue(grpSalidas.doubleValue());
                    sgOutputCell.setCellStyle(numberBoldStyle);
                    HSSFCell sgBalanceCell = row.createCell(6);
                    sgBalanceCell.setCellValue(grpSaldo.doubleValue());
                    sgBalanceCell.setCellStyle(numberBoldStyle);
                    rowNum++;
                }
                grpInicial = BigDecimal.ZERO;
                grpEntradas = BigDecimal.ZERO;
                grpSalidas = BigDecimal.ZERO;
                grpSaldo = BigDecimal.ZERO;

                currentGroup = subgroup;
                row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(currentGroup);
                row.getCell(0).setCellStyle(groupStyle);

                // Cabecera de columnas
                row = sheet.createRow(rowNum++);
                String[] headers = {"Codigo", "Articulo", "Unidad", "Inv. Inicial", "Entradas", "Salidas", "Saldo"};
                for (int i = 0; i < headers.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
            }

            row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(data.getCode());
            row.createCell(1).setCellValue(data.getProductName());
            row.createCell(2).setCellValue(data.getUnit());

            HSSFCell inicialCell = row.createCell(3);
            inicialCell.setCellValue(data.getInitialAmount() != null ? data.getInitialAmount().doubleValue() : 0);
            inicialCell.setCellStyle(numberStyle);

            HSSFCell entryCell = row.createCell(4);
            entryCell.setCellValue(data.getEntryAmount() != null ? data.getEntryAmount().doubleValue() : 0);
            entryCell.setCellStyle(numberStyle);

            HSSFCell outputCell = row.createCell(5);
            outputCell.setCellValue(data.getOutputAmount() != null ? data.getOutputAmount().doubleValue() : 0);
            outputCell.setCellStyle(numberStyle);

            HSSFCell balanceCell = row.createCell(6);
            balanceCell.setCellValue(data.getBalance() != null ? data.getBalance().doubleValue() : 0);
            balanceCell.setCellStyle(numberStyle);

            BigDecimal ini = data.getInitialAmount() != null ? data.getInitialAmount() : BigDecimal.ZERO;
            BigDecimal ent = data.getEntryAmount() != null ? data.getEntryAmount() : BigDecimal.ZERO;
            BigDecimal sal = data.getOutputAmount() != null ? data.getOutputAmount() : BigDecimal.ZERO;
            BigDecimal sld = data.getBalance() != null ? data.getBalance() : BigDecimal.ZERO;

            grpInicial = BigDecimalUtil.sum(grpInicial, ini, 2);
            grpEntradas = BigDecimalUtil.sum(grpEntradas, ent, 2);
            grpSalidas = BigDecimalUtil.sum(grpSalidas, sal, 2);
            grpSaldo = BigDecimalUtil.sum(grpSaldo, sld, 2);

            totalInicial = BigDecimalUtil.sum(totalInicial, ini, 2);
            totalEntradas = BigDecimalUtil.sum(totalEntradas, ent, 2);
            totalSalidas = BigDecimalUtil.sum(totalSalidas, sal, 2);
            totalSaldo = BigDecimalUtil.sum(totalSaldo, sld, 2);
        }

        // Subtotal del ultimo grupo
        if (currentGroup != null) {
            row = sheet.createRow(rowNum++);
            HSSFCell sgInicialCell = row.createCell(3);
            sgInicialCell.setCellValue(grpInicial.doubleValue());
            sgInicialCell.setCellStyle(numberBoldStyle);
            HSSFCell sgEntryCell = row.createCell(4);
            sgEntryCell.setCellValue(grpEntradas.doubleValue());
            sgEntryCell.setCellStyle(numberBoldStyle);
            HSSFCell sgOutputCell = row.createCell(5);
            sgOutputCell.setCellValue(grpSalidas.doubleValue());
            sgOutputCell.setCellStyle(numberBoldStyle);
            HSSFCell sgBalanceCell = row.createCell(6);
            sgBalanceCell.setCellValue(grpSaldo.doubleValue());
            sgBalanceCell.setCellStyle(numberBoldStyle);
        }

        // Fila de totales generales
        rowNum++;
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("TOTALES");
        row.getCell(0).setCellStyle(headerStyle);

        HSSFCell tInicialCell = row.createCell(3);
        tInicialCell.setCellValue(totalInicial.doubleValue());
        tInicialCell.setCellStyle(numberBoldStyle);

        HSSFCell tEntryCell = row.createCell(4);
        tEntryCell.setCellValue(totalEntradas.doubleValue());
        tEntryCell.setCellStyle(numberBoldStyle);

        HSSFCell tOutputCell = row.createCell(5);
        tOutputCell.setCellValue(totalSalidas.doubleValue());
        tOutputCell.setCellStyle(numberBoldStyle);

        HSSFCell tBalanceCell = row.createCell(6);
        tBalanceCell.setCellValue(totalSaldo.doubleValue());
        tBalanceCell.setCellStyle(numberBoldStyle);

        // Autoajustar columnas
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }

        // Enviar respuesta
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-disposition", "attachment; filename=ReporteInventarioAgrupado.xls");
        markReportReady();
        ServletOutputStream stream = response.getOutputStream();
        workbook.write(stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void exportarPDF(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=ReporteGeneralInv.pdf");
        markReportReady();
        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void cleanProductItem() {
        this.productItem = null;
    }

    public void assignProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void cleanWarehouseField() {
        warehouse = null;
    }

    public Boolean getArticlesWithMovement() {
        return articlesWithMovement;
    }

    public void setArticlesWithMovement(Boolean articlesWithMovement) {
        this.articlesWithMovement = articlesWithMovement;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public SubGroup getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(SubGroup subGroup) {
        this.subGroup = subGroup;
    }

    /**
     *
     */
    public class InitialArticle{

        private String code;
        private BigDecimal quantity;

        public InitialArticle(String code, BigDecimal quantity){
            this.code = code;
            this.quantity = quantity;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    /**
     * CollectionData class for Product Inventory Report
     */
    public class CollectionData{

        private String subgroupName;
        private String code;
        private String productName;
        private String unit;
        private BigDecimal initialAmount;
        private BigDecimal entryAmount;
        private BigDecimal outputAmount;
        private BigDecimal balance;
        private BigDecimal unitCost;
        private BigDecimal valuedBalance;

        public CollectionData(String code, String productName, String unit, BigDecimal initialAmount,  BigDecimal entryAmount, BigDecimal outputAmount, BigDecimal balance, BigDecimal unitCost){

            this.setCode(code);
            this.setProductName(productName);
            this.unit = unit;
            this.setInitialAmount(initialAmount);
            this.setEntryAmount(entryAmount);
            this.setOutputAmount(outputAmount);
            this.setBalance(balance);
            this.unitCost = unitCost;
            this.valuedBalance = BigDecimal.ZERO;
        }

        public CollectionData(String subgroupName, String code, String productName, String unit, BigDecimal initialAmount,  BigDecimal entryAmount, BigDecimal outputAmount, BigDecimal balance, BigDecimal unitCost){

            this.setSubgroupName(subgroupName);
            this.setCode(code);
            this.setProductName(productName);
            this.unit = unit;
            this.setInitialAmount(initialAmount);
            this.setEntryAmount(entryAmount);
            this.setOutputAmount(outputAmount);
            this.setBalance(balance);
            this.unitCost = unitCost;
            this.valuedBalance = BigDecimal.ZERO;
        }


        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getInitialAmount() {
            return initialAmount;
        }

        public void setInitialAmount(BigDecimal initialAmount) {
            this.initialAmount = initialAmount;
        }

        public BigDecimal getEntryAmount() {
            return entryAmount;
        }

        public void setEntryAmount(BigDecimal entryAmount) {
            this.entryAmount = entryAmount;
        }

        public BigDecimal getOutputAmount() {
            return outputAmount;
        }

        public void setOutputAmount(BigDecimal outputAmount) {
            this.outputAmount = outputAmount;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getUnitCost() {
            return unitCost;
        }

        public void setUnitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
        }

        public BigDecimal getValuedBalance() {
            return valuedBalance;
        }

        public void setValuedBalance(BigDecimal valuedBalance) {
            this.valuedBalance = valuedBalance;
        }

        public String getSubgroupName() {
            return subgroupName;
        }

        public void setSubgroupName(String subgroupName) {
            this.subgroupName = subgroupName;
        }
    }


    public class ArticleMovement{

        private Date date;
        private String productCode;
        private MovementDetailType movementType;
        private BigDecimal quantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;

        ArticleMovement(Date date, String productCode, MovementDetailType movementType, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost){
            this.setDate(date);
            this.setProductCode(productCode);
            this.setMovementType(movementType);
            this.setQuantity(quantity);
            this.setUnitCost(unitCost);
            this.setTotalCost(totalCost);
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitCost() {
            return unitCost;
        }

        public void setUnitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
        }

        public BigDecimal getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
        }

        public MovementDetailType getMovementType() {
            return movementType;
        }

        public void setMovementType(MovementDetailType movementType) {
            this.movementType = movementType;
        }
    }

    public void cleanGroupField() {
        setGroup(null);
    }

    public void cleanSubGroupField() {
        setSubGroup(null);
    }
}