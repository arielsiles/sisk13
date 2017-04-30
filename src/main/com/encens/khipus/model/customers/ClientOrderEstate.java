package com.encens.khipus.model.customers;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 9/01/14
 * Time: 17:35
 * To change this template use File | Settings | File Templates.
 */
public enum ClientOrderEstate {
     PEN("Order.estate.pen")
    //,ECL("Order.estate.ecl")
    ,ANL("Order.estate.anl")
    ,ECH("Order.estate.ech")
    ,ALL("Order.estate.all");

    private String resourceKey;

    private ClientOrderEstate(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public static String getVal(ClientOrderEstate resourceKey){

        if(resourceKey == PEN)
            return "PEN";
        if(resourceKey == ANL)
            return "ANL";
        if(resourceKey == ECH)
            return "ECH";

        return "TODOS";
    }
}
