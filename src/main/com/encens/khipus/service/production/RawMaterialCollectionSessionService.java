package com.encens.khipus.service.production;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialCollectionSession;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface RawMaterialCollectionSessionService extends GenericService {
    void updateRawMaterialProducer(RawMaterialCollectionSession rawMaterialCollectionSession,ProductiveZone productiveZone);
    public List<RawMaterialCollectionSession> getRawMaterialCollectionSessionByDateAndProductiveZone(ProductiveZone productiveZone, Date date);
    public List<RawMaterialCollectionSession> getRawMaterialCollectionSessionByDateAndProductiveZone(ProductiveZone productiveZone, Date startDate, Date endDate);
    public List<RawMaterialCollectionSession> getNextRawMaterialCollectionSessionByDateAndProductiveZone(ProductiveZone productiveZone, Date date);
}
