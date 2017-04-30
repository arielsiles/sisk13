package com.encens.khipus.action.production.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.Gestion;
import com.encens.khipus.model.employees.GestionPayroll;
import com.encens.khipus.model.employees.Month;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.Periodo;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialPayRoll;
import com.encens.khipus.service.production.ProductiveZoneService;
import com.encens.khipus.service.production.RawMaterialPayRollService;
import com.encens.khipus.service.production.RawMaterialPayRollServiceBean;
import com.encens.khipus.util.MessageUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
@Name("rawMaterialCollectedByGABReportAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialCollectedByGABReportAction extends GenericReportAction {
    @In
    RawMaterialPayRollService rawMaterialPayRollService;

    @In
    ProductiveZoneService productiveZoneService;

    private String summaryReportTitle;
    private String gestionTitle;

    private Gestion gestion;
    private Month month;
    private Periodo periodo;
    private ProductiveZone zone;
    private MetaProduct metaProduct;
    private double unitPrice;
    private double mountCollection;
    private double totalCollectionXUnitPrice;
    private double differences;
    private double weightReal;
    private double totalWeightRealXUnitPrice;
    private double yogurt;
    private double veterinari;
    private double credit;
    private double recipient;
    private RawMaterialPayRollServiceBean.SummaryTotal summaryTotal;
    private RawMaterialPayRollServiceBean.Discounts discounts;

    private Calendar dateIni;
    private Calendar dateEnd;

    private List<GestionPayroll> gestionPayrollList;


    private GeneratedPayrollType generatedPayrollType = GeneratedPayrollType.OFFICIAL;


    public void generateReport() {
        log.debug("Generate RotatoryFundReportAction........");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        dateIni = Calendar.getInstance();
        dateEnd = Calendar.getInstance();
        dateIni.set(gestion.getYear(),month.getValue(),periodo.getInitDay());
        dateEnd.set(gestion.getYear(),month.getValue(),periodo.getEndDay(month.getValue()+1,gestion.getYear()));
        sdf.setCalendar(dateIni);
        sdf.setCalendar(dateEnd);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");



        Map params = new HashMap();

        params.put("title",messages.get("Report.titleGeneral"));
        params.put("periodo",(periodo.getResourceKey().toString()== "Periodo.first") ?"1RA QUINCENA":"2DA QUINCENA" +" "+getMes(month));
        params.put("startDate",df.format(dateIni.getTime()));
        params.put("endDate",df.format(dateEnd.getTime()));
        params.put("nombre_gab","GAB: "+zone.getNumber()+" - "+zone.getName());


        int cont = periodo.getInitDay();
        for(int i = periodo.getInitDay(); i<=periodo.getEndDay(month.getValue()+1,gestion.getYear());i++)
        {

            params.put("DAY"+cont,"D"+i);
            cont ++;
        }

        if(cont < 31 && (periodo.getResourceKey().toString()== "Periodo.first") ?false:true)
        {
            for(int i = cont; i<=31;i++)
            {
                cont = 16;
                params.put("DAY"+cont,"D"+i);
                cont ++;
            }
        }

        params.put("dateStart","Fecha Inicio - " + FastDateFormat.getInstance("dd-MM-yyyy").format(dateIni));
        params.put("dateEnd","Fecha Fin - "+ FastDateFormat.getInstance("dd-MM-yyyy").format(dateEnd));
        super.generateSqlReport("rotatoryFundReport", "/production/reports/rawMaterialCollectedByGABReport.jrxml", MessageUtils.getMessage("Report.rawMaterialPayRollReportAction"), params);

    }

    @Override
    protected String getNativeSql()
    {

        int initDay = periodo.getInitDay();
        int endDay = periodo.getEndDay(month.getValue()+1,gestion.getYear());
        int cont = 1;
        String sql =" select \n"+
                " A"+cont+".productor as productor \n";
        String total = "";
        for(int i = initDay; i<=endDay;i++)
        {
            sql += "      , A"+cont+".CANTIDAD AS D"+cont+"\n";
            total += ((i==initDay)?",":"+")+" A"+cont+".CANTIDAD";
            cont ++;
        }
        if(cont < 16 && (periodo.getResourceKey().toString()== "Periodo.first") ?false:true)
        {
            for(int i = cont; i<=16;i++)
            {
                sql += "      , 0.0 AS D"+cont+"\n";
                cont ++;
            }
        }
        sql += total+" AS TOTAL \n";
        sql += " from \n";
        cont = 1;
        int month_act = (month.getValue())+1;
        for(int i = initDay; i<= endDay; i++)
        {
            sql +=  "   (select zp.numero , zp.nombre, am.cantidad , pe.nombres||' '||pe.apellidopaterno ||' '||pe.apellidomaterno as productor, am.IDPRODUCTORMATERIAPRIMA\n" +
                    "  from sesionacopio sa\n" +
                    "                              inner join acopiomateriaprima am\n" +
                    "                              on am.idsesionacopio = sa.idsesionacopio\n" +
                    "                              inner join persona pe \n" +
                    "                              on am.IDPRODUCTORMATERIAPRIMA=pe.idpersona \n" +
                    "                              inner join zonaproductiva zp\n" +
                    "                              on zp.idzonaproductiva = sa.idzonaproductiva\n" +
                    "                              where sa.fecha = to_date('"+i+"/"+month_act+"/"+gestion.getYear()+"','dd/mm/yyyy') \n" +
                    "                              AND zp.idzonaproductiva = "+zone.getId().toString()+"\n" +
                    "      ) A"+cont+( (i != endDay)?",\n":"\n");
            cont++;
        }
        cont = 2;

        for(int i = initDay; i<endDay;i++)
        {
            sql += ((cont == 2)?" WHERE A":" and A") +1+".IDPRODUCTORMATERIAPRIMA = A"+cont+".IDPRODUCTORMATERIAPRIMA\n";
            cont ++;
        }

        return sql;
    }

    private String getMes(Month month)
    {
        String result = "";
        if(Month.JANUARY == month)
            result = "Enero";
        if(Month.FEBRUARY == month)
            result = "Febrero";
        if(Month.MARCH == month)
            result = "Marzo";
        if(Month.APRIL == month)
            result = "Abril";
        if(Month.MAY == month)
            result = "Mayo";
        if(Month.JUNE == month)
            result = "Junio";
        if(Month.JULY == month)
            result = "Julio";
        if(Month.AUGUST == month)
            result = "Agosto";
        if(Month.SEPTEMBER == month)
            result = "Septiembre";
        if(Month.OCTOBER == month)
            result = "Octubre";
        if(Month.NOVEMBER == month)
            result = "Noviembre";
        if(Month.DECEMBER == month)
            result = "Diciembre";

        return result;
    }

    @Create
    public void init() {
        restrictions = new String[]{"rawMaterialPayRoll.productiveZone = #{rawMaterialPayRollReportAction.zone}",
                "rawMaterialPayRoll.metaProduct = #{rawMaterialPayRollReportAction.metaProduct}"};
        sortProperty = "rawMaterialProducer.firstName";
    }

    private void getTotal(RawMaterialPayRoll rawMaterialPayRoll)
    {
        //rawMaterialPayRoll.
    }

    public RawMaterialPayRollService getRawMaterialPayRollService() {
        return rawMaterialPayRollService;
    }

    public void setRawMaterialPayRollService(RawMaterialPayRollService rawMaterialPayRollService) {
        this.rawMaterialPayRollService = rawMaterialPayRollService;
    }

    public String getSummaryReportTitle() {
        return summaryReportTitle;
    }

    public void setSummaryReportTitle(String summaryReportTitle) {
        this.summaryReportTitle = summaryReportTitle;
    }

    public String getGestionTitle() {
        return gestionTitle;
    }

    public void setGestionTitle(String gestionTitle) {
        this.gestionTitle = gestionTitle;
    }

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public List<GestionPayroll> getGestionPayrollList() {
        return gestionPayrollList;
    }

    public void setGestionPayrollList(List<GestionPayroll> gestionPayrollList) {
        this.gestionPayrollList = gestionPayrollList;
    }

    public void cleanGestionList() {
        setGestionPayrollList(null);
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Periodo periodo) {
        this.periodo = periodo;
    }

    public ProductiveZone getZone() {
        return zone;
    }

    public void setZone(ProductiveZone zone) {
        this.zone = zone;
    }

    @Factory(value = "monthEnumCollectedByGAB")
    public Month[] getMonthEnum() {
        return Month.values();
    }

    @Factory(value = "periodosCollectedByGAB", scope = ScopeType.STATELESS)
    public Periodo[] getPeriodos() {
        return Periodo.values();
    }
    /*
    public void selectProductiveZone(ProductiveZone productiveZone) {
        try {
            productiveZone = getService().findById(ProductiveZone.class, productiveZone.getId());
            getInstance().setProductiveZone(productiveZone);
        } catch (Exception ex) {
            log.error("Caught Error", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }
    */

    public GeneratedPayrollType getGeneratedPayrollType() {
        return generatedPayrollType;
    }

    public void setGeneratedPayrollType(GeneratedPayrollType generatedPayrollType) {
        this.generatedPayrollType = generatedPayrollType;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public Calendar getDateIni() {
        return dateIni;
    }

    public void setDateIni(Calendar dateIni) {
        this.dateIni = dateIni;
    }

    public Calendar getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Calendar dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getFullNameOfProductiveZone() {
        return (zone == null ? "" : zone.getFullName());
    }

    public void setFullNameOfProductiveZone(String fullName) {

    }

    protected GenericService getService() {
        return rawMaterialPayRollService;
    }

    public void selectProductiveZone(ProductiveZone productiveZone) {
        try {
            productiveZone = getService().findById(ProductiveZone.class, productiveZone.getId());
            setZone(productiveZone);
        } catch (Exception ex) {
            log.error("Caught Error", ex);
        }
    }


}