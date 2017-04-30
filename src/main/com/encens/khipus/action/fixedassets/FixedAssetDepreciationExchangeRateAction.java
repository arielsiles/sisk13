package com.encens.khipus.action.fixedassets;

import com.encens.khipus.service.fixedassets.FixedAssetService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author
 */
@Name("fixedAssetDepreciationExchangeRateAction")
@Scope(ScopeType.PAGE)

public class FixedAssetDepreciationExchangeRateAction implements Serializable {
    @In
    protected Map<String, String> messages;

    @In
    FixedAssetService fixedAssetService;
    private BigDecimal exchangeRate;
    @In
    private FacesMessages facesMessages;


    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
