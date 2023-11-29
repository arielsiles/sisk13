package com.encens.khipus.action.production;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.production.CollectMaterial;
import com.encens.khipus.model.production.ProductiveZone;
import com.encens.khipus.model.production.RawMaterialProducer;
import com.encens.khipus.service.production.ProducerPriceService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;

import java.math.BigDecimal;

@Name("collectMaterialAction")
@Scope(ScopeType.CONVERSATION)
public class CollectMaterialAction extends GenericAction<CollectMaterial> {

    private RawMaterialProducer rawMaterialProducer;
    private ProductiveZone productiveZone;
    private BigDecimal rawMaterialPrice = BigDecimal.ZERO;

    @In
    private ProducerPriceService producerPriceService;

    @Factory(value = "collectMaterial", scope = ScopeType.STATELESS)
    public CollectMaterial initCollectMaterial() {
        return getInstance();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "code";
    }

    @Override
    @End
    public String create() {

        try {
            CollectMaterial collectMaterial = getInstance();
            //collectMaterial.setPrice(this.rawMaterialPrice);

            //collectMaterial.setProductiveZone(this.productiveZone);
            //ProductItem productItem = getInstance().getProductItem();
            //metaProduct.setProductItem(productItem);
            //metaProduct.setName(productItem.getName());
            //metaProduct.setCode(productItem.getProductItemCode());
            //metaProduct.setProductItemCode(productItem.getProductItemCode());
            //metaProduct.setCompanyNumber(productItem.getCompanyNumber());

            getService().create(collectMaterial);

            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }

    }

    public void updateProducerPrice(){
        CollectMaterial instance = getInstance();
        //setRawMaterialPrice(instance.getMetaProduct().getPrice());
        instance.setPrice(instance.getMetaProduct().getPrice());

        BigDecimal price = producerPriceService.findProducerPrice(instance.getMetaProduct(), instance.getProducer());
        if ( price.doubleValue() > 0 ) {
            //setRawMaterialPrice(price);
            instance.setPrice(price);
        }
    }

    public void assignSupplier(RawMaterialProducer supplier){
        setRawMaterialProducer(supplier);
        getInstance().setProducer(supplier);
    }

    public void assignOrigin(ProductiveZone productiveZone){
        setProductiveZone(productiveZone);
        getInstance().setProductiveZone(productiveZone);
    }

    public void clearRawMaterialProducer() {
        getInstance().setProducer(null);
    }

    public void clearOrigin() {
        getInstance().setProductiveZone(null);
    }

    public BigDecimal getRawMaterialPrice() {
        return rawMaterialPrice;
    }

    public void setRawMaterialPrice(BigDecimal rawMaterialPrice) {
        this.rawMaterialPrice = rawMaterialPrice;
    }

    public RawMaterialProducer getRawMaterialProducer() {
        return rawMaterialProducer;
    }

    public void setRawMaterialProducer(RawMaterialProducer rawMaterialProducer) {
        this.rawMaterialProducer = rawMaterialProducer;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }
}
