package com.encens.khipus.service.xproduction;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.xproduction.ProductiveZone;
import com.encens.khipus.model.xproduction.RawMaterialProducer;

import javax.ejb.Local;
import java.util.List;


@Local
public interface RawMaterialProducerService extends GenericService {

//    public List<RawMaterialProducer> findAllThatDontHaveCollectedRawMaterial(ProductiveZone productiveZone, Date date);

    public List<RawMaterialProducer> findAll(ProductiveZone productiveZone);

    RawMaterialProducer findProducerByIdNumber(String idNumber);

}
