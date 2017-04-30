package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.production.BaseProduct;
import com.encens.khipus.model.production.OrderMaterial;
import com.encens.khipus.model.production.ProductionPlanningState;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.RoundUtil;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate payroll summary report by payment method and currency
 *
 * @author
 * @version $Id: SummaryPayrollByPaymentMethodReportAction.java  22-ene-2010 11:38:12$
 */
@Name("productionPlanningReportReproAction")
@Scope(ScopeType.CONVERSATION)
public class ProductionPlanningReportReproAction extends GenericReportAction {

    @In
    User currentUser;

    private String date;
    private String state;

    BaseProduct baseProduct;

    public void generateReportSummary(BaseProduct base) {

        baseProduct = base;
        log.debug("Generate ProductionPlannigReportReproAction........");
        String templatePath = "/production/reports/productionReproSummaryReport.jrxml";
        String fileName = "Orden_Produccion_Summary_Repro";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        date = sdf.format(baseProduct.getProductionPlanningBase().getDate());
        state = getEstate(baseProduct.getState());
        Map params = new HashMap();

        params.putAll(getCommonDocumentParamsInfo());
        setReportFormat(ReportFormat.PDF);
        addProductionOrderMaterialDetailSubReport(params);
        addProductionOrderMaterialSummaryDetailSubReport(params);
        super.generateSqlReport(
                fileName,
                templatePath,
                messages.get("ArticleReport.report.title"),
                params);

    }

    protected String getNativeSql() {
        String sql = "SELECT orderinput.cod_art as COD_INPUT,ia.DESCRI as NOM_INPUT, ia.COD_MED as UNIT_INPUT\n" +
                "                     , orderInput.CANTIDAD as CANT_INPUT, orderInput.COSTOUNITARIO as COST_UNIT_INPUT,orderInput.COSTOTOTAL as COST_TOTAL_INPUT \n" +
                "                      FROM productobase baseProduct \n" +
                "                      inner join ordeninsumo orderInput\n" +
                "                      on baseproduct.idproductobase = orderinput.idproductobase\n" +
                "                      inner join WISE.inv_articulos ia\n" +
                "                      on ia.cod_art = orderInput.cod_art\n" +
                "                      where baseProduct.idproductobase = "+baseProduct.getId().toString();

        return sql;
    }

    @Override
    protected String getEjbql()
    {

        String sql = " SELECT orderInput.productItemCode as COD_INPUT,orderInput.productItem.name as NOM_INPUT,orderInput.productItem.usageMeasureCode as UNIT_INPUT" +
                ", orderInput.amount as CANT_INPUT, orderInput.costUnit as COST_UNIT_INPUT,orderInput.costTotal as COST_TOTAL_INPUT " +
                " FROM BaseProduct baseProduct " +
                " inner join baseProduct.orderInputs orderInput";
        return sql;
    }

    @Create
    public void init() {
        restrictions = new String[]{"baseProduct=#{baseProduct}"};
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
        paramMap.put("nameProduct", "Reproceso");
        paramMap.put("numOrder", baseProduct.getCode());
        return paramMap;
    }

    private void addProductionOrderMaterialDetailSubReport(Map mainReportParams) {
        log.debug("Generating productionOrderMaterialDetailSubReport.............................");

        Map<String, Object> params = new HashMap<String, Object>();

        String sql = "select mp.nombre ,ia.descri, IA.COD_ART, om.cantidadpesosolicitada, om.cantidadpesousada, om.cantidadpesoretornada,om.COSTOUNITARIO ,om.COSTOTOTAL from productobase pb\n" +
                "inner join productosimple ps\n" +
                "on ps.idproductobase = pb.idproductobase\n" +
                "inner join PRODUCTOSIMPLEPROCESADO psp\n" +
                "on psp.idproductosimple = ps.idproductosimple\n" +
                "inner join metaproductoproduccion mp\n" +
                "on mp.idmetaproductoproduccion = psp.idmetaproductoproduccion\n" +
                "inner join ordenmaterial om \n" +
                "on om.idproductosimple = ps.idproductosimple\n" +
                "inner join WISE.inv_articulos ia\n" +
                "on ia.cod_art = om.cod_art\n" +
                "where pb.idproductobase = " + baseProduct.getId();

        String subReportKey = "ORDERMATERIALSUBREPORT";
        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/production/reports/productionOrderMaterialDetailSubReproReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                sql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());

    }

    private void addProductionOrderMaterialSummaryDetailSubReport(Map mainReportParams) {
        log.debug("Generating productionOrderMaterialDetailSubReport.............................");

        Map<String, Object> params = new HashMap<String, Object>();

        String sql = "select mp.nombre , ps.cantidad ,ps.costototalmateriales ,ps.costototalinsumos,ps.costototalmanoobra\n" +
                "      ,ps.costototalindirecto,ps.costototalproduccion,ps.costounitario  \n" +
                "from productobase pb\n" +
                "inner join productosimple ps\n" +
                "on ps.idproductobase = pb.idproductobase\n" +
                "inner join PRODUCTOSIMPLEPROCESADO psp\n" +
                "on psp.idproductosimple = ps.idproductosimple\n" +
                "inner join metaproductoproduccion mp\n" +
                "on mp.idmetaproductoproduccion = psp.idmetaproductoproduccion\n" +
                "where pb.idproductobase = "+ baseProduct.getId();

        //generate the sub report

        String subReportKey = "ORDERMATERIALSUMMARYSUBREPORT";
        TypedReportData subReportData = super.generateSqlSubReport(
                subReportKey,
                "/production/reports/productionOrderMaterialSummaryDetailReproSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                sql,
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());

    }

}