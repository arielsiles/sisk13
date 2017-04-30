package com.encens.khipus.action.admin;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.contacts.*;
import com.encens.khipus.model.customers.DocumentType;
import com.encens.khipus.service.admin.BusinessUnitService;
import com.encens.khipus.service.customers.ExtensionService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: macmac
 *
 * @version 2.8
 */
@Name("businessUnitAction")
@Scope(ScopeType.CONVERSATION)
public class BusinessUnitAction extends GenericAction<BusinessUnit> {

    @In
    private BusinessUnitService businessUnitService;
    @In
    private ExtensionService extensionService;

    public List<Extension> extensionList;
    private Extension extensionSite;
    private boolean showExtension = false;
    private Address address;
    private Street street;
    private District district;
    private Zone zone;
    private City city;
    private DocumentType documentType;

    @Factory(value = "businessUnit", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('BUSINESSUNIT','VIEW')}")
    public BusinessUnit initBusinessUnit() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameMessage() {
        return getInstance().getOrganization().getName();
    }

    @Create
    public void init() {
        address = new Address();
//        documentType= new DocumentType();
        getInstance().setOrganization(new Organization());
        getInstance().getOrganization().setAddress(address);
//        getInstance().getOrganization().setDocumentType(documentType);
    }

    @Override
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('BUSINESSUNIT','VIEW')}")
    public String select(BusinessUnit instance) {
        String outCome = super.select(instance);
        extensionSite = getInstance().getOrganization().getExtensionSite();
        documentType = getInstance().getOrganization().getDocumentType();
        address = getInstance().getOrganization().getAddress();
        street = getInstance().getOrganization().getAddress().getStreet();
        district = getInstance().getOrganization().getAddress().getStreet().getDistrict();
        zone = getInstance().getOrganization().getAddress().getStreet().getDistrict().getZone();
        city = getInstance().getOrganization().getAddress().getStreet().getDistrict().getZone().getCity();
        updateShowExtension();
        return outCome;
    }

    @Override
    @Restrict("#{s:hasPermission('BUSINESSUNIT','DELETE')}")
    public String delete() {
        try {
            businessUnitService.delete(getInstance());
            addDeletedMessage();
        } catch (EntryNotFoundException e) {
            addNotFoundMessage();
            return Outcome.REDISPLAY;
        } catch (ConcurrencyException e) {
            entryNotFoundLog();
            addDeleteConcurrencyMessage();
            return Outcome.REDISPLAY;
        } catch (ReferentialIntegrityException e) {
            referentialIntegrityLog();
            addDeleteReferentialIntegrityMessage();
            return Outcome.REDISPLAY;
        }
        return Outcome.SUCCESS;
    }

    public List<Extension> getExtensionList() {
        return extensionList;
    }

    public void setExtensionList(List<Extension> extensionList) {
        this.extensionList = extensionList;
    }

    public boolean isShowExtension() {
        return showExtension;
    }

    public void setShowExtension(boolean showExtension) {
        this.showExtension = showExtension;
    }

    public Extension getExtensionSite() {
        return extensionSite;
    }

    public void setExtensionSite(Extension extensionSite) {
        this.extensionSite = extensionSite;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void updateShowExtension() {
        extensionList = extensionService.findExtensionsByDocumentType(getInstance().getOrganization().getDocumentType());
        showExtension = extensionList != null && !extensionList.isEmpty();
        if (!showExtension) {
            getInstance().getOrganization().setExtensionSite(null);
        }
    }

    public void clearHumanResourcesResponsible() {
        getInstance().setHumanResourcesResponsible(null);
    }
}