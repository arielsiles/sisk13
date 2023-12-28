package com.encens.khipus.service.production;

import com.encens.khipus.model.production.CollectMaterialState;
import com.encens.khipus.model.production.RawMaterialDiscount;
import com.encens.khipus.model.production.RawMaterialPayment;
import com.encens.khipus.model.production.RawMaterialPaymentDetail;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("rawMaterialPaymentService")
@AutoCreate
public class RawMaterialPaymentServiceBean implements RawMaterialPaymentService {

    @In(value = "#{entityManager}")
    private EntityManager em;

    @Override
    public void saveRawMaterialPayment(RawMaterialPayment rawMaterialPayment, List<RawMaterialPaymentDetail> paymentDetails, List<RawMaterialDiscount> discountList) {

        em.merge(rawMaterialPayment);
        em.flush();

        for (RawMaterialPaymentDetail paymentDetail : paymentDetails){
            if (em.contains(paymentDetail)){
                System.out.println("--> Merge: " + paymentDetail.getId() + " - " + paymentDetail.getCollectMaterial().getCode());
                em.merge(paymentDetail);
                em.flush();
            } else {
                paymentDetail.setRawMaterialPayment(rawMaterialPayment);
                System.out.println("--> Persist: " + paymentDetail.getCollectMaterial().getCode());
                em.persist(paymentDetail);
                em.flush();
            }
        }

        for (RawMaterialPaymentDetail paymentDetail : paymentDetails) {
            em.refresh(paymentDetail);
            paymentDetail.getCollectMaterial().setState(CollectMaterialState.PAY);
            em.merge(paymentDetail.getCollectMaterial());
            em.flush();
        }

        for (RawMaterialDiscount discount : discountList) {
            discount.setRawMaterialPayment(rawMaterialPayment);
            if (em.contains(discount)){
                em.merge(discount);
                em.flush();
            } else {
                em.persist(discount);
                em.flush();
            }

        }

    }

    @Override
    public void saveLiquidateRawMaterialPayment(RawMaterialPayment rawMaterialPayment) {

        em.merge(rawMaterialPayment);
        em.flush();

        for (RawMaterialPaymentDetail paymentDetail : rawMaterialPayment.getRawMaterialPaymentDetaiList()){
            paymentDetail.getCollectMaterial().setState(CollectMaterialState.LIQ);
            em.merge(paymentDetail.getCollectMaterial());
            em.flush();
        }

    }

    @Override
    public void deleteRawMaterialPayment(RawMaterialPayment rawMaterialPayment) {

        for (RawMaterialPaymentDetail paymentDetail : rawMaterialPayment.getRawMaterialPaymentDetaiList()){
            paymentDetail.getCollectMaterial().setState(CollectMaterialState.APR);
            em.merge(paymentDetail.getCollectMaterial());
            em.flush();
        }

        em.remove(rawMaterialPayment);
        em.flush();
    }

    @Override
    public List<RawMaterialPaymentDetail> getPaymentDetails(Long rawMaterialPaymentId) {
        try {
            return em.createQuery("select rawMaterialPaymentDetail " +
                    "from RawMaterialPaymentDetail rawMaterialPaymentDetail " +
                    "where rawMaterialPaymentDetail.rawMaterialPayment.id =:rawMaterialPaymentId ")
                    .setParameter("rawMaterialPaymentId", rawMaterialPaymentId)
                    .getResultList();
        } catch (Exception e) { }
        return new ArrayList<RawMaterialPaymentDetail>(0);
    }

    @Override
    public List<RawMaterialDiscount> getRawMaterialDiscounts(Long rawMaterialPaymentId) {
        try {
            return em.createQuery("select rawMaterialDiscount " +
                    "from RawMaterialDiscount rawMaterialDiscount " +
                    "where rawMaterialDiscount.rawMaterialPayment.id =:rawMaterialPaymentId ")
                    .setParameter("rawMaterialPaymentId", rawMaterialPaymentId)
                    .getResultList();
        } catch (Exception e) { }
        return new ArrayList<RawMaterialDiscount>(0);
    }
}
