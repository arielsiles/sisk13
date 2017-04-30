package com.encens.khipus.service.production;


import com.encens.khipus.framework.service.ExtendedGenericServiceBean;
import com.encens.khipus.model.production.CollectionForm;
import com.encens.khipus.model.production.CollectionRecord;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucherType;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("CollectionFormService")
@AutoCreate
@Stateless
public class CollectionFormServiceBean extends ExtendedGenericServiceBean implements CollectionFormService {

    @In
    private ProductiveZoneService productiveZoneService;

    @Override
    public void populateWithCollectionRecords(CollectionForm collectionForm) {
        List<ProductiveZone> zones = productiveZoneService.findAll();
        for (ProductiveZone zone : zones) {
            CollectionRecord record = createCollectionRecord(collectionForm, zone);
            collectionForm.getCollectionRecordList().add(record);
        }
    }

    private CollectionRecord createCollectionRecord(CollectionForm collectionForm, ProductiveZone zone) {
        CollectionRecord record = new CollectionRecord();
        record.setCollectionForm(collectionForm);
        record.setProductiveZone(zone);
        record.setReceivedAmount(0.0);
        record.setWeightedAmount(0.0);
        record.setRejectedAmount(0.0);
        return record;
    }

    @Override
    public void populateWithTotalsOfCollectedAmount(CollectionForm collectionForm) {
        Query query = getEntityManager().createNamedQuery("CollectionForm.calculateCollectedAmountOnDateByMetaProduct")
                                        .setParameter("date", collectionForm.getDate())
                                        .setParameter("metaProduct", collectionForm.getMetaProduct());
        Map<Long, Double> map = createMap(query);
        for(CollectionRecord record : collectionForm.getCollectionRecordList()) {
            Double collectedRawMaterial = map.get(record.getProductiveZone().getId());
            if (collectedRawMaterial != null) {
                record.setReceivedAmount(collectedRawMaterial);
            } else {
                record.setReceivedAmount(0.0);
            }
        }
    }

    public CollectionForm finCollectionFormByDate(Date date)
    {
        CollectionForm collectionForm;
        try{
            collectionForm = (CollectionForm)getEntityManager().createQuery(" select collectionForm from CollectionForm collectionForm" +
                                                                            " where collectionForm.date = :date ")
                                                               .setParameter("date",date)
                                                               .getSingleResult();
        }catch (NoResultException e)
        {
            return null;
        }
        return collectionForm;
    }

    public void updateProductiveZone(CollectionForm collectionForm)
    {

            List<ProductiveZone> datas = getEntityManager().createQuery("SELECT productiveZone " +
                    "from ProductiveZone productiveZone")
                    .getResultList();

            if(datas.size() > collectionForm.getCollectionRecordList().size())
            {
                for(CollectionRecord collectionRecord :collectionForm.getCollectionRecordList())
                {
                    datas.remove(collectionRecord.getProductiveZone());
                }
                for(ProductiveZone productiveZone: datas)
                {
                    CollectionRecord aux = collectionForm.getCollectionRecordList().get(0);
                    CollectionRecord collectionRecord = new CollectionRecord();
                    collectionRecord.setCompany(aux.getCompany());
                    collectionRecord.setReceivedAmount(0.0);
                    collectionRecord.setWeightedAmount(0.0);
                    collectionRecord.setRejectedAmount(0.0);
                    collectionRecord.setCollectionForm(aux.getCollectionForm());
                    collectionRecord.setProductiveZone(productiveZone);
                    getEntityManager().persist(collectionRecord);
                }

                getEntityManager().flush();
                getEntityManager().refresh(collectionForm);
            }
    }

    @Override
    public void populateWithTotalsOfRejectedAmount(CollectionForm collectionForm) {

    }

    private Map<Long, Double> createMap(Query query) {
        Map<Long, Double> map = new HashMap<Long, Double>();
        List<Object[]> results = query.getResultList();
        for(Object[] result : results) {
            Long id = (Long)result[0];
            Double value = (Double)result[1];
            map.put(id, value);
        }
        return map;
    }

    @Override
    protected Object preCreate(Object entity) {
        updateTotalsForCollectedAndRejectedRawMaterial((CollectionForm)entity);
        return entity;
    }

    @Override
    protected Object preUpdate(Object entity) {
        updateTotalsForCollectedAndRejectedRawMaterial((CollectionForm)entity);
        return entity;
    }

    private void updateTotalsForCollectedAndRejectedRawMaterial(CollectionForm form) {
        populateWithTotalsOfCollectedAmount(form);
        populateWithTotalsOfRejectedAmount(form);
    }

    @SuppressWarnings(value = "unchecked")
    public WarehouseDocumentType getFirstReceptionType() {
        List<WarehouseDocumentType> warehouseDocumentTypeList = getEntityManager()
                .createNamedQuery("WarehouseDocumentType.findByType")
                .setParameter("companyNumber", Constants.defaultCompanyNumber)
                .setParameter("warehouseVoucherType", WarehouseVoucherType.R).getResultList();

        if (!ValidatorUtil.isEmptyOrNull(warehouseDocumentTypeList)) {
            return warehouseDocumentTypeList.get(0);
        }

        return null;
    }
}
