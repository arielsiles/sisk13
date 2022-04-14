package com.encens.khipus.action.customers.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.customers.SaleStatus;
import com.encens.khipus.model.customers.SaleTypeEnum;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.customers.CustomerOrderService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the purchaseOrder report action
 *
 * @author
 * @version 3.0
 */
@Name("salesCustomersReportAction")
@Scope(ScopeType.PAGE)
public class SalesCustomersReportAction extends GenericReportAction {

    private Date startDate;
    private Date endDate;
    private SaleTypeEnum saleType;
    private Warehouse warehouse;

    private SaleStatus annuledState = SaleStatus.ANULADO;

    @In
    private CustomerOrderService customerOrderService;

    @Create
    public void init() {
        restrictions = new String[]{};
    }


    protected String getEjbql() {
        String ejbql = "";
        return ejbql;
    }


    public void generateReport() {

        String documentTitle = "VENTAS POR CLIENTE";
        String period = DateUtils.format(startDate, "dd/MM/yyyy") + " - " + DateUtils.format(endDate, "dd/MM/yyyy");

        Collection<CollectionData> beanCollection = new ArrayList();
        beanCollection = calculateReport();

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("documentTitle", documentTitle);
        reportParameters.put("period", period);
        reportParameters.put("warehouse", warehouse.getName());
        reportParameters.put("saleType", MessageUtils.getMessage(saleType.getResourceKey()));

        try{
            File jasper = new File(JSFUtil.getRealPath("/customers/reports/salesCustomerReport.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), reportParameters, new JRBeanCollectionDataSource(beanCollection));
            exportarExcel(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Collection<CollectionData> calculateReport(){

        Collection<CollectionData> beanCollection = new ArrayList();
        List<Object[]> objects = customerOrderService.getSalesCustomer(startDate, endDate, saleType, warehouse);

        for ( Object[] value : objects){
            CollectionData data = new CollectionData((Long)value[0], (String)value[1], (String)value[2], (String) value[3], (Double) value[4], (Long) value[5]);
            beanCollection.add(data);
        }
        return beanCollection;
    }

    public void exportarExcel(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=VENTASxCLIENTE.xlsx");
        ServletOutputStream stream = response.getOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setParameter(JRTextExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRTextExporterParameter.OUTPUT_STREAM, stream);
        //exporter.setParameter(JRXlsExporterParameter.OUTPUT_FILE_NAME, "ReporteVentas01.xlsx");
        exporter.exportReport();
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SaleStatus getAnnuledState() {
        return annuledState;
    }

    public void setAnnuledState(SaleStatus annuledState) {
        this.annuledState = annuledState;
    }

    public SaleTypeEnum getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleTypeEnum saleType) {
        this.saleType = saleType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public class CollectionData{

        private Long clientId;
        private String doc;
        private String name;
        private String zone;
        private Double amount;
        private Long quantity;

        public CollectionData(Long clientId, String doc, String name, String zone, Double amount, Long quantity) {
            this.clientId = clientId;
            this.doc = doc;
            this.name = name;
            this.zone = zone;
            this.amount = amount;
            this.quantity = quantity;
        }

        public String getDoc() {
            return doc;
        }

        public void setDoc(String doc) {
            this.doc = doc;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }

        public Long getClientId() {
            return clientId;
        }

        public void setClientId(Long clientId) {
            this.clientId = clientId;
        }
    }
}