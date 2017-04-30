package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.contacts.Salutation;
import com.encens.khipus.model.customers.DocumentType;
import com.encens.khipus.model.employees.Charge;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.PayableDocumentType;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

/**
 * @author
 * @version 2.4.2
 */
@Name("companyConfigurationService")
@Stateless
@AutoCreate
public class CompanyConfigurationServiceBean extends GenericServiceBean implements CompanyConfigurationService {

    /**
     * @return retrieves the corresponding CompanyConfiguration according to the string Company number supplied
     */

    public CompanyConfiguration findCompanyConfiguration() throws CompanyConfigurationNotFoundException {
        CompanyConfiguration companyConfiguration = (CompanyConfiguration) getEntityManager()
                .createNamedQuery("CompanyConfiguration.findByCompany").getSingleResult();
        if (companyConfiguration == null) {
            throw new CompanyConfigurationNotFoundException("The company configuration for current company \"" + Constants.defaultCompanyNumber + "\" haven't been configured");
        }
        return companyConfiguration;
    }

    public String findDefaultTreasuryUserNumber() {
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            log.debug("Cannot find the CompanyConfiguration.", e);
        }
        //return companyConfiguration != null ? companyConfiguration.getDefaultTreasuryUser().getOracleUser() : null;
        return companyConfiguration != null ? companyConfiguration.getDefaultTreasuryUser().getId() : null;
    }

    public String findDefaultAccountancyUserNumber() {
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
            log.debug("Cannot find the CompanyConfiguration.", e);
        }
        return companyConfiguration != null ? companyConfiguration.getDefaultAccountancyUser().getId() : null;
    }

    public Charge findDefaultProfessorsCharge() throws CompanyConfigurationNotFoundException {
        return findCompanyConfiguration().getDefaultProfessorsCharge();
    }

    public DocumentType findDefaultDocumentType() throws CompanyConfigurationNotFoundException {
        return findCompanyConfiguration().getDefaultDocumentType();
    }

    public Salutation findDefaultSalutationForWoman() throws CompanyConfigurationNotFoundException {
        return findCompanyConfiguration().getDefaultSalutationForWoman();
    }

    public Salutation findDefaultSalutationForMan() throws CompanyConfigurationNotFoundException {
        return findCompanyConfiguration().getDefaultSalutationForMan();
    }

    public boolean isPurchaseOrderCodificationEnabled() throws CompanyConfigurationNotFoundException {
        return findCompanyConfiguration().isPurchaseOrderCodificationEnabled();
    }

    public PayableDocumentType findDefaultPayableCashBoxDocumentType() throws CompanyConfigurationNotFoundException {
        PayableDocumentType documentType = findCompanyConfiguration().getCashBoxDocumentType();
        if (documentType == null) {
            throw new CompanyConfigurationNotFoundException("The default payable document type for current company \"" + Constants.defaultCompanyNumber + "\" haven't been configured");
        }
        return documentType;
    }

    public String findDefaultPurchaseOrderRemakePaymentUserNumber() throws CompanyConfigurationNotFoundException {
        String userNumber = findCompanyConfiguration().getDefaultPurchaseOrderRemakePaymentUserNumber();
        if (userNumber == null) {
            throw new CompanyConfigurationNotFoundException("The default purchase order remake payment user for current company \"" + Constants.defaultCompanyNumber + "\" haven't been configured");
        }
        return userNumber;
    }

    public Integer findDefaultPurchaseOrderRemakeYear() throws CompanyConfigurationNotFoundException {
        Integer remakeYear = findCompanyConfiguration().getDefaultPurchaseOrderRemakeYear();
        if (remakeYear == null) {
            throw new CompanyConfigurationNotFoundException("The default purchase order remake year for current company \"" + Constants.defaultCompanyNumber + "\" haven't been configured");
        }
        return remakeYear;
    }

    @Override
    public double findUnitPriceMilk() throws CompanyConfigurationNotFoundException {
        double unitPrice = findCompanyConfiguration().getUnitPriceMilk();
        if(unitPrice == 0.0){
            throw new CompanyConfigurationNotFoundException("The default unit price milk for current company \"" + Constants.defaultCompanyNumber + "\" haven't been configured");
        }
         return unitPrice;
    }

    @Override
    public double findIT() throws CompanyConfigurationNotFoundException {
        double it = findCompanyConfiguration().getIt();
        if(it == 0.0){
            throw new CompanyConfigurationNotFoundException("The default IT. for current company \"" + Constants.defaultCompanyNumber + "\" haven't been configured");
        }
        return it;
    }

    @Override
    public double finIUE() throws CompanyConfigurationNotFoundException {
        double iue = findCompanyConfiguration().getIue();
        if(iue == 0.0){
            throw new CompanyConfigurationNotFoundException("The default Iue. for current company \"" + Constants.defaultCompanyNumber + "\" haven't been configured");
        }
        return iue;
    }

}