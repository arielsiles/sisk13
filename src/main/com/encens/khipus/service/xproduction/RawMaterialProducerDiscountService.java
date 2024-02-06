package com.encens.khipus.service.xproduction;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.xproduction.RawMaterialProducer;
import com.encens.khipus.model.xproduction.RawMaterialProducerDiscount;
import com.encens.khipus.model.xproduction.SalaryMovementProducer;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface RawMaterialProducerDiscountService extends GenericService {
    public RawMaterialProducerDiscount prepareDiscount(RawMaterialProducer rawMaterialProducer) throws EntryNotFoundException;
    public SalaryMovementProducer prepareDiscountSalary(RawMaterialProducer rawMaterialProducer,Date startDate,Date endDate) throws EntryNotFoundException;
}
