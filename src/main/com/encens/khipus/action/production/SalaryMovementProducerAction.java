package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.ProductionCollectionState;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.SalaryMovementProducer;
import com.encens.khipus.model.production.TypeMovementProducer;
import com.encens.khipus.service.production.SalaryMovementProducerService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.jboss.seam.international.StatusMessage.Severity.ERROR;

@Name("salaryMovementProducerAction")
@Scope(ScopeType.CONVERSATION)
public class SalaryMovementProducerAction extends GenericAction<SalaryMovementProducer> {

    @In
    private SalaryMovementProducerService salaryMovementProducerService;

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
