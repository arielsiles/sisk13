package com.encens.khipus.util.barcode;

import org.krysalis.barcode4j.BarcodeClassResolver;
import org.krysalis.barcode4j.DefaultBarcodeClassResolver;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.ConfigurableBarcodeGenerator;

/**
 * @author
 * @version 3.1
 */
public class BarcodeBeanFactory {
    public static final BarcodeBeanFactory i = new BarcodeBeanFactory();

    private BarcodeClassResolver resolver;

    private BarcodeBeanFactory() {
    }

    public AbstractBarcodeBean getBarcodeBean(String type) {
        AbstractBarcodeBean bean = null;
        try {
            Class clazz = getResolver().resolve(type);
            bean = ((ConfigurableBarcodeGenerator) clazz.newInstance()).getBean();
            bean.doQuietZone(true);
        } catch (ClassNotFoundException e) {
            //
        } catch (InstantiationException e) {
            //
        } catch (IllegalAccessException e) {
            //
        }

        return bean;
    }

    private BarcodeClassResolver getResolver() {
        if (null == resolver) {
            resolver = new DefaultBarcodeClassResolver();
        }

        return resolver;
    }
}
