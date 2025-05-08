package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.service.xproduction.XProductionService;
import com.encens.khipus.util.JSFUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Encens S.R.L.
 * This class implements the valued warehouse residue report action
 *
 * @author
 * @version 2.3
 */

@Name("productionInputsReportAction")
@Scope(ScopeType.PAGE)
public class ProductionInputsReportAction {

    private Date startDate;
    private Date endDate;

    @In
    private XProductionService xproductionService;
    @In
    private CompanyConfigurationService companyConfigurationService;
    @In
    private FacesMessages facesMessages;

    public void generateReport() {

        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"CompanyConfiguration.notFound");}

        try {
            List<Object[]> datos = xproductionService.findProductionInputsByDates(startDate, endDate);

            // Convertir Object[] a una lista de objetos JavaBeans (recomendado)
            List<ProductionInputDTO> reportData = new ArrayList<>();
            for (Object[] fila : datos) {
                ProductionInputDTO dto = new ProductionInputDTO();
                dto.setAlmacen((String) fila[0]);
                dto.setCodigo((String) fila[1]);
                dto.setNombre((String) fila[2]);
                dto.setUnidad((String) fila[3]);
                dto.setCantidad((BigDecimal) fila[4]);
                reportData.add(dto);
            }

            // Parámetros del reporte
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "INSUMOS UTILIZADOS EN LA PRODUCCIÓN");
            parameters.put("companyName", companyConfiguration.getCompanyName());
            parameters.put("systemName", companyConfiguration.getSystemName());
            parameters.put("locationName", companyConfiguration.getLocationName());
            parameters.put("startDate", startDate);
            parameters.put("endDate", endDate);

            // Llenar el reporte
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);

            try{
                /* iReport 3 */
                File jasper = new File(JSFUtil.getRealPath("/xproduction/reports/productionInputsReport.jasper"));
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasper.getPath(), parameters, dataSource);
                exportarPDF(jasperPrint);
            }catch (Exception e){
                System.out.println("...........Error al generar el reporte: " + e.getMessage());
                e.printStackTrace();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportarPDF(JasperPrint jasperPrint) throws IOException, JRException {

        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.addHeader("Content-disposition", "attachment; filename=productionInputsReport.pdf");
        ServletOutputStream stream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, stream);
        stream.flush();
        stream.close();
        FacesContext.getCurrentInstance().responseComplete();
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

    public class ProductionInputDTO {
        private String almacen;
        private String codigo;
        private String nombre;
        private String unidad;
        private BigDecimal cantidad;

        // Getters y setters
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public BigDecimal getCantidad() { return cantidad; }
        public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }


        public String getAlmacen() {
            return almacen;
        }

        public void setAlmacen(String almacen) {
            this.almacen = almacen;
        }

        public String getUnidad() {
            return unidad;
        }

        public void setUnidad(String unidad) {
            this.unidad = unidad;
        }
    }


}
