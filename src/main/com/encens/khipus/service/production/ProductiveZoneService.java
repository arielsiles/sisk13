package com.encens.khipus.service.production;

import com.encens.khipus.model.production.ProductiveZone;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 5/29/13
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
@Local
public interface ProductiveZoneService {

    public List<ProductiveZone> findAll();

    public List<ProductiveZone> findAllThatDoNotHaveCollectionForm(Date startDate,Date endDate);

    public ProductiveZone find(long id);
}
