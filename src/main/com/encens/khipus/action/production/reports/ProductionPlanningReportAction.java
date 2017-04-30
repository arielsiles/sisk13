package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.production.ProductionPlanningAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.production.*;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.RoundUtil;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRTemplatePrintText;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate payroll summary report by payment method and currency
 *
 * @author
 * @version $Id: SummaryPayrollByPaymentMethodReportAction.java  22-ene-2010 11:38:12$
 */
@Name("productionPlanningReportAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionPlanningReportAction extends GenericReportAction {

    @In
    User currentUser;
    private List<ProductionPlanningAction.Consolidated> consolidatedsIN;
    private List<ProductionOrder> productionOrders;
    private List<ProductionIngredient> ingredientList;
    private List<OrderMaterial> orderMaterials;
    private ProductionPlanning productionPlanning;
    private ProductionOrder productionOrder;
    private List<OrderInput> orderInputs;

    private String date;
    private String state;
    private Double unitPrice;

    public void generateReportByOrder(List<ProductionIngredient> ingredients, List<OrderInput> inputs, List<OrderMaterial> materials, ProductionPlanning planning, ProductionOrder order) {
        productionOrder = order;
        productionPlanning = planning;
        ingredientList = ingredients;
        orderInputs = inputs;
        orderMaterials = materials;
        log.debug("Generate ProductionPlannigReportAction........");
        TypedReportData typedReportData;
        String templatePath = "/production/reports/productionOrderReport.jrxml";
        String fileName = "Orden_Produccion";
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        date = sdf.format(productionPlanning.getDate());
        state = getEstate(order.getEstateOrder());
        Map params = new HashMap();

        params.putAll(getCommonDocumentParamsInfo());

        String query = "select IA.COD_ART, MP.NOMBRE , IA.COD_MED \n" +
                "from INGREDIENTEPRODUCCION IP\n" +
                "INNER JOIN METAPRODUCTOPRODUCCION MP\n" +
                "ON IP.IDMETAPRODUCTOPRODUCCION=MP.IDMETAPRODUCTOPRODUCCION \n" +
                "INNER JOIN WISE.INV_ARTICULOS IA \n" +
                "ON MP.COD_ART=IA.COD_ART\n" +
                "WHERE IP.IDINGREDIENTEPRODUCCION IN ( ";

        boolean band = true;
        if(ingredients.size() == 0)
            query += "(0)";
        else
            for (ProductionIngredient ingredient : ingredients) {
                query += (band ? " " : ",") + ingredient.getId().toString();
                band = false;
            }
        query += " )";
        setReportFormat(ReportFormat.PDF);

        addProductionOrderMaterialDetailSubReport(params);
        typedReportData = getReport(
                fileName
                , templatePath
                , query
                , params
                , "Orden_Materiales_Insumos"
        );

        JasperPrint jasperPrint = typedReportData.getJasperPrint();

        for (int i = 0; i < typedReportData.getJasperPrint().getPages().size(); i++) {
            int codeCount = 14;
            int nameCount = 13;
            int unitCount = 12;
            int mountCount = 15;

            for (OrderInput input : orderInputs) {
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(codeCount))).setText(input.getProductItem().getProductItemCode());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(nameCount))).setText(input.getProductItem().getName());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(unitCount))).setText(input.getProductItem().getUsageMeasureCode());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(mountCount))).setText(String.format("%.2f", input.getAmount()));
                codeCount += 4;
                nameCount += 4;
                unitCount += 4;
                mountCount += 4;
            }
        }
        try {
            typedReportData.setJasperPrint(jasperPrint);
            GenerationReportData generationReportData = new GenerationReportData(typedReportData);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateReportSummary(List<ProductionIngredient> ingredients, List<OrderInput> inputs, List<OrderMaterial> materials, ProductionPlanning planning, ProductionOrder order, BigDecimal price) {
        productionOrder = order;
        productionPlanning = planning;
        ingredientList = ingredients;
        orderInputs = inputs;
        orderMaterials = materials;
        unitPrice = price.doubleValue();
        log.debug("Generate ProductionPlannigReportAction........");
        TypedReportData typedReportData;
        String templatePath = "/production/reports/productionOrderSummaryReport.jrxml";
        String fileName = "Orden_Produccion_Summary";
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        date = sdf.format(productionPlanning.getDate());
        state = getEstate(productionOrder.getEstateOrder());
        Map params = new HashMap();

        params.putAll(getCommonDocumentParamsInfo());

        String query = "select IA.COD_ART, MP.NOMBRE , IA.COD_MED \n" +
                "from INGREDIENTEPRODUCCION IP\n" +
                "INNER JOIN METAPRODUCTOPRODUCCION MP\n" +
                "ON IP.IDMETAPRODUCTOPRODUCCION=MP.IDMETAPRODUCTOPRODUCCION \n" +
                "INNER JOIN WISE.INV_ARTICULOS IA \n" +
                "ON MP.COD_ART=IA.COD_ART\n" +
                "WHERE IP.IDINGREDIENTEPRODUCCION IN ( ";

        boolean band = true;
        for (ProductionIngredient ingredient : ingredients) {
            query += (band ? " " : ",") + ingredient.getId().toString();
            band = false;
        }
        query += " )";
        setReportFormat(ReportFormat.PDF);

        addProductionOrderMaterialDetailSubReport(params);
        addProductionOrderMaterialSummaryDetailSubReport(params);
        //addProductionOrderHoursDetailSubReport(params);
        typedReportData = getReport(
                fileName
                , templatePath
                , query
                , params
                , "RESUMEN_GENERAL_ORDEN_PRODUCCION"
        );

        JasperPrint jasperPrint = typedReportData.getJasperPrint();

        for (int i = 0; i < typedReportData.getJasperPrint().getPages().size(); i++) {
            int codeCount = 16;
            int nameCount = 15;
            int unitCount = 14;
            int mountCount = 17;
            int costUnit = 18;
            int totalcost = 19;

            for (OrderInput input : orderInputs) {
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(codeCount))).setText(input.getProductItem().getProductItemCode());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(nameCount))).setText(input.getProductItem().getName());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(unitCount))).setText(input.getProductItem().getUsageMeasureCode());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(mountCount))).setText(String.format("%.2f", input.getAmount()));
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(costUnit))).setText(String.format("%.2f", input.getCostUnit()));
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(totalcost))).setText(String.format("%.2f", input.getCostTotal()));
                codeCount += 6;
                nameCount += 6;
                unitCount += 6;
                mountCount += 6;
                costUnit += 6;
                totalcost += 6;
            }
        }
        try {
            typedReportData.setJasperPrint(jasperPrint);
            GenerationReportData generationReportData = new GenerationReportData(typedReportData);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*@Override
    protected String getEjbql()
    {
        String sql = " SELECT productionIngredient.metaProduct.name, " +
                " productionIngredient.metaProduct.productItem.usageMeasureCode " +
                " FROM ProductionIngredient productionIngredient ";

        return sql;
    }

    @Create
    public void init() {
        restrictions = new String[]{"productionIngredient = #{ingredients}"};
    }*/

    private void addProductionOrderMaterialSummaryDetailSubReport(Map mainReportParams) {
        log.debug("Generating productionOrderMaterialDetailSubReport.............................");

        Map<String, Object> params = new HashMap<String, Object>();

        String sql = "SELECT op.cantidadproducida, op.cantidadesperada, op.preciototalmaterial, \n" +
                "       op.preciototalinsumo, op.preciototalmanoobra , op.costotoalproduccion, op.totalcostoindirecto, \n" +
                "       " + RoundUtil.getRoundValue(unitPrice, 3, RoundUtil.RoundMode.SYMMETRIC).toString() + " as precioUnitario\n" +
                "FROM ordenproduccion OP\n" +
                "WHERE op.idordenproduccion = " + productionOrder.getId().toString();

        //generate the sub report

        String subReportKey = "ORDERMATERIALSUMMARYSUBREPORT";
        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/production/reports/productionOrderMaterialSummaryDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                sql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());

    }

    private void addProductionOrderHoursDetailSubReport(Map mainReportParams) {
        log.debug("Generating ProductionOrderHoursDetailSub.............................");

        Map<String, Object> params = new HashMap<String, Object>();

        String sql = "SELECT distinct pe.nombres, pe.apellidopaterno, pe.apellidomaterno, tt.nombre \n" +
                "       ,to_char(te.horainicio,'HH24:MI') as horainicio\n" +
                "       ,to_char(te.horafin,'HH24:MI') as  horafin \n" +
                "       ,24 * (te.horafin - te.horainicio ) as num_horas\n" +
                "       ,nvl(te.costoporhora,0.0) as costoporhora\n" +
                "       ,(\n" +
                "       select nvl( sum(COSTOPORHORA),0.0) FROM tarjetatiempoempleado               \n" +
                "       where idordenproduccion = " + productionOrder.getId().toString() + "\n" +
                "       ) as total \n" +
                "FROM tarjetatiempoempleado te\n" +
                "inner join empleado em \n" +
                "on em.idempleado = te.idempleado\n" +
                "inner join  tipotareaprod tt\n" +
                "on tt.idtipotareaprod = te.idtipotareaprod\n" +
                "inner join persona pe\n" +
                "on pe.idpersona = em.idempleado\n" +
                "where te.IDORDENPRODUCCION = " + productionOrder.getId().toString() + "\n" +
                "group by pe.nombres, pe.apellidopaterno, pe.apellidomaterno, tt.nombre,te.horainicio, te.horafin, te.costoporhora\n" +
                "order by pe.nombres, pe.apellidopaterno, pe.apellidomaterno";

        String subReportKey = "ORDERHOURSUBREPORT";
        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/production/reports/productionOrderHoursDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                sql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());

    }

    private void addProductionOrderMaterialDetailSubReport(Map mainReportParams) {
        log.debug("Generating productionOrderMaterialDetailSubReport.............................");

        Map<String, Object> params = new HashMap<String, Object>();

        String sql = "select ia.descri, IA.COD_ART, om.cantidadpesosolicitada, om.cantidadpesousada, om.cantidadpesoretornada,om.COSTOUNITARIO ,om.COSTOTOTAL \n" +
                "from ordenmaterial om\n" +
                "inner join WISE.INV_ARTICULOS IA \n" +
                "ON om.COD_ART=IA.COD_ART\n" +
                "inner join ordenproduccion op\n" +
                "on op.idordenproduccion = om.idordenproduccion\n" +
                "where IA.COD_ART in ( ";

        boolean band = true;
        for (OrderMaterial orderMaterial : orderMaterials) {
            sql += (band ? " " : ",") + orderMaterial.getProductItem().getProductItemCode().toString();
            band = false;
        }
        sql += " )\n" +
                " and op.idordenproduccion = " + productionOrder.getId().toString();

        //generate the sub report
        if (orderMaterials.size() == 0)
            sql = "select ia.descri, IA.COD_ART, om.cantidadpesosolicitada, om.cantidadpesousada, om.cantidadpesoretornada \n" +
                    "from ordenmaterial om \n" +
                    "inner join WISE.INV_ARTICULOS IA \n" +
                    "ON om.COD_ART=IA.COD_ART \n" +
                    "inner join ordenproduccion op \n" +
                    "on op.idordenproduccion = om.idordenproduccion \n" +
                    " where op.idordenproduccion = 0 ";

        String subReportKey = "ORDERMATERIALSUBREPORT";
        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/production/reports/productionOrderMaterialDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                sql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());

    }

    private void addProductionOrderPlanningDetailSubReport(Map mainReportParams) {
        log.debug("Generating productionOrderPlanningDetailSubReport.............................");

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                " productionOrder.productComposition.processedProduct.name, " +
                " productionOrder.productComposition.processedProduct.code, " +
                " productionOrder.code, " +
                " productionOrder.productComposition.name, " +
                " productionOrder.supposedAmount, " +
                " productionOrder.producingAmount " +
                " FROM ProductionOrder productionOrder ";

        String sql = "SELECT mp.nombre as nombre ,mp.codigo as codigo , op.codigo as codigo_orden, cp.nombre as formula, op.TEORICOOBTENIDO as teorico, op.CANTIDADPRODUCIR as producido \n" +
                "FROM ordenproduccion OP\n" +
                "INNER JOIN composicionproducto CP\n" +
                "ON cp.idcomposicionproducto = op.idcomposicionproducto\n" +
                "INNER JOIN productoprocesado PP\n" +
                "ON pp.idproductoprocesado = cp.idproductoprocesado\n" +
                "INNER JOIN metaproductoproduccion MP\n" +
                "ON mp.idmetaproductoproduccion = pp.idproductoprocesado\n" +
                "WHERE op.idordenproduccion IN (";

        boolean band = true;
        for (ProductionOrder productionOrder : productionOrders) {
            sql += (band ? " " : ",") + productionOrder.getId().toString();
            band = false;
        }
        sql += " )";


        String[] restrictions = new String[]{
                "productionOrder = #{productionOrders}",
        };

        String orderBy = "";
        //generate the sub report
        String subReportKey = "INCOMEBYCONCEPTSUBREPORT";

        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/production/reports/productionOrderPlanningDetailSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                sql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    public void generateReport(List<ProductionPlanningAction.Consolidated> consolidatedLists, ProductionPlanning productionPlanning, List<ProductionOrder> orders) {
        log.debug("Generate ProductionPlannigReportAction........");
        productionOrders = orders;
        TypedReportData typedReportData;
        String templatePath = "/production/reports/productionPlanningReportprub.jrxml";
        String fileName = "ProductionPlanningReportAction";
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        date = sdf.format(productionPlanning.getDate());
        state = getEstate(productionPlanning.getState());
        Map params = new HashMap();

        params.putAll(getCommonDocumentParamsInfo());
        consolidatedsIN = consolidatedLists;
        setReportFormat(ReportFormat.PDF);

        addProductionOrderPlanningDetailSubReport(params);

        String query = " select nombre, codigo, cod_med  " +
                " from metaproductoproduccion mp " +
                " inner join WISE.inv_articulos ia " +
                " on ia.cod_art = mp.codigo " +
                " where idmetaproductoproduccion in ( ";
        boolean band = true;
        for (ProductionPlanningAction.Consolidated consolidated : consolidatedsIN) {
            query += (band ? " " : ",") + consolidated.getIdMeta().toString();
            band = false;
        }
        query += " )";

        typedReportData = getReport(
                fileName
                , templatePath
                , query
                , params
                , "productionPlanningReport"
        );

        JasperPrint jasperPrint = typedReportData.getJasperPrint();

        for (int i = 0; i < typedReportData.getJasperPrint().getPages().size(); i++) {
            int contName = 7;
            int contUnidad = 9;
            int contCod = 8;
            int cantidad = 10;

            for (ProductionPlanningAction.Consolidated consolidated : consolidatedsIN) {
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(contName))).setText(consolidated.getName());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(contUnidad))).setText(consolidated.getUnit());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(contCod))).setText(consolidated.getCode());
                ((JRTemplatePrintText) (((JRPrintPage) (typedReportData.getJasperPrint().getPages().get(i))).getElements().get(cantidad))).setText(String.format("%.2f", consolidated.getAmount()));
                contName += 4;
                contUnidad += 4;
                contCod += 4;
                cantidad += 4;
            }
        }
        try {
            typedReportData.setJasperPrint(jasperPrint);
            GenerationReportData generationReportData = new GenerationReportData(typedReportData);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getEstate(ProductionPlanningState statePlaning) {
        String estateLiteral = "";

        if (statePlaning == ProductionPlanningState.EXECUTED)
            estateLiteral = MessageUtils.getMessage("ProductionPlanning.state.executed");

        if (statePlaning == ProductionPlanningState.FINALIZED)
            estateLiteral = MessageUtils.getMessage("ProductionPlanning.state.finalized");

        if (statePlaning == ProductionPlanningState.PENDING)
            estateLiteral = MessageUtils.getMessage("ProductionPlanning.state.pending");

        if (statePlaning == ProductionPlanningState.TABULATED)
            estateLiteral = MessageUtils.getMessage("productionOrderForPlanning.estateOrder.tabulated");

        return estateLiteral;
    }

    public Map<String, Object> getCommonDocumentParamsInfo() {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("position", currentUser.getRoles().get(0).getName());
        paramMap.put("userLoginParam", currentUser.getEmployee().getFullName());
        paramMap.put("reportTitle", MessageUtils.getMessage("ProductionPlanning.orderInputOrMaterial"));
        paramMap.put("dateParam", date);
        paramMap.put("estate", state);
        paramMap.put("nameProduct", productionOrder.getProductComposition().getProcessedProduct().getName());
        paramMap.put("codeProduct", productionOrder.getProductComposition().getProcessedProduct().getCode());
        paramMap.put("numOrder", productionOrder.getCode());
        return paramMap;
    }

}