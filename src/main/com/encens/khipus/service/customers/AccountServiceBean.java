package com.encens.khipus.service.customers;

import com.encens.khipus.model.contacts.Entity;
import com.encens.khipus.model.customers.*;
import com.encens.khipus.model.finances.VoucherDetail;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Credit service implementation class
 *
 * @author
 */
@Stateless
@Name("accountService")
@AutoCreate
public class AccountServiceBean implements AccountService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public List<VoucherDetail> getAccountDetailList(Account account){

        List<VoucherDetail> voucherDetails = new ArrayList<VoucherDetail>();

        try {
            voucherDetails = (List<VoucherDetail>) em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " where voucherDetail.partnerAccount = :account ")
                    .setParameter("account", account)
                    .getResultList();
            
        }catch (NoResultException e){
            return null;
        }
        return voucherDetails;
    }


}
