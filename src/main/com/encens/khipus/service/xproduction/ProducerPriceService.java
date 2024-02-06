package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.MetaProduct;
import com.encens.khipus.model.xproduction.RawMaterialProducer;

import javax.ejb.Local;
import java.math.BigDecimal;

@Local
public interface ProducerPriceService {
    BigDecimal findProducerPrice(MetaProduct metaProduct, RawMaterialProducer producer);
}
