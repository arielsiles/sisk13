package com.encens.khipus.service.production;

import com.encens.khipus.model.production.MetaProduct;
import com.encens.khipus.model.production.RawMaterialProducer;

import javax.ejb.Local;
import java.math.BigDecimal;

@Local
public interface ProducerPriceService {
    BigDecimal findProducerPrice(MetaProduct metaProduct, RawMaterialProducer producer);
}
