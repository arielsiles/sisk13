package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.action.reports.ReportFormat;
import com.encens.khipus.model.production.Periodo;
import com.encens.khipus.model.production.RawMaterialPayRecord;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.util.RoundUtil;
import org.jboss.beans.metadata.api.annotations.Create;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import sun.util.calendar.BaseCalendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 9/09/13
 * Time: 12:48
 * To change this template use File | Settings | File Templates.
 */
@Name("rawMaterialPayRecordReportAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialPayRecordReportAction extends GenericReportAction {
    private RawMaterialPayRecord rawMaterialPayRecord;
    private RawMaterialProducer producer;
    private String nameProducer;
    private String gab;
    private String totalCollection;
    private String prizeUnit;
    private String concept;
    private String period;
    private String ci;
    private BaseCalendar.Date birthDay;
    private String birthPlace;
    private String isRetention;
    private Periodo periodo;

    public void generateReport(RawMaterialPayRecord rawMaterialPay){

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();
        rawMaterialPayRecord = rawMaterialPay;
        //periodo = new Periodo(rawMaterialPayRecord.getRawMaterialPayRoll().getStartDate().getDay(),rawMaterialPayRecord.getRawMaterialPayRoll().getEndDate().getDay());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        log.debug("generating RawMaterialPayRecordReportAction......................................");
        producer = rawMaterialPayRecord.getRawMaterialProducerDiscount().getRawMaterialProducer();
        setReportFormat(ReportFormat.PDF);
        reportParameters.put("gab", producer.getProductiveZone().getFullName());
        reportParameters.put("prizeUnit", ((Double) (rawMaterialPayRecord.getRawMaterialPayRoll().getUnitPrice())).toString());
        reportParameters.put("concept",messages.get("Report.rawMaterialPayRecordReportAction.concept") + getPeriodo(rawMaterialPayRecord.getRawMaterialPayRoll().getStartDate().getDay()));

        Date dateIni = rawMaterialPayRecord.getRawMaterialPayRoll().getStartDate();
        Date dateEnd = rawMaterialPayRecord.getRawMaterialPayRoll().getStartDate();
        sdf.format(dateIni);
        sdf.format(dateEnd);
        reportParameters.put("period",dateIni.toString()+" - "+dateEnd.toString());
        reportParameters.put("ci",producer.getIdNumber().toString());
        reportParameters.put("birthDay",producer.getBirthDay().toString());
        reportParameters.put("isRetention",producer.getExpirationDateTaxLicence() != null ? "SI" : "NO" );
        reportParameters.put("name_producer",producer.getFullName());
        reportParameters.put("totalCollection",((Double)(rawMaterialPayRecord.getTotalAmount())).toString());
        /////////
        //todo: implementar el total al momento de generar la planilla
        Double totalPay = RoundUtil.getRoundValue(rawMaterialPayRecord.getTotalAmount() * rawMaterialPayRecord.getRawMaterialPayRoll().getUnitPrice(),2, RoundUtil.RoundMode.SYMMETRIC);

        reportParameters.put("totalPay", totalPay);
        reportParameters.put("otherIncome", ((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getOtherDiscount())).toString());
        Double totalDiscount = rawMaterialPayRecord.getRawMaterialProducerDiscount().getYogurt() +
                rawMaterialPayRecord.getRawMaterialProducerDiscount().getVeterinary()+
                rawMaterialPayRecord.getRawMaterialProducerDiscount().getCredit()+
                rawMaterialPayRecord.getRawMaterialProducerDiscount().getCans()+
                rawMaterialPayRecord.getRawMaterialProducerDiscount().getWithholdingTax()+
                rawMaterialPayRecord.getRawMaterialProducerDiscount().getOtherDiscount();
        Double totalIncomePay = rawMaterialPayRecord.getRawMaterialProducerDiscount().getOtherIncoming() + totalPay;
        reportParameters.put("alcohol", ((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getAlcohol())).toString());
        reportParameters.put("concentrated",((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getConcentrated())).toString());
        reportParameters.put("yogurt",((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getYogurt())).toString());
        reportParameters.put("veterinary",((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getVeterinary())).toString());
        reportParameters.put("credit",((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getCredit())).toString());
        reportParameters.put("cans",((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getCans())).toString());
        reportParameters.put("retention",((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getWithholdingTax())).toString());
        reportParameters.put("discounts",((Double)(rawMaterialPayRecord.getRawMaterialProducerDiscount().getOtherDiscount())).toString());
        reportParameters.put("productiveZoneAdjustment",((Double)(rawMaterialPayRecord.getProductiveZoneAdjustment())).toString());
        reportParameters.put("totalDiscounts",totalDiscount.toString());
        reportParameters.put("totalIncomePay",totalIncomePay.toString());
        reportParameters.put("totalLiquid",((Double)(rawMaterialPayRecord.getLiquidPayable())).toString());
        reportParameters.put("totalPay",((Double)(rawMaterialPayRecord.getLiquidPayable())).toString());


        super.generateReport(
                "RawMaterialPayRecordReportAction",
                "/production/reports/rawMaterialPayRecordReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Report.rawMaterialPayRecordReportAction"),
                reportParameters);
    }

    private String getPeriodo(int initDay)
    {
        if(initDay >15)
            return messages.get("Periodo.second");
        else
            return messages.get("Periodo.first");
    }

    @Override
    protected String getEjbql()
    {
        return "";
    }

    @Create
    protected void init()
    {

    }

    public RawMaterialProducer getProducer() {
        return producer;
    }

    public void setProducer(RawMaterialProducer producer) {
        this.producer = producer;
    }

    public String getNameProducer() {
        return nameProducer;
    }

    public void setNameProducer(String nameProducer) {
        this.nameProducer = nameProducer;
    }
}
