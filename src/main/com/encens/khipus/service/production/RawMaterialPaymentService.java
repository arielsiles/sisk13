package com.encens.khipus.service.production;

import com.encens.khipus.model.production.PartialPaymentRawMaterial;
import com.encens.khipus.model.production.RawMaterialDiscount;
import com.encens.khipus.model.production.RawMaterialPayment;
import com.encens.khipus.model.production.RawMaterialPaymentDetail;

import javax.ejb.Local;
import java.util.List;

@Local
public interface RawMaterialPaymentService {

    public void saveRawMaterialPayment(RawMaterialPayment rawMaterialPayment, List<RawMaterialPaymentDetail> paymentDetails, List<RawMaterialDiscount> discountList,List<PartialPaymentRawMaterial> partialPaymentRawMaterial);

    public void saveLiquidateRawMaterialPayment(RawMaterialPayment rawMaterialPayment);

    public void deleteRawMaterialPayment(RawMaterialPayment rawMaterialPayment);

    public void deletePartialPayment(PartialPaymentRawMaterial partialPaymentRawMaterial);

    public List<RawMaterialPaymentDetail> getPaymentDetails(Long rawMaterialPaymentId);

    public List<RawMaterialDiscount> getRawMaterialDiscounts(Long rawMaterialPaymentId);

    public List<PartialPaymentRawMaterial> getPartialPaymentRawMaterials(Long rawMaterialPaymentId);

}
