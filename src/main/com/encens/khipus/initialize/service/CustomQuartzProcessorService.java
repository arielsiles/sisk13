package com.encens.khipus.initialize.service;

import com.encens.khipus.exception.initialize.CustomQuartzProcessorNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.initialize.CustomQuartzProcessor;

import javax.ejb.Local;

/**
 * @author
 * @version 3.0
 */
@Local
public interface CustomQuartzProcessorService extends GenericService {
    void execute();

    /**
     * Finds the configuration object given a seamServiceName
     *
     * @param seamServiceName the key name to search for
     * @return a configuration object
     * @throws com.encens.khipus.exception.initialize.CustomQuartzProcessorNotFoundException
     *          thrown when the configuration object was not found
     */
    CustomQuartzProcessor findConfiguration(String seamServiceName) throws CustomQuartzProcessorNotFoundException;
}
