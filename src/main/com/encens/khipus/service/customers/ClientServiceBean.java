package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.customers.PaymentMethodSin;
import com.encens.khipus.model.finances.VoucherDetail;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Customer service operations
 *
 * @author
 * @version $Id: CustomerServiceBean.java 2008-9-10 10:33:11 $
 */
@Stateless
@Name("clientService")
@AutoCreate
public class ClientServiceBean implements ClientService {

    @In
    private FacesMessages facesMessages;

    @In("#{entityManager}")
    private EntityManager em;


    /**
     * Returns all Entity instances which have customer information defined. So it retrives
     * all customers.
     *
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public List<Client> getAllClients() {
        return em.createNamedQuery("Client.findAll").getResultList();
    }

    public Double getBalanceClient(Date startDate, String cashAccountCode, Long clientId){

        List<VoucherDetail> voucherDetailList = new ArrayList<VoucherDetail>();
        try {

            voucherDetailList = em.createQuery("select voucherDetail from VoucherDetail voucherDetail " +
                    " left join voucherDetail.voucher voucher " +
                    " where voucher.date < :startdate " +
                    " and voucherDetail.account = :cashAccountCode " +
                    " and voucherDetail.client.id = :clientId " +
                    " and voucher.state <> 'ANL'")
                    .setParameter("startdate", startDate)
                    .setParameter("cashAccountCode", cashAccountCode)
                    .setParameter("clientId", clientId)
                    .getResultList();
        }catch (NoResultException e){
            return null;
        }

        Double balance = new Double("0.00");
        Double balanceD = new Double("0.00");
        Double balanceC = new Double("0.00");

        for (VoucherDetail voucherDetail : voucherDetailList){
            balanceD = balanceD + voucherDetail.getDebit().doubleValue();
            balanceC = balanceC + voucherDetail.getCredit().doubleValue();
        }
        balance = (balanceD - balanceC);
        return balance;
    }

    public PaymentMethodSin findPaymentMethodSin(Integer code){

        return (PaymentMethodSin)em.createQuery("select p from PaymentMethodSin p " +
                "where p.code =:code ")
                .setParameter("code", code)
                .getSingleResult();

    }

    @Override
    public Client findClientByIdNumber(String idNumber) {

        try {
            return (Client) em.createNamedQuery("Client.findByIdNumber")
                    .setParameter("idNumber", idNumber).getSingleResult();
        } catch (NoResultException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,"La persona con Nro. CI: " + idNumber +
                    " no se encuentra registrado en Clientes. Completar la contabilidad.");
            return null;
        }
    }

}
