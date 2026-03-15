package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.ProductionCollectionState;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.SalaryMovementProducer;
import com.encens.khipus.model.production.TypeMovementProducer;
import com.encens.khipus.service.production.SalaryMovementProducerService;
import com.encens.khipus.util.JSFUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("salaryMovementProducerAction")
@Scope(ScopeType.CONVERSATION)
public class SalaryMovementProducerAction extends GenericAction<SalaryMovementProducer> {

    @In
    private SalaryMovementProducerService salaryMovementProducerService;

    @In(required = false)
    private SalaryMovementProducerDataModel salaryMovementProducerDataModel;

    private boolean readonly;

    private TypeMovementProducer movementProducerType;
    private Date startDate;
    private Date endDate;
    private Double amount;
    private String description;

    @Factory(value = "salaryMovementProducer", scope = ScopeType.STATELESS)
    public SalaryMovementProducer initSalaryMovementProducer() {
        return getInstance();
    }

    public void selectRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        try {
            rawMaterialProducer = getService().findById(RawMaterialProducer.class, rawMaterialProducer.getId());
            getInstance().setRawMaterialProducer(rawMaterialProducer);
        } catch (Exception ex) {
            log.error("Exception caught", ex);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    @Override
    @End
    public String create() {
        try {
            Double totalCollected = salaryMovementProducerService.getTotalCollectedByProductor(getInstance().getRawMaterialProducer(), getInstance().getDate());
            if(totalCollected < getInstance().getValor())
            {
                addMessgeFailBalance(getInstance().getRawMaterialProducer().getFullName(),totalCollected);
                return Outcome.REDISPLAY;
            }
            getInstance().setProductiveZone(getInstance().getRawMaterialProducer().getProductiveZone());
            getService().create(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }


    public String createGeneralDiscounts(){

        System.out.println("=====> TIPO MOV PRODUCTOR: " + movementProducerType.getName());
        List<RawMaterialProducer> rawMaterialProducerList = salaryMovementProducerService.findProducersWithCollection(startDate, endDate);
        List<SalaryMovementProducer> salaryMovementProducerList = new ArrayList<SalaryMovementProducer>();

        for (RawMaterialProducer producer : rawMaterialProducerList){
            SalaryMovementProducer salaryMovementProducer = new SalaryMovementProducer();
            salaryMovementProducer.setDate(this.startDate);
            salaryMovementProducer.setTypeMovementProducer(this.movementProducerType);
            salaryMovementProducer.setValor(this.amount);
            salaryMovementProducer.setDescription(this.description);
            salaryMovementProducer.setState(ProductionCollectionState.PENDING);

            salaryMovementProducer.setRawMaterialProducer(producer);
            salaryMovementProducer.setProductiveZone(producer.getProductiveZone());

            salaryMovementProducerList.add(salaryMovementProducer);
        }
        salaryMovementProducerService.createSalaryMovementProducer(salaryMovementProducerList);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"SalaryMovementProducer.message.generalDiscountCreated");
        return Outcome.SUCCESS;
    }

    public void exportToExcel() {
        try {
            Date filterStartDate = salaryMovementProducerDataModel != null ? salaryMovementProducerDataModel.getStartDate() : null;
            Date filterEndDate = salaryMovementProducerDataModel != null ? salaryMovementProducerDataModel.getEndDate() : null;
            TypeMovementProducer filterType = salaryMovementProducerDataModel != null && salaryMovementProducerDataModel.getCriteria() != null
                    ? salaryMovementProducerDataModel.getCriteria().getTypeMovementProducer() : null;
            String filterFirstName = salaryMovementProducerDataModel != null ? salaryMovementProducerDataModel.getFirstName() : null;
            String filterLastName = salaryMovementProducerDataModel != null ? salaryMovementProducerDataModel.getLastName() : null;
            String filterMaidenName = salaryMovementProducerDataModel != null ? salaryMovementProducerDataModel.getMaidenName() : null;

            List<SalaryMovementProducer> list = salaryMovementProducerService.findFiltered(
                    filterStartDate, filterEndDate, filterType, filterFirstName, filterLastName, filterMaidenName);

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Descuentos");

            HSSFRow header = sheet.createRow(0);
            header.createCell(0).setCellValue("FECHA");
            header.createCell(1).setCellValue("CI");
            header.createCell(2).setCellValue("IDPRODUCTORMATERIAPRIMA");
            header.createCell(3).setCellValue("NOMBRE COMPLETO");
            header.createCell(4).setCellValue("DESCRIPCION");
            header.createCell(5).setCellValue("VALOR");
            header.createCell(6).setCellValue("CONCEPTO");
            header.createCell(7).setCellValue("IDTIPOMOVIMIENTOPRODUCTOR");
            header.createCell(8).setCellValue("IDCOMPANIA");
            header.createCell(9).setCellValue("IDZONAPRODUCTIVA");
            header.createCell(10).setCellValue("NOMBRE");
            header.createCell(11).setCellValue("NUMERO");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            int rowNum = 1;
            for (SalaryMovementProducer item : list) {
                HSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getDate() != null ? sdf.format(item.getDate()) : "");
                RawMaterialProducer producer = item.getRawMaterialProducer();
                row.createCell(1).setCellValue(producer != null ? (producer.getIdNumber() != null ? producer.getIdNumber() : "") : "");
                row.createCell(2).setCellValue(producer != null && producer.getId() != null ? producer.getId().toString() : "");
                row.createCell(3).setCellValue(producer != null ? producer.getFullName() : "");
                row.createCell(4).setCellValue(item.getDescription() != null ? item.getDescription() : "");
                row.createCell(5).setCellValue(item.getValor());
                TypeMovementProducer type = item.getTypeMovementProducer();
                row.createCell(6).setCellValue(type != null ? type.getName() : "");
                row.createCell(7).setCellValue(type != null && type.getId() != null ? type.getId().toString() : "");
                row.createCell(8).setCellValue(item.getCompany() != null && item.getCompany().getId() != null ? item.getCompany().getId().toString() : "");
                com.encens.khipus.model.production.ProductiveZone zone = item.getProductiveZone();
                row.createCell(9).setCellValue(zone != null && zone.getId() != null ? zone.getId().toString() : "");
                row.createCell(10).setCellValue(zone != null && zone.getName() != null ? zone.getName() : "");
                row.createCell(11).setCellValue(zone != null && zone.getNumber() != null ? zone.getNumber() : "");
            }

            HttpServletResponse response = JSFUtil.getHttpServletResponse();
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=descuentos_productor.xls");
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException e) {
            log.error("Error exporting to Excel", e);
            facesMessages.addFromResourceBundle(ERROR, "Common.globalError.description");
        }
    }

    private void addMessgeFailBalance(String fullName,Double totalCollected ) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"SalaryMovementProducer.message.insufficientBalance",fullName,totalCollected);
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearRawMaterialProducer() {
        getInstance().setRawMaterialProducer(null);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isPending() {
        return ProductionCollectionState.PENDING.equals(getInstance().getState());
    }

    /** **/
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

    public TypeMovementProducer getMovementProducerType() {
        return movementProducerType;
    }

    public void setMovementProducerType(TypeMovementProducer movementProducerType) {
        this.movementProducerType = movementProducerType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
