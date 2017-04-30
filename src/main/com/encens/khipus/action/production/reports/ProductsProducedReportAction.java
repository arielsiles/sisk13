package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("productsProducedReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTPRODUCTPRODUCED','VIEW')}")
public class ProductsProducedReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;

    @Create
    public void init() {

        restrictions = new String[]{
               //  "productionPlanning.date between #{productsProducedReportAction.startDate} and #{productsProducedReportAction.endDate}"
        };
        //sortProperty = "name";
    }

    @Override
    protected String getEjbql() {
        //DateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //For Oracle
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String start = df.format(startDate);
        String end = df.format(endDate);

        //For Oracle
        /*return  " SELECT distinct productionOrder.productComposition.processedProduct.productItem.productItemCode as productItemCode " +
                      " ,productionOrder.productComposition.processedProduct.productItem.name as name" +
                " FROM  ProductionPlanning productionPlanning " +
                " inner join productionPlanning.productionOrderList productionOrder " +
                " where productionPlanning.date between to_date('"+start+"','dd/mm/yyyy') and to_date('"+end+"','dd/mm/yyyy')" +
                " union " +
                " SELECT distinct singleProduct.productProcessingSingle.metaProduct.productItem.productItemCode as productItemCode " +
                " ,singleProduct.productProcessingSingle.metaProduct.productItem.name as name " +
                " FROM ProductionPlanning productionPlanning " +
                " inner join productionPlanning.baseProducts baseProduct " +
                " inner join baseProduct.singleProducts singleProduct " +
                " where productionPlanning.date between to_date('"+start+"','dd/mm/yyyy') and to_date('"+end+"','dd/mm/yyyy')";*/

        return  " SELECT distinct productionOrder.productComposition.processedProduct.productItem.productItemCode as productItemCode " +
                " ,productionOrder.productComposition.processedProduct.productItem.name as name" +
                " FROM  ProductionPlanning productionPlanning " +
                " inner join productionPlanning.productionOrderList productionOrder " +
                " where productionPlanning.date between '"+start+"' and '"+end+"'"; /*+
                " union " +
                " SELECT distinct singleProduct.productProcessingSingle.metaProduct.productItem.productItemCode as productItemCode " +
                " ,singleProduct.productProcessingSingle.metaProduct.productItem.name as name " +
                " FROM ProductionPlanning productionPlanning " +
                " inner join productionPlanning.baseProducts baseProduct " +
                " inner join baseProduct.singleProducts singleProduct " +
                " where productionPlanning.date between '"+start+"' and '"+end+"'";*/
    }

    public void generateReport() {
        log.debug("Generating products produced report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        reportParameters.put("startDate",startDate);
        reportParameters.put("endDate",endDate);
        super.generateReport(
                "productsProdecedReport",
                "/production/reports/productsProducedReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("production.productsProduced.TitleReport"),
                reportParameters);
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

}
