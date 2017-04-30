package com.encens.khipus.dataintegration.service;

import com.encens.khipus.dataintegration.configuration.structure.IntegrationElement;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;

import javax.ejb.Local;
import javax.transaction.UserTransaction;
import java.util.Map;

/**
 * @author
 */
@Local
public interface DataIntegrationService {
    void executeIntegration(String localDataSource, IntegrationElement integrationElement);

    void customOperations(UserTransaction userTransaction,
                          IntegrationElement integrationElement,
                          Map<String, String> row) throws CompanyConfigurationNotFoundException;
}
