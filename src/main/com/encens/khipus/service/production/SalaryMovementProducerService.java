package com.encens.khipus.service.production;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.production.SalaryMovementProducerException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.customers.CustomerOrder;
import com.encens.khipus.model.production.*;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface SalaryMovementProducerService extends GenericService {
    public RawMaterialProducerDiscount prepareDiscount(RawMaterialProducer rawMaterialProducer, Date startDate, Date endDate,ProductiveZone productiveZone) throws EntryNotFoundException;

    Double getTotalCollectedByProductor(RawMaterialProducer rawMaterialProducer, Date date);

    public ProductiveZone getZoneProductiveByProductor(RawMaterialProducer rawMaterialProducer);

    public void moveSessionsProductor(RawMaterialProducer rawMaterialProducer, Date date,ProductiveZone productiveZone) throws SalaryMovementProducerException;

    void moveDiscountsProductor(RawMaterialProducer rawMaterialProducer, Date date, ProductiveZone productiveZone) throws SalaryMovementProducerException;

    void createSalaryMovementProducer(CustomerOrder customerOrder);

    List<RawMaterialProducer> findProducersWithCollection(Date startDate, Date endDate);

    void createSalaryMovementProducer(List<SalaryMovementProducer> salaryMovementProducerList);

    List<SalaryMovementProducer> findSalaryMovementProducerList(Date starDate, Date endDate, TypeMovementProducer typeMovementProducer);

}
