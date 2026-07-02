package com.encens.khipus.action.warehouse.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.InitialInventory;
import com.encens.khipus.model.warehouse.MovementDetail;
import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.xproduction.XProductionProduct;
import com.encens.khipus.model.xproduction.XProductionUlexita;
import com.encens.khipus.model.xproduction.XSupply;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.production.CollectMaterialService;
import com.encens.khipus.service.production.ProductionOrderService;
import com.encens.khipus.service.warehouse.MovementDetailService;
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
import java.text.DecimalFormat;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("kardexProductMovementAction")
@Scope(ScopeType.PAGE)
public class KardexProductMovementAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private ProductItem productItem;

    /** Resultado calculado para mostrar en pantalla (misma data que PDF/Excel). */
    private List<CollectionData> resultList = new ArrayList<CollectionData>();
    private BigDecimal previousAmount = BigDecimal.ZERO;
    private BigDecimal totalEntradas = BigDecimal.ZERO;
    private BigDecimal totalSalidas = BigDecimal.ZERO;
    private BigDecimal saldoFinal = BigDecimal.ZERO;
    private boolean showResults = false;
    /** Descripcion completa seleccionada para mostrar en el modal al hacer click. */
    private String selectedDescription;
    /** Año de origen para recalcular el saldo cuando no hay ningun inv_inicio cargado. */
    private static final int ORIGIN_YEAR = 2000;

    @In
    private MovementDetailService movementDetailService;
    @In
    private ProductItemService productItemService;
    @In
    private ProductionOrderService productionOrderService;
    @In
    private CollectMaterialService collectMaterialService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    @In
    private XProductionService xproductionService;

    @Create
    public void init() {
        restrictions = new String[]{};
        // Rango por defecto: 1ro de enero del año actual hasta hoy (reporte mas agil).
        Date today = new Date();
        startDate = DateUtils.firstDayOfYear(DateUtils.getCurrentYear(today));
        endDate = today;
    }


    protected String getEjbql() {
        return "";
    }

    public void generateReport() {

        log.debug("generating Kardex Product Movement................................................");
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");;}

        Collection<CollectionData> beanCollection = computeReport();

        HashMap parameters = new HashMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("reportTitle", "REPORTE DE MOVIMIENTOS");
        paramMap.put("companyName", companyConfiguration.getCompanyName());
        paramMap.put("systemName", companyConfiguration.getSystemName());
        paramMap.put("locationName", companyConfiguration.getLocationName());
        paramMap.put("productItemName", productItem.getFullName());
        paramMap.put("unit", productItem.getUsageMeasureCode());
        paramMap.put("startDate", startDate);
        paramMap.put("endDate", endDate);
        paramMap.put("previousAmount", previousAmount);

        parameters.putAll(paramMap);

        try{
            if (getReportFormat() != null && (getReportFormat().name().equals("XLS") || getReportFormat().name().equals("XLSX"))) {
                exportarExcel(beanCollection, companyConfiguration, previousAmount);
            } else {
                File jasper = new File(JSFUtil.getRealPath("/warehouse/reports/kardexProductMovement.jasper"));
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), parameters, new JRBeanCollectionDataSource(beanCollection));
                exportarPDF(jasperPrint);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Fuente unica de datos del reporte: arma la coleccion ordenada, calcula el saldo
     * anterior, el saldo corrido por fila (CollectionData.amount) y los totales.
     * Usada por la vista en pantalla, el PDF y el Excel para que siempre coincidan.
     */
    public List<CollectionData> computeReport() {
        List<CollectionData> datas = new ArrayList<CollectionData>(calculateCollectionData());

        previousAmount = BigDecimal.ZERO;
        if (startDate != null && productItem != null) {
            previousAmount = calculateInitialAmountToKardex(productItem.getProductItemCode(), startDate);
        }

        BigDecimal saldo = previousAmount;
        totalEntradas = BigDecimal.ZERO;
        totalSalidas = BigDecimal.ZERO;
        for (CollectionData data : datas) {
            saldo = BigDecimalUtil.sum(saldo, data.getAmountEntry(), 2);
            saldo = BigDecimalUtil.subtract(saldo, data.getAmountOutput(), 2);
            data.setAmount(saldo);
            totalEntradas = BigDecimalUtil.sum(totalEntradas, data.getAmountEntry(), 2);
            totalSalidas = BigDecimalUtil.sum(totalSalidas, data.getAmountOutput(), 2);
        }
        saldoFinal = saldo;
        resultList = datas;
        return datas;
    }

    /** Accion del boton "Ver en pantalla": valida, calcula y muestra el panel de resultados. */
    public void preview() {
        if (productItem == null || productItem.getProductItemCode() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Reports.kardex.productItem.required");
            showResults = false;
            return;
        }
        computeReport();
        showResults = true;
    }

    /** Descarga el reporte en PDF con los filtros actuales. */
    public void downloadPdf() {
        setReportFormat(ReportFormat.PDF);
        generateReport();
    }

    /** Descarga el reporte en Excel con los filtros actuales. */
    public void downloadExcel() {
        setReportFormat(ReportFormat.XLS);
        generateReport();
    }

    public Collection<CollectionData> calculateCollectionData(){

        List<CollectionData> datas = new ArrayList<CollectionData>();

        /*
         * Fuentes de inventario para un articulo (mismo criterio que la vista "Saldos de Almacen"
         * XProductionBalanceService, para que el saldo del reporte cuadre con ese saldo):
         *   inv_movdet (MovementDetail)  vales/despachos aprobados  -> E (+) / S (-)
         *   pro_acopiomateriaprima (CollectMaterial) Peso Empresa    -> E (+)  (estados APR/CONTA)
         *   xpr_producto (XProductionProduct) PT producido           -> E (+)  (orden no ANL)
         *   xpr_insumo (XSupply) consumo de MP/insumos en produccion -> S (-)  (orden no ANL)
         *   xpr_produccion_ulexita (XProductionUlexita) reproceso    -> E/S    (orden no ANL)
         *
         * Se quitaron a proposito las fuentes del modelo de produccion viejo
         * (pro_ordenproduccion/ProductionOrder, pro_productobase/BaseProduct y
         * pro_producto/ProductionProduct) y las ventas (cli_articulopedido/ArticleOrder,
         * contado y credito): la vista de saldos no las considera y, para almacenes de MP/PT,
         * las salidas por venta ya vienen registradas en inv_movdet (S), por lo que contarlas
         * de nuevo duplicaria el movimiento.
         */
        List<MovementDetail> movementDetailList = movementDetailService.findDetailListByProductAndDate(productItem.getProductItemCode(), startDate, endDate);

        List<XProductionProduct> xproductionProductList = productionOrderService.findXProductionByProductItem(productItem.getProductItemCode(), startDate, endDate);

        List<CollectMaterial> collectMaterialList = collectMaterialService.findApprovedCollectMaterialByCode(productItem.getProductItemCode(), startDate, endDate);

        List<XSupply> supplyList = xproductionService.getRawMaterialInProduction( productItem.getProductItemCode(), startDate, endDate);

        List<XProductionUlexita> ulexitaReprocessList = xproductionService.getUlexitaReprocessByArticle(productItem.getProductItemCode(), startDate, endDate);

        for (CollectMaterial collectMaterial : collectMaterialList) {
            CollectionData collectionData = new CollectionData(
                    formatearFecha(collectMaterial.getDate(), "E"),
                    collectMaterial.getCode(),
                    collectMaterial.getBalanceWeight(),
                    BigDecimal.ZERO,
                    "E",
                    "ACOPIO DE MATERIA PRIMA EN FECHA " + DateUtils.format(collectMaterial.getDate(), "dd/MM/yyyy") );
            datas.add(collectionData);
        }


        // XProduction: Producto Terminado producido (entrada). Se excluyen ordenes anuladas.
        for (XProductionProduct product : xproductionProductList){
            if (product.getProduction() != null && ProductionState.ANL.equals(product.getProduction().getState())) {
                continue;
            }
            CollectionData collectionData = new CollectionData(
                    formatearFecha(product.getProductionPlan().getDate(), "E"),
                    product.getProductItemCode() ,
                    product.getQuantity() ,
                    BigDecimal.ZERO,
                    "E",
                    "ORDEN DE PRODUCCION FECHA " + DateUtils.format(product.getProductionPlan().getDate(), "dd/MM/yyyy") );
            datas.add(collectionData);
        }

        for (MovementDetail md:movementDetailList){
            CollectionData collectionData = new CollectionData(
                    /*md.getMovementDetailDate(),*/
                    formatearFecha(md.getInventoryMovement().getWarehouseVoucher().getDate(), md.getMovementType().name()),
                    md.getInventoryMovement().getWarehouseVoucher().getNumber(),
                    md.getMovementType().name().equals("E") ? md.getQuantity() : BigDecimal.ZERO,
                    md.getMovementType().name().equals("S") ? md.getQuantity() : BigDecimal.ZERO,
                    md.getMovementType().name(),
                    md.getInventoryMovement().getDescription());
            datas.add(collectionData);
        }

        for ( XSupply supply : supplyList ){
            if (supply.getProduction() != null && ProductionState.ANL.equals(supply.getProduction().getState())) {
                continue;
            }
            String dateString = DateUtils.format(supply.getProduction().getProductionPlan().getDate(), "dd/MM/yyyy");
            CollectionData collectionData = new CollectionData( formatearFecha(supply.getProduction().getProductionPlan().getDate(), "S"),
                    supply.getProduction().getCode().toString(),
                    BigDecimal.ZERO,
                    supply.getQuantity(),
                    "S",
                    "Materia prima en produccion " + supply.getProduction().getCode() + " " + dateString);
            datas.add(collectionData);
        }

        // Reproceso de ordenes ULEXITA (xpr_produccion_ulexita): reproceso final -> entrada,
        // consumo reproceso -> salida. Valores en TN, se convierten a la unidad del articulo.
        for (XProductionUlexita ulexita : ulexitaReprocessList){
            Date planDate = ulexita.getProduction().getProductionPlan().getDate();
            String dateString = DateUtils.format(planDate, "dd/MM/yyyy");
            String unit = productItem.getUsageMeasureCode();

            BigDecimal reprocesoFinal = tnToUnit(ulexita.getReprocesoFinalTn(), unit);
            if (reprocesoFinal.compareTo(BigDecimal.ZERO) != 0) {
                datas.add(new CollectionData(formatearFecha(planDate, "E"),
                        ulexita.getProduction().getCode().toString(),
                        reprocesoFinal,
                        BigDecimal.ZERO,
                        "E",
                        "Reproceso final ULEXITA produccion " + ulexita.getProduction().getCode() + " " + dateString));
            }

            BigDecimal consumoReproceso = tnToUnit(ulexita.getConsumoReprocesoTn(), unit);
            if (consumoReproceso.compareTo(BigDecimal.ZERO) != 0) {
                datas.add(new CollectionData(formatearFecha(planDate, "S"),
                        ulexita.getProduction().getCode().toString(),
                        BigDecimal.ZERO,
                        consumoReproceso,
                        "S",
                        "Consumo reproceso ULEXITA produccion " + ulexita.getProduction().getCode() + " " + dateString));
            }
        }

        Collections.sort(datas, new Comparator<CollectionData>() {
            @Override
            public int compare(CollectionData o1, CollectionData o2) {
                //return o1.getDate().toString().compareTo(o2.getDate().toString());
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        Collection<CollectionData> beanCollection = new ArrayList();
        for (CollectionData data:datas){
            beanCollection.add(data);
        }

        return beanCollection;
    }

    private Date formatearFecha(Date fechaOriginal, String movementType){

        // Añadir horas, minutos y segundos
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaOriginal);
        if ( movementType.equals("E")){
            calendar.set(Calendar.HOUR_OF_DAY, 1);
            calendar.set(Calendar.MINUTE, 1);
            calendar.set(Calendar.SECOND, 1);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }
        Date fechaHora = calendar.getTime();
        return fechaHora;
    }

    /** Convierte un valor en TN a la unidad del articulo (KG = x1000; otra unidad = se asume TN). */
    static BigDecimal tnToUnit(BigDecimal tn, String measureCode){
        if (tn == null) {
            return BigDecimal.ZERO;
        }
        if ("KG".equalsIgnoreCase(measureCode)) {
            return tn.multiply(new BigDecimal("1000"));
        }
        return tn;
    }

    /*
     * Saldo a la fecha de inicio: se acumulan las mismas fuentes que la lista de movimientos
     * (ver calculateCollectionData) hasta el dia anterior a initDate.
     * Se quitaron a proposito el modelo de produccion viejo (pro_ordenproduccion/ProductionOrder,
     * pro_productobase/BaseProduct, pro_producto/ProductionProduct) y las ventas
     * (cli_articulopedido/ArticleOrder), por las mismas razones descritas en calculateCollectionData.
     *
     * Base del saldo (robusto): se toma el inv_inicio de la gestion mas reciente <= año del
     * reporte que TENGA registro para el articulo, y se acumulan los movimientos desde el 1ro
     * de enero de esa gestion. Si el articulo no tiene ningun inv_inicio <= año del reporte, se
     * recalcula todo el historico desde ORIGIN_YEAR (evita saldo 0 por falta de carga de apertura).
     * Se distingue "no cargado" de "cargado en 0" por la existencia del registro, no por el valor.
     */
    public BigDecimal calculateInitialAmountToKardex(String productItemCode, Date initDate){

        Calendar calendar = Calendar.getInstance();
        BigDecimal initialQuantity = BigDecimal.ZERO;

        /** Año del inicio del reporte (antes de restar el dia) **/
        String reportYear = DateUtils.getCurrentYear(initDate).toString();

        /** Restando un dia a la fecha **/
        calendar.setTime(initDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        initDate = calendar.getTime();

        /** Base + fecha desde la cual acumular **/
        Date firstDate;
        InitialInventory base = productItemService.findLatestInitialInventory(productItemCode, reportYear);
        if (base != null) {
            initialQuantity = (base.getQuantity() != null) ? base.getQuantity() : BigDecimal.ZERO;
            firstDate = DateUtils.firstDayOfYear(Integer.valueOf(base.getYear()));
        } else {
            firstDate = DateUtils.firstDayOfYear(ORIGIN_YEAR);
        }

        List<MovementDetail> movementDetailList = movementDetailService.findDetailListByProductAndDate(productItemCode, firstDate, initDate);
        List<CollectMaterial> collectMaterialList = collectMaterialService.findApprovedCollectMaterialByCode(productItemCode, firstDate, initDate);
        List<XProductionProduct> xproductionProductList = productionOrderService.findXProductionByProductItem(productItemCode, firstDate, initDate);
        List<XSupply> supplyList = xproductionService.getRawMaterialInProduction(productItemCode, firstDate, initDate);
        List<XProductionUlexita> ulexitaReprocessList = xproductionService.getUlexitaReprocessByArticle(productItemCode, firstDate, initDate);

        for (MovementDetail md:movementDetailList){
            if (md.getMovementType().equals(MovementDetailType.E))
                initialQuantity = BigDecimalUtil.sum(initialQuantity, md.getQuantity(), 2);
            if (md.getMovementType().equals(MovementDetailType.S))
                initialQuantity = BigDecimalUtil.subtract(initialQuantity, md.getQuantity(), 2);
        }

        // Acopio de materia prima (entrada): Peso Empresa (balanceWeight)
        for (CollectMaterial collectMaterial : collectMaterialList){
            initialQuantity = BigDecimalUtil.sum(initialQuantity, collectMaterial.getBalanceWeight(), 2);
        }

        // XProduction: Producto Terminado producido (entrada). Se excluyen ordenes anuladas.
        for (XProductionProduct product : xproductionProductList){
            if (product.getProduction() != null && ProductionState.ANL.equals(product.getProduction().getState())) {
                continue;
            }
            initialQuantity = BigDecimalUtil.sum(initialQuantity, product.getQuantity(), 2);
        }

        // XProduction: consumo de materia prima/insumos (salida). Se excluyen ordenes anuladas.
        for (XSupply supply : supplyList){
            if (supply.getProduction() != null && ProductionState.ANL.equals(supply.getProduction().getState())) {
                continue;
            }
            initialQuantity = BigDecimalUtil.subtract(initialQuantity, supply.getQuantity(), 2);
        }

        // Reproceso ULEXITA (TN -> unidad): reproceso final entra, consumo reproceso sale.
        String unit = productItem.getUsageMeasureCode();
        for (XProductionUlexita ulexita : ulexitaReprocessList){
            initialQuantity = BigDecimalUtil.sum(initialQuantity, tnToUnit(ulexita.getReprocesoFinalTn(), unit), 2);
            initialQuantity = BigDecimalUtil.subtract(initialQuantity, tnToUnit(ulexita.getConsumoReprocesoTn(), unit), 2);
        }

        return  initialQuantity;
    }

    public void exportarExcel(Collection<CollectionData> beanCollection, CompanyConfiguration companyConfiguration, BigDecimal previousAmount) throws IOException {

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Kardex");

        // Estilos
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerStyle.setFont(headerFont);

        HSSFCellStyle dateStyle = workbook.createCellStyle();
        HSSFDataFormat dateFormat = workbook.createDataFormat();
        dateStyle.setDataFormat(dateFormat.getFormat("dd/MM/yyyy"));

        HSSFCellStyle numberStyle = workbook.createCellStyle();
        HSSFDataFormat numFormat = workbook.createDataFormat();
        numberStyle.setDataFormat(numFormat.getFormat("#,##0.00"));

        // Encabezado
        int rowNum = 0;
        HSSFRow row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(companyConfiguration.getCompanyName());
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("REPORTE DE MOVIMIENTOS - KARDEX");
        row.getCell(0).setCellStyle(headerStyle);

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Articulo:");
        row.createCell(1).setCellValue(productItem.getFullName());

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Unidad:");
        row.createCell(1).setCellValue(productItem.getUsageMeasureCode());

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Periodo:");
        row.createCell(1).setCellValue(sdf.format(startDate) + " - " + sdf.format(endDate));

        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Saldo Anterior:");
        HSSFCell prevCell = row.createCell(1);
        prevCell.setCellValue(previousAmount.doubleValue());
        prevCell.setCellStyle(numberStyle);

        rowNum++; // fila vacia

        // Cabecera de tabla
        row = sheet.createRow(rowNum++);
        String[] headers = {"Fecha", "Codigo", "Entradas", "Salidas", "Saldo", "Tipo", "Descripcion"};
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        BigDecimal saldo = previousAmount;
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSalidas = BigDecimal.ZERO;

        for (CollectionData data : beanCollection) {
            row = sheet.createRow(rowNum++);

            HSSFCell dateCell = row.createCell(0);
            dateCell.setCellValue(data.getDate());
            dateCell.setCellStyle(dateStyle);

            row.createCell(1).setCellValue(data.getCode());

            HSSFCell entryCell = row.createCell(2);
            entryCell.setCellValue(data.getAmountEntry().doubleValue());
            entryCell.setCellStyle(numberStyle);

            HSSFCell outputCell = row.createCell(3);
            outputCell.setCellValue(data.getAmountOutput().doubleValue());
            outputCell.setCellStyle(numberStyle);

            saldo = BigDecimalUtil.sum(saldo, data.getAmountEntry(), 2);
            saldo = BigDecimalUtil.subtract(saldo, data.getAmountOutput(), 2);

            HSSFCell saldoCell = row.createCell(4);
            saldoCell.setCellValue(saldo.doubleValue());
            saldoCell.setCellStyle(numberStyle);

            row.createCell(5).setCellValue(data.getMovementType());
            row.createCell(6).setCellValue(data.getDescription());

            totalEntradas = BigDecimalUtil.sum(totalEntradas, data.getAmountEntry(), 2);
            totalSalidas = BigDecimalUtil.sum(totalSalidas, data.getAmountOutput(), 2);
        }

        // Fila de totales
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("TOTALES");
        row.getCell(0).setCellStyle(headerStyle);

        HSSFCell totalEntCell = row.createCell(2);
        totalEntCell.setCellValue(totalEntradas.doubleValue());
        totalEntCell.setCellStyle(numberStyle);

        HSSFCell totalSalCell = row.createCell(3);
        totalSalCell.setCellValue(totalSalidas.doubleValue());
        totalSalCell.setCellStyle(numberStyle);

        HSSFCell saldoFinalCell = row.createCell(4);
        saldoFinalCell.setCellValue(saldo.doubleValue());
        saldoFinalCell.setCellStyle(numberStyle);

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Enviar respuesta
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-disposition", "attachment; filename=kardexProductMovement.xls");
        ServletOutputStream stream = response.getOutputStream();
        workbook.write(stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void exportarPDF(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=kardexProductMovement.pdf");
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

    public List<CollectionData> getResultList() {
        return resultList;
    }

    public BigDecimal getPreviousAmount() {
        return previousAmount;
    }

    public BigDecimal getTotalEntradas() {
        return totalEntradas;
    }

    public BigDecimal getTotalSalidas() {
        return totalSalidas;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }

    public boolean isShowResults() {
        return showResults;
    }

    /** Guarda la descripcion a mostrar en el modal (accion del click en la celda). */
    public void selectDescription(String description) {
        this.selectedDescription = description;
    }

    public String getSelectedDescription() {
        return selectedDescription;
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


    public class CollectionData{

        private Date date;
        private String code;
        private BigDecimal amountEntry;
        private BigDecimal amountOutput;
        private BigDecimal amount;
        private String movementType;
        private String description;

        public CollectionData(Date date, String code, BigDecimal amountEntry, BigDecimal amountOutput, String movementType, String description){
            this.date = date;
            this.code = code;
            this.amountEntry = amountEntry;
            this.amountOutput = amountOutput;
            this.movementType = movementType;
            this.description = description;
        }


        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getMovementType() {
            return movementType;
        }

        public void setMovementType(String movementType) {
            this.movementType = movementType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmountEntry() {
            return amountEntry;
        }

        public void setAmountEntry(BigDecimal amountEntry) {
            this.amountEntry = amountEntry;
        }

        public BigDecimal getAmountOutput() {
            return amountOutput;
        }

        public void setAmountOutput(BigDecimal amountOutput) {
            this.amountOutput = amountOutput;
        }
    }


}