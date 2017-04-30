package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.production.CollectionForm;
import com.encens.khipus.service.production.CollectionFormService;
import com.encens.khipus.service.production.ProductionPlanningService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

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

@Name("productionBalanceReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('REPORTPRODUCCTIONBALANCE','VIEW')}")
public class ProductionBalanceReportAction extends GenericReportAction {

    private Date date;

    @In("CollectionFormService")
    private CollectionFormService collectionFormService;

    @In
    protected FacesMessages facesMessages;

    @Create
    public void init() {

        restrictions = new String[]{
                 //"productionPlanning.date = #{productionBalanceReportAction.date}"
        };
        //sortProperty = "productionOrder.productComposition.processedProduct.productItem.name";
    }

    @Override
    protected String getEjbql() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String dateBalance = df.format(date);

        return  " SELECT productionOrder.productComposition.processedProduct.productItem.productItemCode as productItemCode " +
                      " ,productionOrder.productComposition.processedProduct.productItem.name as name " +
                      " ,productionOrder.productComposition.processedProduct.unidMeasure as measureUnit " +
                " FROM  ProductionPlanning productionPlanning " +
                " inner join productionPlanning.productionOrderList productionOrder " +
                " where productionPlanning.date = to_date('"+dateBalance+"','dd/mm/yyyy') ";/* +
                " union " +
                " SELECT singleProduct.productProcessingSingle.metaProduct.productItem.productItemCode as productItemCode " +
                " ,singleProduct.productProcessingSingle.metaProduct.productItem.name as name " +
                " ,singleProduct.productProcessingSingle.metaProduct.productItem.usageMeasureUnit.measureUnitCode as measureUnit " +
                " FROM ProductionPlanning productionPlanning " +
                " inner join productionPlanning.baseProducts baseProduct " +
                " inner join baseProduct.singleProducts singleProduct " +
                " where productionPlanning.date = to_date('"+dateBalance+"','dd/mm/yyyy')";*/

    }

    public void generateReport() {
        log.debug("Generating production balance report...................");
        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        CollectionForm collectionForm = collectionFormService.finCollectionFormByDate(date);
        if(collectionForm == null)
        {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "Reports.rotatoryFundBalancesDetail.NotFoundMessage");
            return;
        }

        reportParameters.put("totalCollected",collectionForm.getTotalWeighed());
        reportParameters.put("totalSNG",collectionForm.getGreasePercentage());
        Double total = collectionForm.getTotalWeighed() *(collectionForm.getGreasePercentage() / 100);
        reportParameters.put("total",total);
        reportParameters.put("date",date);
        super.generateReport(
                "productsProdecedReport",
                "/production/reports/productionBalanceReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("production.productionBalance.TitleReport"),
                reportParameters);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
