package com.encens.khipus.action.xproduction;

import com.encens.khipus.model.xproduction.ProductionLine;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.Calendar;

/**
 * Pantalla unica "Reporte Diario de Produccion": el usuario elige la linea de
 * produccion y, segun su tipo (ProductionLineType), se genera el Excel
 * correspondiente:
 *   - ULEXITA  -> {@link UlexitaDailyReportAction}
 *   - BARITINA -> {@link BaritinaDailyReportAction}
 *
 * Cada reporte especifico mantiene su propia logica/columnas; este dispatcher
 * solo enruta segun el tipo de linea. Extensible: una nueva linea agrega su
 * accion y un caso aqui.
 */
@Name("dailyProductionReportAction")
@Scope(ScopeType.PAGE)
public class DailyProductionReportAction {

    @In
    private FacesMessages facesMessages;

    private Integer year;
    private Integer month;
    private ProductionLine productionLine;

    @Create
    public void init() {
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH) + 1;
    }

    public void generateReport() {
        if (productionLine == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DailyProductionReport.error.lineRequired");
            return;
        }
        if (year == null || month == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "DailyProductionReport.error.periodRequired");
            return;
        }

        if (productionLine.isUlexitaTemplate()) {
            UlexitaDailyReportAction a = (UlexitaDailyReportAction)
                    Component.getInstance("ulexitaDailyReportAction", true);
            a.setProductionLine(productionLine);
            a.setYear(year);
            a.setMonth(month);
            a.generateReport();
        } else if (productionLine.isBaritinaTemplate()) {
            BaritinaDailyReportAction a = (BaritinaDailyReportAction)
                    Component.getInstance("baritinaDailyReportAction", true);
            a.setProductionLine(productionLine);
            a.setYear(year);
            a.setMonth(month);
            a.generateReport();
        } else {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "DailyProductionReport.error.noTemplate");
        }
    }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public ProductionLine getProductionLine() { return productionLine; }
    public void setProductionLine(ProductionLine productionLine) { this.productionLine = productionLine; }
}
