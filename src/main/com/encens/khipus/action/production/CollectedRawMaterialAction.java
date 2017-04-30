package com.encens.khipus.action.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.CollectedRawMaterial;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.service.production.CollectedRawMaterialService;
import com.encens.khipus.service.production.ProductiveZoneService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import java.util.Date;
import java.util.List;

import static org.jboss.seam.international.StatusMessage.Severity.INFO;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/7/13
 * Time: 9:50 AM
 * To change this template use File | Settings | File Templates.
 */
@Name("collectedRawMaterialAction")
@Scope(ScopeType.CONVERSATION)
public class CollectedRawMaterialAction {

    private Date date;
    private Long idProductiveZone;
    private ProductiveZone productiveZone;
    private List<CollectedRawMaterial> collectedRawMaterialList;
    private boolean isManaged;

    @Logger
    private Log log;

    @In
    private ProductiveZoneService productiveZoneService;

    @In
    private CollectedRawMaterialService collectedRawMaterialService;

    @In
    protected FacesMessages facesMessages;

    @Begin(ifOutcome = Outcome.SUCCESS)
    public String beginCollectionRawMaterial() {
        if (productiveZone == null) {
            writeAwarenessMessage("CollectedRawMaterial.create.requiredProductiveZone");
            return Outcome.REDISPLAY;
        }

//        collectedRawMaterialList = collectedRawMaterialService.prepareRawMaterialCollection(productiveZone, date);
        checkIfItsNew();
        return Outcome.SUCCESS;
    }

    private void checkIfItsNew() {
        isManaged = false;
        for (CollectedRawMaterial cm : collectedRawMaterialList) {
            isManaged = isManaged || (cm.getId() != null);
        }
    }

    @End
    public String cancel() {
        collectedRawMaterialList = null;
        productiveZone = null;
        date = null;
        return Outcome.CANCEL;
    }

    @End
    public String create() {
        try {
            return save("CollectedRawMaterial.create.successful");
        } catch (Exception ex) {
            writeAwarenessMessage("CollectedRawMaterial.error", ex);
            return Outcome.FAIL;
        }
    }

    @End
    public String update() {
        try {
            return save("CollectedRawMaterial.update.successful");
        } catch (Exception ex) {
            writeAwarenessMessage("CollectedRawMaterial.error", ex);
            return Outcome.FAIL;
        }
    }

    private String save(String msg) throws ConcurrencyException, EntryDuplicatedException {
        collectedRawMaterialService.save(productiveZone, collectedRawMaterialList, date);
        writeAwarenessMessage(msg);
        return Outcome.SUCCESS;
    }

    @End
    public String delete() {
        try {
            collectedRawMaterialService.delete(productiveZone, date);
            writeAwarenessMessage("CollectedRawMaterial.delete.successful");
            return Outcome.SUCCESS;
        } catch (Exception ex) {
            writeAwarenessMessage("CollectedRawMaterial.error", ex);
            return Outcome.FAIL;
        }
    }

    private void writeAwarenessMessage(String msg) {
        String source = productiveZone.getName() + " GAB " + productiveZone.getNumber() + "." + productiveZone.getGroup();
        facesMessages.addFromResourceBundle(INFO, msg, source,  date);
    }

    private void writeAwarenessMessage(String msg, Exception ex) {
        log.error("An error has ocurred: ", ex);
        writeAwarenessMessage(msg);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
        this.idProductiveZone = productiveZone.getId();
    }

    public List<CollectedRawMaterial> getCollectedRawMaterialList() {
        return collectedRawMaterialList;
    }

    public boolean isManaged() {
        return isManaged;
    }

    public Long getIdProductiveZone() {
        return idProductiveZone;
    }

    public void setIdProductiveZone(Long idProductiveZone) {
        this.idProductiveZone = idProductiveZone;
        try {
            this.productiveZone = productiveZoneService.find(idProductiveZone);
        } catch (Exception ex) {
            this.productiveZone = null;
        }
    }

    public double getTotalAmount() {
        double total = 0.0;
        if (collectedRawMaterialList != null) {
            for (CollectedRawMaterial cm : collectedRawMaterialList) {
                total += cm.getAmount();
            }
        }

        return total;
    }
}
