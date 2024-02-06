package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.SalaryMovementProducerTypeEnum;
import com.encens.khipus.model.xproduction.TypeMovementProducer;

import javax.ejb.Local;

/**
 * @author
 * @version 2.26
 */
@Local
public interface TypeMovementProducerService {

    TypeMovementProducer findTypeMovementProducer(SalaryMovementProducerTypeEnum salaryMovementProducerTypeEnum);

}
