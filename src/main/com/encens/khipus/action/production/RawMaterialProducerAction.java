package com.encens.khipus.action.production;

import com.encens.khipus.exception.production.SalaryMovementProducerException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.production.*;
import com.encens.khipus.service.customers.ExtensionService;
import com.encens.khipus.service.production.*;
import com.encens.khipus.util.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.jboss.seam.international.StatusMessage.Severity.WARN;

@Name("rawMaterialProducerAction")
@Scope(ScopeType.CONVERSATION)
public class RawMaterialProducerAction extends GenericAction<RawMaterialProducer> {

    @In(value = "rawMaterialProducerService")
    private RawMaterialProducerService rawMaterialProducerService;

    @In
    private ExtensionService extensionService;

    @In
    private ProductiveZoneService productiveZoneService;

    @In
    private LogProductiveZoneService logProductiveZoneService;

    @In
    private CollectedRawMaterialService collectedRawMaterialService;

    @In
    private SalaryMovementProducerService salaryMovementProducerService;

    @In(create = true)
    private ProducerTaxAction producerTaxAction;

    public List<Extension> extensionList;
    private boolean showExtension = false;
    private boolean moveDiscoints = true;
    private boolean moveSessions = true;
    private boolean showOptionsProductiveZone = false;

    private List<ProductiveZone> productiveZones;
    private ProductiveZone  productiveZoneConcurrent;
    private List<ProducerTax> producerTaxesDelete = new ArrayList<ProducerTax>();

    @Override
    protected GenericService getService() {
        return rawMaterialProducerService;
    }

    @Factory(value = "rawMaterialProducer", scope = ScopeType.STATELESS)
    public RawMaterialProducer initContinent() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getInstance().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getInstance().setExtensionSite(null);
        }
    }

    public boolean isShowExtension() {
        return showExtension;
    }

    public void setShowExtension(boolean showExtension) {
        this.showExtension = showExtension;
    }

    public List<ProductiveZone> getProductiveZones() {
        if (productiveZones == null) {
            productiveZones = productiveZoneService.findAll();
        }
        return productiveZones;
    }

    public void selectProductiveZone(ProductiveZone productiveZone) {
        try {
            productiveZoneConcurrent = getInstance().getProductiveZone();
            productiveZone = getService().findById(ProductiveZone.class, productiveZone.getId());
            getInstance().setProductiveZone(productiveZone);
            if(productiveZone != productiveZoneConcurrent)
                showOptionsProductiveZone = true;
            else
                showOptionsProductiveZone = false;
        } catch (Exception ex) {
            log.error(ex);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.globalError.description");
        }
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public String startCreate() {
        return Outcome.SUCCESS;
    }

    @Override
    @End(ifOutcome = Outcome.SUCCESS)
    public String update() {
        if (checkLicenseBoundaries() == false) {
            return Outcome.REDISPLAY;
        }
        if(getInstance().getProductiveZone() != productiveZoneConcurrent && showOptionsProductiveZone)
        {
           //if(!moveSessions)
           if(verifySessionsToRawMaterialProducer())
           {
              return Outcome.REDISPLAY;
           }

           //if(!moveDiscoints)
           if(verifyDiscointToRawMaterialProducer())
           {
               return Outcome.REDISPLAY;
           }

          // if(moveDiscoints)
          // {
               try {
                    salaryMovementProducerService.moveDiscountsProductor(getInstance(),new Date(), productiveZoneConcurrent);
               } catch (SalaryMovementProducerException e) {
                   addErrorHasRawMaterialPayRoll();
                   return Outcome.REDISPLAY;
               }
          // }

         //  if(moveSessions)
          // {
               try {
                   salaryMovementProducerService.moveSessionsProductor(getInstance(), new Date(), productiveZoneConcurrent);
               } catch (SalaryMovementProducerException e) {
                   addErrorHasRawMaterialPayRoll();
                   return Outcome.REDISPLAY;
               }
         //  }

           LogProductiveZone logProductiveZone = new LogProductiveZone();
           logProductiveZone.setDate(new Date());
           logProductiveZone.setProductiveZone(productiveZoneConcurrent);
           logProductiveZone.setRawMaterialProducer(getInstance());
           logProductiveZoneService.createLog(logProductiveZone);
        }
        for(ProducerTax tax:producerTaxesDelete)
        {
            producerTaxAction.setInstance(tax);
            producerTaxAction.delete();
        }

        for(ProducerTax tax :getInstance().getProducerTaxes())
        {
            if(tax.getId() == null)
            {
                producerTaxAction.setInstance(tax);
                producerTaxAction.create();
            }

        }

        //super.setOp(OP_UPDATE);
        return super.update();
    }

    private void addErrorHasRawMaterialPayRoll() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"RawMaterialProducer.error.errorHasRawMaterialPayRoll");
    }

    private Boolean verifyDiscointToRawMaterialProducer()
    {
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH,1);*/
        Date startDate = DateUtils.getFirsDayFromPeriod(new Date());
        Date endDate = DateUtils.getLastDayFromPeriod(new Date());
        /*Date startDate = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH,15);
        Date endDate = calendar.getTime();*/


        if(collectedRawMaterialService.getHasDiscounts(startDate, endDate, getInstance()))
        {
            addErrorHasDiscounts(getInstance().getName());
            return true;
        }
        return false;
    }

    private Boolean verifySessionsToRawMaterialProducer()
    {
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH,Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH,1);*/
        Date startDate = DateUtils.getFirsDayFromPeriod(new Date());
        Date endDate = DateUtils.getLastDayFromPeriod(new Date());
        /*Date startDate = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH,15);
        Date endDate = calendar.getTime();*/



        if(collectedRawMaterialService.getHasCollected(startDate, endDate, getInstance()))
        {
            addErrorHasSessions(getInstance().getName());
            return true;
        }
        return false;
    }

    protected void addErrorHasDiscounts(String name) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"RawMaterialProducer.error.errorHasDiscounts",name);
    }
    protected void addErrorHasSessions(String name) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,"RawMaterialProducer.error.errorHasSessions",name);
    }

    @Override
    @End(ifOutcome = Outcome.SUCCESS)
    public String create() {
        if (checkLicenseBoundaries() == false) {
            return Outcome.REDISPLAY;
        }

        return super.create();
    }

    private boolean checkLicenseBoundaries() {
        if (getInstance().getStartDateTaxLicence() == null || getInstance().getExpirationDateTaxLicence() == null) {
            return true;
        }

        boolean valid = getInstance().getStartDateTaxLicence().compareTo(getInstance().getExpirationDateTaxLicence()) <= 0;
        if (!valid) {
            facesMessages.addFromResourceBundle(WARN, "RawMaterialProducer.warning.licenseStartDateGreaterThanEndDate");
            return false;
        }

        return true;
    }

    public ProductiveZone getProductiveZoneConcurrent() {
        return productiveZoneConcurrent;
    }

    public void setProductiveZoneConcurrent(ProductiveZone productiveZoneConcurrent) {
        this.productiveZoneConcurrent = productiveZoneConcurrent;
    }

    public boolean isMoveDiscoints() {
        return moveDiscoints;
    }

    public void setMoveDiscoints(boolean moveDiscoints) {
        this.moveDiscoints = moveDiscoints;
    }

    public boolean isMoveSessions() {
        return moveSessions;
    }

    public void setMoveSessions(boolean moveSessions) {
        this.moveSessions = moveSessions;
    }

    public boolean isShowOptionsProductiveZone() {
        return showOptionsProductiveZone;
    }

    public void setShowOptionsProductiveZone(boolean showOptionsProductiveZone) {
        this.showOptionsProductiveZone = showOptionsProductiveZone;
    }

    public void removeGestionTax(ProducerTax producerTax){
        getInstance().getProducerTaxes().remove(producerTax);
        producerTaxesDelete.add(producerTax);
    }

    public void addGestionTaxs(List<GestionTax> gestionTaxes)
    {
        for(GestionTax gestionTax:gestionTaxes)
        {
            if(gestionTaxWasChoise(gestionTax))
            {ProducerTax producerTax = new ProducerTax();
                producerTax.setGestionTax(gestionTax);
                producerTax.setRawMaterialProducerTax(getInstance());
                getInstance().getProducerTaxes().add(producerTax);
            }
        }
        for(ProducerTax tax:getInstance().getProducerTaxes())
        {
            producerTaxesDelete.remove(tax);
        }
    }

    public Boolean gestionTaxWasChoise(GestionTax gestionTax)
    {
        for(ProducerTax tax:getInstance().getProducerTaxes())
        {
            if(tax.getGestionTax().getId().compareTo(gestionTax.getId()) == 0)
                return false;
        }
        return true;
    }
}
