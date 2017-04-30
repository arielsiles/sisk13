package com.encens.khipus.service.fixedassets;

import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.contacts.Salutation;
import com.encens.khipus.model.customers.DocumentType;
import com.encens.khipus.model.employees.Charge;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.PayableDocumentType;

import javax.ejb.Local;

/**
 * CompanyConfigurationService
 *
 * @author
 * @version 2.4.2
 */
@Local
public interface CompanyConfigurationService extends GenericService {
    /**
     * @return retrieves the corresponding CompanyConfiguration according to the string Company number supplied
     * @throws com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException
     *          when the result is not found
     */

    CompanyConfiguration findCompanyConfiguration() throws CompanyConfigurationNotFoundException;

    String findDefaultTreasuryUserNumber();

    String findDefaultAccountancyUserNumber();

    Charge findDefaultProfessorsCharge() throws CompanyConfigurationNotFoundException;

    DocumentType findDefaultDocumentType() throws CompanyConfigurationNotFoundException;

    Salutation findDefaultSalutationForWoman() throws CompanyConfigurationNotFoundException;

    Salutation findDefaultSalutationForMan() throws CompanyConfigurationNotFoundException;

    boolean isPurchaseOrderCodificationEnabled() throws CompanyConfigurationNotFoundException;

    PayableDocumentType findDefaultPayableCashBoxDocumentType() throws CompanyConfigurationNotFoundException;

    String findDefaultPurchaseOrderRemakePaymentUserNumber() throws CompanyConfigurationNotFoundException;

    Integer findDefaultPurchaseOrderRemakeYear() throws CompanyConfigurationNotFoundException;

    double findUnitPriceMilk() throws CompanyConfigurationNotFoundException;

    double findIT() throws CompanyConfigurationNotFoundException;

    double finIUE() throws CompanyConfigurationNotFoundException;
}