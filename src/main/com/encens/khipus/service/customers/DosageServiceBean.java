package com.encens.khipus.service.customers;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.customers.Customer;
import com.encens.khipus.model.customers.Dosage;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * Customer service operations
 *
 * @author
 * @version $Id: CustomerServiceBean.java 2008-9-10 10:33:11 $
 */
@Stateless
@Name("dosageService")
@AutoCreate
public class DosageServiceBean extends GenericServiceBean implements DosageSevice {

    @In
    protected Map<String, String> messages;

    @Override
    public void find() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
