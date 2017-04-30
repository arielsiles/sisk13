package com.encens.khipus.service.production;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.model.production.RawMaterialProducerDiscount;
import com.encens.khipus.model.production.SalaryMovementProducer;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface RawMaterialProducerDiscountService extends GenericService {
    public RawMaterialProducerDiscount prepareDiscount(RawMaterialProducer rawMaterialProducer) throws EntryNotFoundException;
    public SalaryMovementProducer prepareDiscountSalary(RawMaterialProducer rawMaterialProducer,Date startDate,Date endDate) throws EntryNotFoundException;
}
