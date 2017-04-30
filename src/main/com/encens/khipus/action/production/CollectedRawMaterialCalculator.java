package com.encens.khipus.action.production;


import com.encens.khipus.model.production.CollectionForm;
import com.encens.khipus.service.production.CollectedRawMaterialCalculatorService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Calendar;
import java.util.Date;

@Name("collectedRawMaterialCalculator")
@Scope(ScopeType.PAGE)
public class CollectedRawMaterialCalculator {

    @In
    private CollectedRawMaterialCalculatorService collectedRawMaterialCalculatorService;

    private Date originalDate;

    private double collectedRawMaterialAmount;
    private double availableRowMaterialAmount;
    private double usedRowMaterialAmount;

    public double calculateCollectedAmount(CollectionForm collectionForm) {
        originalDate = collectionForm.getDate();
        collectedRawMaterialAmount = collectedRawMaterialCalculatorService.calculateCollectedAmount(collectionForm.getDate(), collectionForm.getMetaProduct());
        return collectedRawMaterialAmount;
    }

    public double calculateAvailableAmount(CollectionForm collectionForm) {
        Date date = collectionForm.getDate();
        if (date != originalDate) {
            throw new RuntimeException();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        availableRowMaterialAmount = collectedRawMaterialCalculatorService.calculateAvailableAmount(calendar.getTime(), collectionForm.getMetaProduct());
        availableRowMaterialAmount = availableRowMaterialAmount + collectedRawMaterialAmount;
        return availableRowMaterialAmount;
    }

    public double calculateUsedAmount(CollectionForm collectionForm) {
        Date date = collectionForm.getDate();
        if (date != originalDate) {
            throw new RuntimeException();
        }

        usedRowMaterialAmount = collectedRawMaterialCalculatorService.calculateUsedAmount(date, collectionForm.getMetaProduct());
        return usedRowMaterialAmount;
    }

    public double calculateRemainingAmount() {
        return availableRowMaterialAmount - usedRowMaterialAmount;
    }
}
