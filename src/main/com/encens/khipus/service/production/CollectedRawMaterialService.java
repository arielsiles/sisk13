package com.encens.khipus.service.production;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.exception.ReferentialIntegrityException;
import com.encens.khipus.model.production.CollectedRawMaterial;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialProducer;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 6/7/13
 * Time: 11:20 AM
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface CollectedRawMaterialService {

//    public List<CollectedRawMaterial> prepareRawMaterialCollection(ProductiveZone productiveZone, Date date);

    void save(ProductiveZone productiveZone, List<CollectedRawMaterial> collectedRawMaterialList, Date date) throws ConcurrencyException, EntryDuplicatedException;

    void delete(ProductiveZone productiveZone, Date date) throws ConcurrencyException, ReferentialIntegrityException;

    boolean getHasCollected(Date startDate, Date endDate, RawMaterialProducer rawMaterialProducer);

    boolean getHasDiscounts(Date startDate, Date endDate, RawMaterialProducer rawMaterialProducer);
}
