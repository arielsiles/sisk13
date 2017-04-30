package com.encens.khipus.util.warehouse;

import com.encens.khipus.model.warehouse.MovementDetailType;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import com.encens.khipus.model.warehouse.WarehouseVoucherType;

/**
 * @author
 * @version 2.0
 */
public final class WarehouseUtil {
    private WarehouseUtil() {
    }

    public static MovementDetailType getMovementTye(WarehouseDocumentType documentType) {
        if (WarehouseVoucherType.getInputTypes().contains(documentType.getWarehouseVoucherType())) {
            return MovementDetailType.E;
        }

        if (WarehouseVoucherType.getOutputTypes().contains(documentType.getWarehouseVoucherType())) {
            return MovementDetailType.S;
        }

        return null;
    }
}
