package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.customers.ArticleOrder;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.*;
import com.encens.khipus.model.xproduction.XProductionProduct;
import com.encens.khipus.model.xproduction.XSupply;
import com.encens.khipus.service.customers.ArticleOrderService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.CollectMaterialService;
import com.encens.khipus.service.production.ProductionOrderService;
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

@Name("extendedInventoryReportAction")
@Scope(ScopeType.PAGE)
public class ExtendedInventoryReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private Warehouse warehouse;
    private Group group;
    private SubGroup subGroup;
    private ReportFormat reportFormat;

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

    @Create
    public void init() {
        restrictions = new String[]{};
    }

    protected String getEjbql() {
        return "";
    }

    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "CompanyConfiguration.notFound");
        }

        List<ArticleReportData> reportData = calculateExtendedData();

        // Filtrar por grupo si se selecciono
        if (group != null) {
            List<ProductItem> groupItems = productItemService.findByGroupCode(group.getGroupCode());
            Set<String> groupCodes = new HashSet<String>();
            for (ProductItem item : groupItems) {
                groupCodes.add(item.getProductItemCode());
            }
            List<ArticleReportData> filtered = new ArrayList<ArticleReportData>();
            for (ArticleReportData ard : reportData) {
                if (groupCodes.contains(ard.getArticleCode())) {
                    filtered.add(ard);
                }
            }
            reportData = filtered;
        }

        // Filtrar por subgrupo si se selecciono
        if (subGroup != null) {
            List<ProductItem> subGroupItems = productItemService.findBySubGroupCode(subGroup.getGroupCode(), subGroup.getSubGroupCode());
            Set<String> subGroupCodes = new HashSet<String>();
            for (ProductItem item : subGroupItems) {
                subGroupCodes.add(item.getProductItemCode());
            }
            List<ArticleReportData> filtered = new ArrayList<ArticleReportData>();
            for (ArticleReportData ard : reportData) {
                if (subGroupCodes.contains(ard.getArticleCode())) {
                    filtered.add(ard);
                }
            }
            reportData = filtered;
        }

        try {
            if (reportFormat != null && (reportFormat.name().equals("XLS") || reportFormat.name().equals("XLSX"))) {
                exportarExcel(reportData, companyConfiguration);
            } else {
                exportarPDF(reportData, companyConfiguration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calcula datos extendidos: para cada articulo del almacen,
     * genera la lista de movimientos individuales con fecha, entrada, salida, saldo acumulado y glosa.
     */
    public List<ArticleReportData> calculateExtendedData() {

        List<ArticleReportData> result = new ArrayList<ArticleReportData>();

        /** 1.- Articulos del almacen con inventario de periodo **/
        List<InventoryPeriod> inventoryPeriodList = productInventoryService.findInitialInventoryAll(
                warehouse.getWarehouseCode(), DateUtils.getCurrentYear(startDate).toString());

        /** 2.- Calcular saldo inicial por articulo (desde inicio gestion hasta startDate-1) **/
        Map<String, BigDecimal> initialBalanceMap = calculateInitialBalanceMap(warehouse.getWarehouseCode(), startDate);

        /** 3.- Cargar todos los movimientos del periodo en listas **/
        List<MovementDetail> movementDetailList;
        if (warehouse.getWarehouseCode().equals(WarehouseType.DAIRY))
            movementDetailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouse.getWarehouseCode(), startDate, endDate, null);
        else
            movementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouse.getWarehouseCode(), startDate, endDate, null);

        List<ProductionProduct> productionProductList = productionOrderService.findProductionByDate(startDate, endDate);
        List<XProductionProduct> xproductionProductList = productionOrderService.findXProductionByDate(startDate, endDate);
        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrders(startDate, endDate);
        List<BaseProduct> baseProductList = productionOrderService.findBaseProductByDate(startDate, endDate);
        List<CollectMaterial> collectMaterialList = collectMaterialService.findApprovedCollectMaterial(startDate, endDate);
        List<ArticleOrder> cashSaleDetailList = articleOrderService.findCashSaleDetailList(startDate, endDate);
        List<ArticleOrder> orderDetailList = articleOrderService.findCustomerOrderDetailList(startDate, endDate);

        // Materia prima en XProduccion
        List<XSupply> supplyList = new ArrayList<XSupply>();
        if (warehouse.getWarehouseType().equals(WarehouseType.RAW_MATERIAL)) {
            supplyList = xproductionService.getAllRawMaterialInProduction(startDate, endDate);
        }

        /** 4.- Indexar movimientos por productItemCode usando HashMap<String, List<MovementRow>> **/
        Map<String, List<MovementRow>> movementsByArticle = new HashMap<String, List<MovementRow>>();

        // Acopio MP
        for (CollectMaterial cm : collectMaterialList) {
            String code = cm.getMetaProduct().getProductItemCode();
            addMovement(movementsByArticle, code, new MovementRow(
                    cm.getDate(), cm.getBalanceWeight(), BigDecimal.ZERO,
                    "ACOPIO DE MATERIA PRIMA EN FECHA " + DateUtils.format(cm.getDate(), "dd/MM/yyyy")));
        }

        // Produccion
        for (ProductionProduct product : productionProductList) {
            addMovement(movementsByArticle, product.getProductItemCode(), new MovementRow(
                    product.getProductionPlan().getDate(), product.getQuantity(), BigDecimal.ZERO,
                    "ORDEN DE PRODUCCION FECHA " + DateUtils.format(product.getProductionPlan().getDate(), "dd/MM/yyyy")));
        }

        // XProduccion
        for (XProductionProduct product : xproductionProductList) {
            addMovement(movementsByArticle, product.getProductItemCode(), new MovementRow(
                    product.getProductionPlan().getDate(), product.getQuantity(), BigDecimal.ZERO,
                    "ORDEN DE PRODUCCION FECHA " + DateUtils.format(product.getProductionPlan().getDate(), "dd/MM/yyyy")));
        }

        // Ordenes de produccion
        for (ProductionOrder po : productionOrderList) {
            String code = po.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode();
            addMovement(movementsByArticle, code, new MovementRow(
                    po.getProductionPlanning().getDate(),
                    BigDecimalUtil.toBigDecimal(po.getProducedAmount()), BigDecimal.ZERO,
                    "ORDEN DE PRODUCCION NRO. " + po.getCode()));
        }

        // Reprocesos
        for (BaseProduct baseProduct : baseProductList) {
            for (SingleProduct sp : baseProduct.getSingleProducts()) {
                String code = sp.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode();
                addMovement(movementsByArticle, code, new MovementRow(
                        baseProduct.getProductionPlanningBase().getDate(),
                        BigDecimalUtil.toBigDecimal(sp.getAmount()), BigDecimal.ZERO,
                        "REPROCESO DE PRODUCCION NRO. " + baseProduct.getCode()));
            }
        }

        // Vales de movimiento
        for (MovementDetail md : movementDetailList) {
            BigDecimal entry = md.getMovementType().equals(MovementDetailType.E) ? md.getQuantity() : BigDecimal.ZERO;
            BigDecimal output = md.getMovementType().equals(MovementDetailType.S) ? md.getQuantity() : BigDecimal.ZERO;
            addMovement(movementsByArticle, md.getProductItemCode(), new MovementRow(
                    md.getInventoryMovement().getWarehouseVoucher().getDate(),
                    entry, output,
                    md.getInventoryMovement().getDescription()));
        }

        // XProduccion - materia prima (salida)
        for (XSupply supply : supplyList) {
            String code = supply.getProductItemCode();
            String dateString = DateUtils.format(supply.getProduction().getProductionPlan().getDate(), "dd/MM/yyyy");
            addMovement(movementsByArticle, code, new MovementRow(
                    supply.getProduction().getProductionPlan().getDate(),
                    BigDecimal.ZERO, supply.getQuantity(),
                    "Materia prima en produccion " + supply.getProduction().getCode() + " " + dateString));
        }

        // Ventas al contado
        for (ArticleOrder ao : cashSaleDetailList) {
            String code = ao.getCodArt();
            String invoiceLabel = "";
            if (ao.getVentaDirecta().getMovement() != null) {
                invoiceLabel = "F-" + ao.getVentaDirecta().getMovement().getNumber().toString() + " ";
            }
            addMovement(movementsByArticle, code, new MovementRow(
                    ao.getVentaDirecta().getFechaPedido(),
                    BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(ao.getTotal()),
                    invoiceLabel + "Venta al contado " + ao.getVentaDirecta().getCodigo() + " " + ao.getVentaDirecta().getCliente().getFullName()));
        }

        // Pedidos
        for (ArticleOrder ao : orderDetailList) {
            String code = ao.getCodArt();
            String invoiceLabel = "";
            if (ao.getCustomerOrder().getMovement() != null) {
                invoiceLabel = "F-" + ao.getCustomerOrder().getMovement().getNumber().toString() + " ";
            }
            String typeLabel = "Venta a credito ";
            if (ao.getCustomerOrder().getSaleType() != null) {
                if (ao.getCustomerOrder().getSaleType().equals(SaleTypeEnum.CASH))
                    typeLabel = "Venta al contado ";
            }
            addMovement(movementsByArticle, code, new MovementRow(
                    ao.getCustomerOrder().getOrderDate(),
                    BigDecimal.ZERO, BigDecimalUtil.toBigDecimal(ao.getTotal()),
                    invoiceLabel + typeLabel + ao.getCustomerOrder().getCode() + " " + ao.getCustomerOrder().getClient().getFullName()));
        }

        /** 5.- Armar resultado: por cada articulo, ordenar movimientos y calcular saldo acumulado **/
        for (InventoryPeriod ip : inventoryPeriodList) {
            String code = ip.getProductItemCode();
            BigDecimal initialBalance = initialBalanceMap.containsKey(code) ? initialBalanceMap.get(code) : BigDecimal.ZERO;

            List<MovementRow> movements = movementsByArticle.get(code);

            // Incluir articulos sin movimiento pero con saldo inicial
            if ((movements == null || movements.isEmpty()) && initialBalance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            ArticleReportData articleData = new ArticleReportData(
                    code,
                    ip.getProductItem().getName(),
                    ip.getProductItem().getUsageMeasureCode(),
                    ip.getProductItem().getSubGroup() != null ? ip.getProductItem().getSubGroup().getName() : "",
                    initialBalance
            );

            if (movements != null && !movements.isEmpty()) {
                Collections.sort(movements, new Comparator<MovementRow>() {
                    @Override
                    public int compare(MovementRow o1, MovementRow o2) {
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });

                BigDecimal runningBalance = initialBalance;
                for (MovementRow mov : movements) {
                    runningBalance = BigDecimalUtil.sum(runningBalance, mov.getEntryAmount(), 6);
                    runningBalance = BigDecimalUtil.subtract(runningBalance, mov.getOutputAmount(), 6);
                    mov.setBalance(runningBalance);
                }
                articleData.setMovements(movements);
            }

            result.add(articleData);
        }

        // Ordenar por subgrupo y nombre
        Collections.sort(result, new Comparator<ArticleReportData>() {
            @Override
            public int compare(ArticleReportData o1, ArticleReportData o2) {
                int cmp = o1.getSubgroupName().compareTo(o2.getSubgroupName());
                if (cmp == 0) return o1.getArticleName().compareTo(o2.getArticleName());
                return cmp;
            }
        });

        return result;
    }

    /**
     * Calcula saldo inicial por articulo desde inicio de gestion hasta (startDate - 1 dia).
     * Reutiliza la logica optimizada con HashMaps.
     */
    private Map<String, BigDecimal> calculateInitialBalanceMap(String warehouseCode, Date initDate) {

        Calendar calendar = Calendar.getInstance();
        Date firstDate = DateUtils.firstDayOfYear(DateUtils.getCurrentYear(initDate));
        calendar.setTime(initDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date endInitDate = calendar.getTime();

        Map<String, BigDecimal> balanceMap = new HashMap<String, BigDecimal>();

        // Inventario inicio gestion
        List<InitialInventory> initialInventoryList = productInventoryService.findInitialInventory(warehouseCode, DateUtils.getCurrentYear(startDate).toString());
        for (InitialInventory inv : initialInventoryList) {
            balanceMap.put(inv.getProductItemCode(), inv.getQuantity());
        }

        // Vales de movimiento
        List<MovementDetail> movementDetailList;
        if (warehouse.getWarehouseCode().equals("2"))
            movementDetailList = movementDetailService.findListMovementByWarehouseAndTypeNull(warehouseCode, firstDate, endInitDate, null);
        else
            movementDetailList = movementDetailService.findListMovementByWarehouseAndType(warehouseCode, firstDate, endInitDate, null);

        for (MovementDetail detail : movementDetailList) {
            String code = detail.getProductItemCode();
            BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
            if (detail.getMovementType().equals(MovementDetailType.E))
                current = BigDecimalUtil.sum(current, detail.getQuantity(), 2);
            if (detail.getMovementType().equals(MovementDetailType.S))
                current = BigDecimalUtil.subtract(current, detail.getQuantity(), 2);
            balanceMap.put(code, current);
        }

        // Ordenes de produccion
        List<ProductionOrder> productionOrderList = productionOrderService.findProductionOrders(firstDate, endInitDate);
        for (ProductionOrder po : productionOrderList) {
            String code = po.getProductComposition().getProcessedProduct().getProductItem().getProductItemCode();
            BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
            balanceMap.put(code, BigDecimalUtil.sum(current, BigDecimalUtil.toBigDecimal(po.getProducedAmount()), 2));
        }

        // Reprocesos
        List<BaseProduct> baseProductList = productionOrderService.findBaseProductByDate(firstDate, endInitDate);
        for (BaseProduct bp : baseProductList) {
            for (SingleProduct sp : bp.getSingleProducts()) {
                String code = sp.getProductProcessingSingle().getMetaProduct().getProductItem().getProductItemCode();
                BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
                balanceMap.put(code, BigDecimalUtil.sum(current, BigDecimalUtil.toBigDecimal(sp.getAmount()), 2));
            }
        }

        // Produccion
        List<ProductionProduct> productionProductList = productionOrderService.findProductionByDate(firstDate, endInitDate);
        for (ProductionProduct product : productionProductList) {
            String code = product.getProductItemCode();
            BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
            balanceMap.put(code, BigDecimalUtil.sum(current, product.getQuantity(), 2));
        }

        // XProduccion (entrada)
        List<XProductionProduct> xproductionProductList = productionOrderService.findXProductionByDate(firstDate, endInitDate);
        for (XProductionProduct product : xproductionProductList) {
            String code = product.getProductItemCode();
            BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
            balanceMap.put(code, BigDecimalUtil.sum(current, product.getQuantity(), 2));
        }

        // Acopio MP (entrada)
        List<CollectMaterial> collectMaterialList = collectMaterialService.findApprovedCollectMaterial(firstDate, endInitDate);
        for (CollectMaterial cm : collectMaterialList) {
            String code = cm.getMetaProduct().getProductItemCode();
            BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
            balanceMap.put(code, BigDecimalUtil.sum(current, cm.getBalanceWeight(), 2));
        }

        // Materia prima en XProduccion (salida)
        if (warehouse.getWarehouseType().equals(WarehouseType.RAW_MATERIAL)) {
            List rawMaterialList = xproductionService.getSumRawMaterialInProduction(firstDate, endInitDate);
            for (int i = 0; i < rawMaterialList.size(); i++) {
                Object[] row = (Object[]) rawMaterialList.get(i);
                String code = (String) row[0];
                BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
                balanceMap.put(code, BigDecimalUtil.subtract(current, (BigDecimal) row[1], 2));
            }
        }

        // Ventas al contado
        List cashSaleDetailList = articleOrderService.findCashSaleDetailListGroupBy(firstDate, endInitDate);
        for (int i = 0; i < cashSaleDetailList.size(); i++) {
            Object[] row = (Object[]) cashSaleDetailList.get(i);
            String code = (String) row[0];
            BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
            balanceMap.put(code, BigDecimalUtil.subtract(current, BigDecimalUtil.toBigDecimal((Long) row[1]), 2));
        }

        // Pedidos
        List orderDetailList = articleOrderService.findCustomerOrderDetailListGroupBy(firstDate, endInitDate);
        for (int i = 0; i < orderDetailList.size(); i++) {
            Object[] row = (Object[]) orderDetailList.get(i);
            String code = (String) row[0];
            BigDecimal current = balanceMap.containsKey(code) ? balanceMap.get(code) : BigDecimal.ZERO;
            balanceMap.put(code, BigDecimalUtil.subtract(current, BigDecimalUtil.toBigDecimal((Long) row[1]), 2));
        }

        return balanceMap;
    }

    private void addMovement(Map<String, List<MovementRow>> map, String code, MovementRow row) {
        List<MovementRow> list = map.get(code);
        if (list == null) {
            list = new ArrayList<MovementRow>();
            map.put(code, list);
        }
        list.add(row);
    }

    /** Exportar a PDF usando JasperReports **/
    public void exportarPDF(List<ArticleReportData> reportData, CompanyConfiguration companyConfiguration) throws IOException, JRException {

        // Aplanar datos para JasperReports
        List<FlatReportRow> flatRows = flattenData(reportData);

        String filterName = "";
        if (group != null) filterName = " - " + group.getName();
        if (subGroup != null) filterName = " - " + subGroup.getName();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String period = "Del " + sdf.format(startDate) + " al " + sdf.format(endDate);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("reportTitle", "REPORTE DE INVENTARIO EXTENDIDO");
        parameters.put("companyName", companyConfiguration.getCompanyName());
        parameters.put("systemName", companyConfiguration.getSystemName());
        parameters.put("locationName", companyConfiguration.getLocationName());
        parameters.put("period", period);
        parameters.put("warehouse", warehouse.getFullName() + filterName);

        // Calcular total de saldos finales
        BigDecimal grandTotalBalance = BigDecimal.ZERO;
        for (ArticleReportData article : reportData) {
            if (article.getMovements() != null && !article.getMovements().isEmpty()) {
                BigDecimal lastBalance = article.getMovements().get(article.getMovements().size() - 1).getBalance();
                grandTotalBalance = BigDecimalUtil.sum(grandTotalBalance, lastBalance, 2);
            } else {
                grandTotalBalance = BigDecimalUtil.sum(grandTotalBalance, article.getInitialBalance(), 2);
            }
        }
        parameters.put("grandTotalBalance", grandTotalBalance);

        File jrxmlFile = new File(JSFUtil.getRealPath("/warehouse/reports/extendedInventoryReport.jrxml"));
        String jrxmlContent = new String(java.nio.file.Files.readAllBytes(jrxmlFile.toPath()), "UTF-8");
        // Compatibilidad: iReport 5.6 agrega uuid (no soportado en JR 3.7) y quita class de textFieldExpression (requerido en JR 3.7)
        jrxmlContent = jrxmlContent.replaceAll(" uuid=\"[^\"]*\"", "");
        // BigDecimal fields/variables
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{initialBalance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{initialBalance}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{entryAmount}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{entryAmount}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{outputAmount}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{outputAmount}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{balance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$F{balance}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{totalEntry}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{totalEntry}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{totalOutput}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{totalOutput}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{grandTotalEntry}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{grandTotalEntry}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$V{grandTotalOutput}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$V{grandTotalOutput}");
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$P{grandTotalBalance}", "<textFieldExpression class=\"java.math.BigDecimal\"><![CDATA[$P{grandTotalBalance}");
        // Date field
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[$F{date}", "<textFieldExpression class=\"java.util.Date\"><![CDATA[$F{date}");
        // All remaining textFieldExpression without class are String
        jrxmlContent = jrxmlContent.replace("<textFieldExpression><![CDATA[", "<textFieldExpression class=\"java.lang.String\"><![CDATA[");
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(jrxmlContent.getBytes("UTF-8"));
        net.sf.jasperreports.engine.JasperReport jasperReport = net.sf.jasperreports.engine.JasperCompileManager.compileReport(bais);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(flatRows));

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=ReporteInventarioExtendido.pdf");
        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    /** Exportar a Excel **/
    public void exportarExcel(List<ArticleReportData> reportData, CompanyConfiguration companyConfiguration) throws IOException {

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Inventario Extendido");

        // Estilos
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerStyle.setFont(headerFont);

        HSSFCellStyle articleStyle = workbook.createCellStyle();
        HSSFFont articleFont = workbook.createFont();
        articleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        articleStyle.setFont(articleFont);
        articleStyle.setFillForegroundColor(org.apache.poi.hssf.util.HSSFColor.GREY_25_PERCENT.index);
        articleStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        HSSFCellStyle dateStyle = workbook.createCellStyle();
        HSSFDataFormat dateFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(dateFormat.getFormat("dd/MM/yyyy"));

        HSSFCellStyle numberStyle = workbook.createCellStyle();
        HSSFDataFormat numFormat = workbook.createDataFormat();
        numberStyle.setDataFormat(numFormat.getFormat("#,##0.00"));

        HSSFCellStyle numberBoldStyle = workbook.createCellStyle();
        numberBoldStyle.setDataFormat(numFormat.getFormat("#,##0.00"));
        numberBoldStyle.setFont(headerFont);

        String filterName = "";
        if (group != null) filterName = " - " + group.getName();
        if (subGroup != null) filterName = " - " + subGroup.getName();

        // Encabezado
        int rowNum = 0;
        HSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getCompanyName());
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("REPORTE DE INVENTARIO EXTENDIDO");
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Almacen:");
        row.createCell(1).setCellValue(warehouse.getFullName() + filterName);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Periodo:");
        row.createCell(1).setCellValue(sdf.format(startDate) + " - " + sdf.format(endDate));

        rowNum++; // fila vacia

        BigDecimal grandTotalEntry = BigDecimal.ZERO;
        BigDecimal grandTotalOutput = BigDecimal.ZERO;
        BigDecimal grandTotalBalance = BigDecimal.ZERO;

        for (ArticleReportData article : reportData) {

            // Cabecera del articulo (fondo gris A-E)
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(article.getArticleCode() + " - " + article.getArticleName());
            row.getCell(0).setCellStyle(articleStyle);
            // B y C: solo fondo gris, sin texto para que A se desborde visualmente
            row.createCell(1).setCellStyle(articleStyle);
            row.createCell(2).setCellStyle(articleStyle);
            row.createCell(3).setCellValue("Unidad: " + article.getUnit());
            row.getCell(3).setCellStyle(articleStyle);
            row.createCell(4).setCellValue("Subgrupo: " + article.getSubgroupName());
            row.getCell(4).setCellStyle(articleStyle);

            // Inventario Inicial + Cabecera de columnas en misma fila
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Inventario Inicial:");
            row.getCell(0).setCellStyle(headerStyle);
            HSSFCell initCell = row.createCell(1);
            initCell.setCellValue(article.getInitialBalance().doubleValue());
            initCell.setCellStyle(numberBoldStyle);

            // Cabecera de columnas de movimiento
            row = sheet.createRow(rowNum++);
            String[] headers = {"Fecha", "Entrada", "Salida", "Saldo", "Glosa"};
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            BigDecimal articleTotalEntry = BigDecimal.ZERO;
            BigDecimal articleTotalOutput = BigDecimal.ZERO;

            if (article.getMovements() != null && !article.getMovements().isEmpty()) {
                for (MovementRow mov : article.getMovements()) {
                    row = sheet.createRow(rowNum++);

                    HSSFCell dateCell = row.createCell(0);
                    dateCell.setCellValue(mov.getDate());
                    dateCell.setCellStyle(dateStyle);

                    HSSFCell entryCell = row.createCell(1);
                    entryCell.setCellValue(mov.getEntryAmount().doubleValue());
                    entryCell.setCellStyle(numberStyle);

                    HSSFCell outputCell = row.createCell(2);
                    outputCell.setCellValue(mov.getOutputAmount().doubleValue());
                    outputCell.setCellStyle(numberStyle);

                    HSSFCell balanceCell = row.createCell(3);
                    balanceCell.setCellValue(mov.getBalance().doubleValue());
                    balanceCell.setCellStyle(numberStyle);

                    row.createCell(4).setCellValue(mov.getDescription());

                    articleTotalEntry = BigDecimalUtil.sum(articleTotalEntry, mov.getEntryAmount(), 2);
                    articleTotalOutput = BigDecimalUtil.sum(articleTotalOutput, mov.getOutputAmount(), 2);
                }
            } else {
                // Sin movimiento: mostrar saldo = inventario inicial
                row = sheet.createRow(rowNum++);
                row.createCell(0);
                HSSFCell entryCell = row.createCell(1);
                entryCell.setCellValue(0);
                entryCell.setCellStyle(numberStyle);
                HSSFCell outputCell = row.createCell(2);
                outputCell.setCellValue(0);
                outputCell.setCellStyle(numberStyle);
                HSSFCell balanceCell = row.createCell(3);
                balanceCell.setCellValue(article.getInitialBalance().doubleValue());
                balanceCell.setCellStyle(numberStyle);
                row.createCell(4).setCellValue("Sin movimiento en el periodo");
            }

            // Totales del articulo
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Subtotal");
            row.getCell(0).setCellStyle(headerStyle);
            HSSFCell stEntryCell = row.createCell(1);
            stEntryCell.setCellValue(articleTotalEntry.doubleValue());
            stEntryCell.setCellStyle(numberBoldStyle);
            HSSFCell stOutputCell = row.createCell(2);
            stOutputCell.setCellValue(articleTotalOutput.doubleValue());
            stOutputCell.setCellStyle(numberBoldStyle);

            grandTotalEntry = BigDecimalUtil.sum(grandTotalEntry, articleTotalEntry, 2);
            grandTotalOutput = BigDecimalUtil.sum(grandTotalOutput, articleTotalOutput, 2);
            // Saldo final del articulo
            if (article.getMovements() != null && !article.getMovements().isEmpty()) {
                BigDecimal lastBalance = article.getMovements().get(article.getMovements().size() - 1).getBalance();
                grandTotalBalance = BigDecimalUtil.sum(grandTotalBalance, lastBalance, 2);
            } else {
                grandTotalBalance = BigDecimalUtil.sum(grandTotalBalance, article.getInitialBalance(), 2);
            }

            rowNum++; // fila vacia separadora
        }

        // Totales generales
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("TOTAL GENERAL");
        row.getCell(0).setCellStyle(headerStyle);
        HSSFCell gtEntryCell = row.createCell(1);
        gtEntryCell.setCellValue(grandTotalEntry.doubleValue());
        gtEntryCell.setCellStyle(numberBoldStyle);
        HSSFCell gtOutputCell = row.createCell(2);
        gtOutputCell.setCellValue(grandTotalOutput.doubleValue());
        gtOutputCell.setCellStyle(numberBoldStyle);
        HSSFCell gtBalanceCell = row.createCell(3);
        gtBalanceCell.setCellValue(grandTotalBalance.doubleValue());
        gtBalanceCell.setCellStyle(numberBoldStyle);

        // Anchos fijos de columnas (en unidades de 1/256 de caracter)
        sheet.setColumnWidth(0, 18 * 256);
        sheet.setColumnWidth(1, 18 * 256);
        sheet.setColumnWidth(2, 18 * 256);
        sheet.setColumnWidth(3, 14 * 256);
        sheet.setColumnWidth(4, 80 * 256);

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-disposition", "attachment; filename=ReporteInventarioExtendido.xls");
        ServletOutputStream stream = response.getOutputStream();
        workbook.write(stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    /** Aplanar datos para JasperReports: una fila por movimiento, con datos del articulo en cada fila **/
    private List<FlatReportRow> flattenData(List<ArticleReportData> reportData) {
        List<FlatReportRow> rows = new ArrayList<FlatReportRow>();
        for (ArticleReportData article : reportData) {
            if (article.getMovements() != null && !article.getMovements().isEmpty()) {
                for (MovementRow mov : article.getMovements()) {
                    rows.add(new FlatReportRow(
                            article.getArticleCode(), article.getArticleName(),
                            article.getUnit(), article.getSubgroupName(),
                            article.getInitialBalance(),
                            mov.getDate(), mov.getEntryAmount(), mov.getOutputAmount(),
                            mov.getBalance(), mov.getDescription()));
                }
            } else {
                // Articulo sin movimiento pero con saldo inicial
                rows.add(new FlatReportRow(
                        article.getArticleCode(), article.getArticleName(),
                        article.getUnit(), article.getSubgroupName(),
                        article.getInitialBalance(),
                        null, BigDecimal.ZERO, BigDecimal.ZERO,
                        article.getInitialBalance(), "Sin movimiento en el periodo"));
            }
        }
        return rows;
    }

    // --- Getters y Setters ---

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public void cleanWarehouseField() { warehouse = null; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public void cleanGroupField() { setGroup(null); }
    public SubGroup getSubGroup() { return subGroup; }
    public void setSubGroup(SubGroup subGroup) { this.subGroup = subGroup; }
    public void cleanSubGroupField() { setSubGroup(null); }
    public ReportFormat getReportFormat() { return reportFormat; }
    public void setReportFormat(ReportFormat reportFormat) { this.reportFormat = reportFormat; }


    // --- Inner classes ---

    /** Datos de un articulo con su lista de movimientos **/
    public static class ArticleReportData {
        private String articleCode;
        private String articleName;
        private String unit;
        private String subgroupName;
        private BigDecimal initialBalance;
        private List<MovementRow> movements;

        public ArticleReportData(String articleCode, String articleName, String unit, String subgroupName, BigDecimal initialBalance) {
            this.articleCode = articleCode;
            this.articleName = articleName;
            this.unit = unit;
            this.subgroupName = subgroupName;
            this.initialBalance = initialBalance;
            this.movements = new ArrayList<MovementRow>();
        }

        public String getArticleCode() { return articleCode; }
        public String getArticleName() { return articleName; }
        public String getUnit() { return unit; }
        public String getSubgroupName() { return subgroupName; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public List<MovementRow> getMovements() { return movements; }
        public void setMovements(List<MovementRow> movements) { this.movements = movements; }
    }

    /** Fila de movimiento individual **/
    public static class MovementRow {
        private Date date;
        private BigDecimal entryAmount;
        private BigDecimal outputAmount;
        private BigDecimal balance;
        private String description;

        public MovementRow(Date date, BigDecimal entryAmount, BigDecimal outputAmount, String description) {
            this.date = date;
            this.entryAmount = entryAmount;
            this.outputAmount = outputAmount;
            this.balance = BigDecimal.ZERO;
            this.description = description;
        }

        public Date getDate() { return date; }
        public BigDecimal getEntryAmount() { return entryAmount; }
        public BigDecimal getOutputAmount() { return outputAmount; }
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        public String getDescription() { return description; }
    }

    /** Fila plana para JasperReports **/
    public static class FlatReportRow {
        private String articleCode;
        private String articleName;
        private String unit;
        private String subgroupName;
        private BigDecimal initialBalance;
        private Date date;
        private BigDecimal entryAmount;
        private BigDecimal outputAmount;
        private BigDecimal balance;
        private String description;

        public FlatReportRow(String articleCode, String articleName, String unit, String subgroupName,
                             BigDecimal initialBalance, Date date, BigDecimal entryAmount,
                             BigDecimal outputAmount, BigDecimal balance, String description) {
            this.articleCode = articleCode;
            this.articleName = articleName;
            this.unit = unit;
            this.subgroupName = subgroupName;
            this.initialBalance = initialBalance;
            this.date = date;
            this.entryAmount = entryAmount;
            this.outputAmount = outputAmount;
            this.balance = balance;
            this.description = description;
        }

        public String getArticleCode() { return articleCode; }
        public String getArticleName() { return articleName; }
        public String getUnit() { return unit; }
        public String getSubgroupName() { return subgroupName; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public Date getDate() { return date; }
        public BigDecimal getEntryAmount() { return entryAmount; }
        public BigDecimal getOutputAmount() { return outputAmount; }
        public BigDecimal getBalance() { return balance; }
        public String getDescription() { return description; }
    }
}
