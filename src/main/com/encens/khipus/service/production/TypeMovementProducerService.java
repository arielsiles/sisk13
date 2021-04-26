package com.encens.khipus.service.production;

import com.encens.khipus.model.production.SalaryMovementProducerTypeEnum;
import com.encens.khipus.model.production.TypeMovementProducer;

import javax.ejb.Local;

/**
 * @author
 * @version 2.26
 */
@Local
public interface TypeMovementProducerService {

    TypeMovementProducer findTypeMovementProducer(SalaryMovementProducerTypeEnum salaryMovementProducerTypeEnum);

}
