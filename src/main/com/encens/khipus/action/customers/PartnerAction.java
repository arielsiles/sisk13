package com.encens.khipus.action.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.model.contacts.Country;
import com.encens.khipus.model.contacts.Department;
import com.encens.khipus.model.contacts.Extension;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.model.customers.PartnerState;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.service.customers.ExtensionService;
import com.encens.khipus.service.customers.PartnerService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import com.encens.khipus.framework.action.Outcome;

import java.util.List;

/**
 * Actions for Credit
 *
 * @author:
 */

@Name("partnerAction")
@Scope(ScopeType.CONVERSATION)
public class PartnerAction extends GenericAction<Partner> {

    @In
    private ExtensionService extensionService;

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @In
    private PartnerService partnerService;

    public List<Extension> extensionList;
    private boolean showExtension = false;

    private Country country;
    private Department department;

    @Factory(value = "partner", scope = ScopeType.STATELESS)
    public Partner initPartner() {
        return getInstance();
    }

    @Override
    @End
    public String create() {
        try {
            partnerService.createPartner(getInstance());
            addCreatedMessage();
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    @Create
    public void atCreateTime() {
        if (!isManaged()) {
            getInstance().setState(PartnerState.VIG);
            assignCode();
        }
    }

    private void assignCode() {
        getInstance().setNumber(String.valueOf(sequenceGeneratorService.findNextSequenceValue(Constants.CUSTOMER_PARTNER_NUMBER_SEQUENCE)));
    }

    public boolean isShowExtension() {
        return showExtension;
    }

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getInstance().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getInstance().setExtensionSite(null);
        }
    }

    public void selectProductiveZone(ProductiveZone productiveZone) {
        getInstance().setProductiveZone(productiveZone);
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
