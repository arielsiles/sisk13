package com.encens.khipus.service.customers;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.customers.Partner;
import com.encens.khipus.service.common.SequenceGeneratorService;
import com.encens.khipus.util.Constants;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.PersistenceException;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

/**
 * Created by Ariel
 */
@Stateless
@Name("partnerService")
@AutoCreate
public class PartnerServiceBean extends GenericServiceBean implements PartnerService {

    @In
    private SequenceGeneratorService sequenceGeneratorService;

    @Override
    @TransactionAttribute(REQUIRES_NEW)
    public void createPartner(Partner partner) throws EntryDuplicatedException {
        try {
            partner.setNumber(String.valueOf(sequenceGeneratorService.nextValue(Constants.CUSTOMER_PARTNER_NUMBER_SEQUENCE)));
            getEntityManager().persist(partner);
            getEntityManager().flush();
        } catch (PersistenceException e) {
            throw new EntryDuplicatedException();
        }
    }

}