package com.encens.khipus.action.billing;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.employees.GeneratedPayroll;
import com.encens.khipus.model.employees.ManagersPayroll;
import com.encens.khipus.model.finances.BankAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.PaymentType;
import com.encens.khipus.service.employees.ManagersPayrollService;
import com.encens.khipus.service.employees.ManagersPayrollSummaryService;
import com.encens.khipus.service.employees.PayrollReportService;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.JSFUtil;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
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
import java.math.BigDecimal;
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


    public void generateReportXml(CustomerOrder customerOrder) throws CompanyConfigurationNotFoundException {


        String input = customerOrder.getMovement().getFactura();

        // decode the encoded data
        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(input));
        System.out.println("Decoded Data: " + decoded);

        String line1 = "";
        String line2 = decoded;

        Collection<CollectionData> beanCollection = new ArrayList();

        beanCollection.add(new CollectionData(line1));
        beanCollection.add(new CollectionData(line2));


        Map<String, Object> params = new HashMap<String, Object>();
        params.put("line1", line1);
        params.put("line2", line2);

        try{
            File jasper = new File(JSFUtil.getRealPath("/employees/reports/payrollBankUnisueldoReport.jasper"));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), params, new JRBeanCollectionDataSource(beanCollection));
            exportarXML(jasperPrint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void exportarXML(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=factura.xml");
        ServletOutputStream stream = response.getOutputStream();

        //JRXlsxExporter exporter = new JRXlsxExporter();
        //exporter.setParameter(JRTextExporterParameter.JASPER_PRINT, jasperPrint);
        //exporter.setParameter(JRTextExporterParameter.OUTPUT_STREAM, stream);
        //exporter.exportReport();


        JRXmlExporter exporter = new JRXmlExporter();

        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);

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
        return "";
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
