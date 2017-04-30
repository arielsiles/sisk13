package com.encens.khipus.initialize.service;

import com.encens.khipus.exception.initialize.CustomQuartzProcessorNotFoundException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.initialize.CustomQuartzProcessor;
import com.encens.khipus.initialize.CustomQuartzProcessorSetting;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author
 * @version 3.0
 */
@Stateless
public class CustomQuartzProcessorServiceBean extends GenericServiceBean implements CustomQuartzProcessorService {
    @PersistenceContext(unitName = "khipus")
    private EntityManager em;

    protected static final LogProvider log = Logging.getLogProvider(CustomQuartzProcessorServiceBean.class);

    @Resource
    protected EJBContext ejbContext;


    @Override
    public void execute() {


    }

    /**
     * Finds the configuration object given a seamServiceName
     *
     * @param seamServiceName the key name to search for
     * @return a configuration object
     * @throws CustomQuartzProcessorNotFoundException
     *          thrown when the configuration object was not found
     */
    public CustomQuartzProcessor findConfiguration(String seamServiceName) throws CustomQuartzProcessorNotFoundException {
        if (CustomQuartzProcessorSetting.i.getCustomQuartzProcessorMap().containsKey(seamServiceName)) {
            return CustomQuartzProcessorSetting.i.getCustomQuartzProcessorMap().get(seamServiceName);
        } else {
            throw new CustomQuartzProcessorNotFoundException();
        }
    }

    protected EntityManager getEntityManager() {
        return em;
    }

    protected void setEntityManager(EntityManager em) {
        this.em = em;
    }

    protected EJBContext getEjbContext() {
        return ejbContext;
    }
}
