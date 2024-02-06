package com.encens.khipus.service.xproduction;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.ProductiveZone;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface SalaryMovementGABService extends GenericService {
    public Double getAlcoholBayGAB(ProductiveZone productiveZone, Date startDate, Date endDate) throws EntryNotFoundException;
}
