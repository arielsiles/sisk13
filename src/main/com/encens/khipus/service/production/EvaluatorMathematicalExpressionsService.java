package com.encens.khipus.service.production;

import com.encens.khipus.exception.production.ProductCompositionException;
import com.encens.khipus.model.production.OrderInput;
import com.encens.khipus.model.production.ProductComposition;
import com.encens.khipus.model.production.ProductionIngredient;
import com.encens.khipus.model.production.ProductionOrder;
import com.encens.khipus.model.warehouse.ProductItem;

import javax.ejb.Local;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Local
public interface EvaluatorMathematicalExpressionsService {

    void executeMathematicalFormulas(ProductComposition productComposition) throws ProductCompositionException, IOException;

    void executeMathematicalFormulas(ProductionOrder productionOrder) throws ProductCompositionException, IOException;

    public void excuteFormulate(ProductionOrder productionOrder, Double formulateContainer, Double formulateSupposed) throws ProductCompositionException, IOException;

    public void excuteParemeterizadFormulate(ProductionOrder productionOrder, Double formulateContainer, Double formulateSupposed) throws ProductCompositionException, IOException;

    public Double excuteParemeterized(OrderInput input,ProductionOrder productionOrder, Double containerWeight, Double supposedAmount)throws ProductCompositionException, IOException;

    public Double getAmountExpected(Double expectedOld, Double containerOld, Double containerNew)throws ProductCompositionException, IOException;

    public BigDecimal getMountInWarehouse(ProductItem item);

    public void excuteFormulate(List<ProductionIngredient> ingredients, Double  expendAmount, Double container,Double supposed) throws IOException, ProductCompositionException;
}
