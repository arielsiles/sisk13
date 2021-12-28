package com.encens.khipus.action.billing;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: PayrollBankReportAction.java  28-ene-2010 15:55:38$
 */
@Name("billReportAction")
@Scope(ScopeType.PAGE)
public class BillReportAction extends GenericReportAction {


    public void generateFileXML(CustomerOrder customerOrder) throws IOException {

        String input = customerOrder.getMovement().getFactura();
        // decode the encoded data
        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(input));

        System.out.println("Decoded Data: " + decoded);

        FileWriter archivo = null;
        PrintWriter escritor = null;

        try {
            archivo = new FileWriter("D:\\factura.xml");
            escritor = new PrintWriter(archivo);
            escritor.print(decoded);
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage() );
        }finally {
            archivo.close();
        }

    }


    /*
    private Long customerId;

    public void generateReport(CustomerOrder customerOrder) {
        this.setCustomerId(customerOrder.getId());
        log.debug("Generate Invoice XML......");

        String input = customerOrder.getMovement().getFactura();
        // decode the encoded data
        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(input));

        System.out.println("Decoded Data: " + decoded);

        HashMap<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put("line1", "Linea 1");
        reportParameters.put("line2", "Linea 2");
        reportParameters.put("data", decoded);

        String fileReport = "/customers/reports/billingReport.jrxml";
        setReportFormat(ReportFormat.PDF);
        super.generateReport(
                "billingReport",
                fileReport,
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                "Archivo-XML",
                reportParameters);
    }

    @Override
    protected String getEjbql() {
        return " SELECT customerOrder " +
                "  FROM  CustomerOrder customerOrder ";
    }

    @Create
    public void init() {
        restrictions = new String[]{
                "customerOrder.id=#{billReportAction.customerId}"
        };
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }*/


    public void generateReport(CustomerOrder customerOrder) {

        String input = customerOrder.getMovement().getFactura();

        // decode the encoded data
        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(input));
        System.out.println("Decoded Data: " + decoded);

        String line1 = "";
        String line2 = "";
        String datas = decoded;

        Collection<CollectionData> beanCollection = new ArrayList();

        beanCollection.add(new CollectionData(line1));
        beanCollection.add(new CollectionData(line2));
        beanCollection.add(new CollectionData(datas));

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("line1", line1);
        params.put("line2", line2);
        params.put("data", datas);

        try{
            File jasper = new File(JSFUtil.getRealPath("/customers/reports/billingReport.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), params, new JRBeanCollectionDataSource(beanCollection));

            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            response.addHeader("Content-disposition", "attachment; filename=factura.pdf");
            ServletOutputStream stream = response.getOutputStream();

            JRExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
            exporter.setParameter(JRExporterParameter.OUTPUT_FILE, new java.io.File("invoice.pdf"));
            exporter.exportReport();


            //exportarXML(jasperPrint, datas);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void exportarXML(JasperPrint jasperPrint, String datas) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=factura.txt");
        ServletOutputStream stream = response.getOutputStream();

        /*JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setParameter(JRTextExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRTextExporterParameter.OUTPUT_STREAM, stream);
        exporter.exportReport();*/

        /*JRXmlExporter exporter = new JRXmlExporter();
        exporter.setParameter(JRXmlExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRXmlExporterParameter.OUTPUT_STREAM, stream);
        exporter.exportReport();*/

        JRTextExporter exporter = new JRTextExporter();
        exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, 80);
        exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, 40);
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, "outputFileName.txt");
        exporter.exportReport();

        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
    }


    @Create
    public void init() {
        restrictions = new String[]{};
    }

    @Override
    protected String getEjbql() {
        return " ";
    }

    public class CollectionData{

        private String register;

        public CollectionData(String register){
            this.setRegister(register);
        }

        public String getRegister() {
            return register;
        }

        public void setRegister(String register) {
            this.register = register;
        }
    }

}
