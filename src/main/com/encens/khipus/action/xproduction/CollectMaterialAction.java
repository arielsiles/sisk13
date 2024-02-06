package com.encens.khipus.action.xproduction;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.xproduction.CollectMaterial;
import com.encens.khipus.model.xproduction.CollectMaterialState;
import com.encens.khipus.model.xproduction.ProductiveZone;
import com.encens.khipus.model.xproduction.RawMaterialProducer;
import com.encens.khipus.service.xproduction.CollectMaterialService;
import com.encens.khipus.service.xproduction.ProducerPriceService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Name("collectMaterialAction")
@Scope(ScopeType.CONVERSATION)
public class CollectMaterialAction extends GenericAction<CollectMaterial> {

    private RawMaterialProducer rawMaterialProducer;
    private ProductiveZone productiveZone;
    private BigDecimal rawMaterialPrice = BigDecimal.ZERO;

    private Date startDate = new Date();
    private Date endDate  = new Date();

    @In
    private ProducerPriceService producerPriceService;

    @In
    private CollectMaterialService collectMaterialService;

    @Factory(value = "collectMaterial", scope = ScopeType.STATELESS)
    public CollectMaterial initCollectMaterial() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "code";
    }

    @Override
    public String create() {

        try {
            CollectMaterial collectMaterial = getInstance();

            getService().create(collectMaterial);
            setOp(OP_UPDATE);
            setInstance(collectMaterial);
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    @Override
    public String update() {

        CollectMaterial collectMaterial = getInstance();

        String outcome = super.update();
        setOp(OP_UPDATE);
        //setInstance(collectMaterial);

        return outcome;
    }

    public String update(CollectMaterial collectMaterial) {
        Long currentVersion = (Long) getVersion(collectMaterial);
        try {
            getService().update(collectMaterial);
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            setVersion(getInstance(), currentVersion);
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            concurrencyLog();
            try {
                setInstance(getService().findById(getEntityClass(), getId(getInstance()), true));
            } catch (EntryNotFoundException e1) {
                entryNotFoundLog();
                addNotFoundMessage();
                return Outcome.FAIL;
            }
            addUpdateConcurrencyMessage();
            return Outcome.REDISPLAY;
        }
        //addUpdatedMessage();
        return Outcome.SUCCESS;
    }

    @End
    public String approve(){

        try {

            CollectMaterial collectMaterial = getInstance();
            collectMaterial.setState(CollectMaterialState.APR);
            getService().update(collectMaterial);
            addApprovedMessage();

            return Outcome.SUCCESS;

        } catch (EntryDuplicatedException e) {
            e.printStackTrace();
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            e.printStackTrace();
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    public String accounting(){
        List<CollectMaterial> collectMaterialList = collectMaterialService.findCollectMaterialNoAccounting( this.startDate, this.endDate);
        String outcome = Outcome.FAIL;
        if (collectMaterialList.size() > 0) {
            outcome = collectMaterialService.createCollectMaterialListAccounting(collectMaterialList,startDate,endDate);
            for( CollectMaterial collectMaterial:collectMaterialList){
                collectMaterial.setAccountigFlag(Boolean.TRUE); //set flag for accounting
                // collectMaterial
                update(collectMaterial);
            }
        }else{
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "No se encontraron registros para contabilizar");
        }
        if (outcome.equals(Outcome.SUCCESS))
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "se contabilizó correctamente");

        return outcome;
    }

    public void updateProducerPrice(){
        CollectMaterial instance = getInstance();
        //setRawMaterialPrice(instance.getMetaProduct().getPrice());
        instance.setPrice(instance.getMetaProduct().getPrice());

        BigDecimal price = producerPriceService.findProducerPrice(instance.getMetaProduct(), instance.getProducer());
        if ( price.doubleValue() > 0 ) {
            //setRawMaterialPrice(price);
            instance.setPrice(price);
        }
    }

    public void assignSupplier(RawMaterialProducer supplier){
        setRawMaterialProducer(supplier);
        getInstance().setProducer(supplier);
    }

    public void assignOrigin(ProductiveZone productiveZone){
        setProductiveZone(productiveZone);
        getInstance().setProductiveZone(productiveZone);
    }

    public void clearRawMaterialProducer() {
        getInstance().setProducer(null);
    }

    public void clearOrigin() {
        getInstance().setProductiveZone(null);
    }

    public void assignEmployee(Employee employee) {
        getInstance().setReceptionEmployee(employee);
    }

    public boolean isPending(){
        return getInstance().getState().equals(CollectMaterialState.PEN);
    }

    public BigDecimal getRawMaterialPrice() {
        return rawMaterialPrice;
    }

    public void setRawMaterialPrice(BigDecimal rawMaterialPrice) {
        this.rawMaterialPrice = rawMaterialPrice;
    }

    public RawMaterialProducer getRawMaterialProducer() {
        return rawMaterialProducer;
    }

    public void setRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        this.rawMaterialProducer = rawMaterialProducer;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    protected void addApprovedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "Common.message.approved", getDisplayPropertyValue());
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
}
