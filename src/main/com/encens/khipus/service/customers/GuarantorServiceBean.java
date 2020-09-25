package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Guarantor;
import com.encens.khipus.model.customers.GuarantorDocument;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

/**
 * Customer service operations
 *
 * @author
 * @version $Id: CustomerServiceBean.java 2008-9-10 10:33:11 $
 */
@Stateless
@Name("guarantorService")
@AutoCreate
public class GuarantorServiceBean implements GuarantorService {

    @In("#{entityManager}")
    private EntityManager em;

    @Override
    public void createCreditGuarantor(Guarantor guarantor) {

        System.out.println("-----> Garante: " + guarantor.getPartner().getPerson().getFullName());
        em.persist(guarantor);
        em.flush();

        for (GuarantorDocument guarantorDocument : guarantor.getGuarantorDocumentList()){
            System.out.println("--> Garantia: "  + guarantorDocument.getDescription());
            em.persist(guarantorDocument);
            em.flush();
        }

    }
}
