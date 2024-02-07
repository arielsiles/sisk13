package com.encens.khipus.service.xproduction;

import com.encens.khipus.model.xproduction.MetaProduct;
import com.encens.khipus.model.xproduction.ProductiveZone;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface CollectedRawMaterialCalculatorService {

    public double calculateCollectedAmountBetweenDates(Date startDate,Date endDate, MetaProduct rawMaterial,ProductiveZone productiveZone);

    public double calculateCollectedAmountBetweenDates(Date startDate,Date endDate, MetaProduct rawMaterial);

    public double   calculateCollectedAmount(Date date, MetaProduct rawMaterial);

    public double calculateAvailableAmount(Date date, MetaProduct rawMaterial);

    public double calculateUsedAmount(Date date, MetaProduct rawMaterial);
}
