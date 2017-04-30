package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.production.ProductionPlanningAction;
import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.production.*;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.reports.GenerationReportData;
import com.encens.khipus.service.production.ProductionPlanningService;
import com.encens.khipus.service.warehouse.ProductItemService;
import com.encens.khipus.service.warehouse.WarehouseService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.RoundUtil;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRTemplatePrintText;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.springframework.core.CollectionFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("summaryMaterialAndInputByDayReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('GENERATEREQUESTBYPLANNIG','VIEW')}")
public class SummaryMaterialAndInputByDayReportAction extends GenericReportAction {
    private ProductionPlanning planning;
    private List<ProductionOrder> productionOrders = new ArrayList<ProductionOrder>();
    private List<BaseProduct> baseProducts = new ArrayList<BaseProduct>();
    private List<SingleProduct> singleProducts = new ArrayList<SingleProduct>();
    private String ordersCodes = "";
    private String codMilk = Constants.ID_ART_RAW_MILK;
    private String codWater = Constants.COD_ART_WATER;
    public void generateReport(ProductionPlanning productionPlanning) {
        log.debug("Generating IncomeByInvoiceReportAction............................");
        planning =  productionPlanning;
        for(ProductionOrder order: productionPlanning.getProductionOrderList())
        {
            if(order.getSelected())
            productionOrders.add(order);
            ordersCodes += order.getCode()+";";
        }
        for(BaseProduct base: productionPlanning.getBaseProducts())
        {
            if(base.getSelected())
            {
                baseProducts.add(base);
                ordersCodes += base.getCode()+";";
                for(SingleProduct single:base.getSingleProducts())
                {
                    singleProducts.add(single);
                }
            }
        }

        if(productionOrders.size() == 0){
            ProductionOrder aux = new ProductionOrder();
            aux.setId(new Long(0));
            productionOrders.add(aux);
        }
        if(baseProducts.size() == 0){
            BaseProduct aux = new BaseProduct();
            aux.setId(new Long(0));
            baseProducts.add(aux);
        }
        if(singleProducts.size() == 0){
            SingleProduct aux = new SingleProduct();
            aux.setId(new Long(0));
            singleProducts.add(aux);
        }

        TypedReportData reportInPuts;
        TypedReportData reportMaterial;
        //add sub reports
        setReportFormat(ReportFormat.PDF);
        setPageFormat(PageFormat.LETTER);

        reportMaterial = getSummaryMaterialSubReport();
        reportInPuts = getSummaryInputSubReport();
        for (Object jrPrintPage : reportInPuts.getJasperPrint().getPages()) {
            reportMaterial.getJasperPrint().addPage((JRPrintPage) jrPrintPage);
        }

        try {
            reportMaterial.getReportConfigParams().setReportPageOrientation("Vertical");
            GenerationReportData generationReportData = new GenerationReportData(reportMaterial);
            generationReportData.exportReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypedReportData getSummaryInputSubReport() {
        log.debug("Generating addSummaryMaterialSubReport.............................!!!!!!!");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("FECHA",planning.getDate());
        params.put("CODIGOS",ordersCodes);
        String subReportKey = "ORDERINPUTSUBREPORT";

        String ejbql = " SELECT orderInput.productItemCode " +
                " ,orderInput.productItem.name " +
                " ,orderInput.productItem.usageMeasureCode " +
                " ,sum(orderInput.amount)  " +
                " from OrderInput orderInput " +
                " where orderInput.productionOrder in (#{summaryMaterialAndInputByDayReportAction.productionOrders}) " +
                " and orderInput.productItemCode <> #{summaryMaterialAndInputByDayReportAction.codMilk}" +
                " and orderInput.productItemCode <> #{summaryMaterialAndInputByDayReportAction.codWater}" +
                " OR orderInput.baseProductInput in (#{summaryMaterialAndInputByDayReportAction.baseProducts})";
                /*" left join orderInput.productionOrder productionOrder" +
                " left join orderInput.baseProductInput baseProductInput" ;*/


        /*String[] restrictions = new String[]{
                "orderInput.productionOrder in (#{summaryMaterialAndInputByDayReportAction.productionOrders})",
                "orderInput.baseProductInput in (#{summaryMaterialAndInputByDayReportAction.baseProducts})"
        };*/
        String[] restrictions = new String[]{};

        String materialsSubReportOrderBy = "orderInput.productItem.name";

        String materialsSubReportGroupBy = "orderInput.productItemCode, orderInput.productItem.name ,orderInput.productItem.usageMeasureCode";

        //generate the sub report
        TypedReportData materialReport = super.getReport(
                subReportKey,
                "/production/reports/requestInputByPlannig.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), materialsSubReportOrderBy,materialsSubReportGroupBy),
                messages.get("ProductionPlanning.report.summaryInputByPlannig"),
                params);

        return materialReport;

    }

    private TypedReportData getSummaryMaterialSubReport() {
        log.debug("Generating addSummaryMaterialSubReport.............................!!!!!!!");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("FECHA",planning.getDate());
        params.put("CODIGOS",ordersCodes);
        String subReportKey = "ORDERMATERIALSUBREPORT";

        String ejbql = " SELECT orderMaterial.productItemCode " +
                       " ,orderMaterial.productItem.name " +
                       " ,orderMaterial.productItem.usageMeasureCode " +
                       " ,sum(orderMaterial.amountRequired)  " +
                       " from OrderMaterial orderMaterial " +
                       " where orderMaterial.productionOrder in (#{summaryMaterialAndInputByDayReportAction.productionOrders})" +
                       " OR orderMaterial.singleProduct in (#{summaryMaterialAndInputByDayReportAction.singleProducts})";/*+
                       " left join orderMaterial.productionOrder productionOrder" +
                       " left join orderMaterial.singleProduct singleProduct" ;*/


        /*String[] restrictions = new String[]{
                 "orderMaterial.productionOrder in (#{summaryMaterialAndInputByDayReportAction.productionOrders})"
                ,"orderMaterial.singleProduct in (#{summaryMaterialAndInputByDayReportAction.singleProducts})"
        };
*/
        String[] restrictions = new String[]{};
        String materialsSubReportOrderBy = "orderMaterial.productItem.name";

        String materialsSubReportGroupBy = "orderMaterial.productItemCode, orderMaterial.productItem.name ,orderMaterial.productItem.usageMeasureCode";

        //generate the sub report
        TypedReportData materialReport = super.getReport(
                subReportKey,
                "/production/reports/requestMaterialByPlannig.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), materialsSubReportOrderBy,materialsSubReportGroupBy),
                messages.get("ProductionPlanning.report.summaryMaterialByPlannig"),
                params);

        return materialReport;

    }

    public ProductionPlanning getPlanning() {
        return planning;
    }

    public void setPlanning(ProductionPlanning planning) {
        this.planning = planning;
    }

    public List<ProductionOrder> getProductionOrders() {
        return productionOrders;
    }

    public void setProductionOrders(List<ProductionOrder> productionOrders) {
        this.productionOrders = productionOrders;
    }

    public List<BaseProduct> getBaseProducts() {
        return baseProducts;
    }

    public void setBaseProducts(List<BaseProduct> baseProducts) {
        this.baseProducts = baseProducts;
    }

    public List<SingleProduct> getSingleProducts() {
        return singleProducts;
    }

    public void setSingleProducts(List<SingleProduct> singleProducts) {
        this.singleProducts = singleProducts;
    }

    public String getOrdersCodes() {
        return ordersCodes;
    }

    public void setOrdersCodes(String ordersCodes) {
        this.ordersCodes = ordersCodes;
    }

    public String getCodMilk() {
        return codMilk;
    }

    public void setCodMilk(String codMilk) {
        this.codMilk = codMilk;
    }

    public String getCodWater() {
        return codWater;
    }

    public void setCodWater(String codWater) {
        this.codWater = codWater;
    }
}
