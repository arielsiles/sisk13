package com.encens.khipus.service.production;

import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.model.production.CollectMaterial;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Local
public interface CollectMaterialService {
    List<CollectMaterial> findCollectMaterialNoAccounting(Date startDate, Date endDate);

    List<CollectMaterial> findApprovedCollectMaterial(Date startDate, Date endDate);
    List<CollectMaterial> findApprovedCollectMaterial(Date startDate, Date endDate, String warehouseCode);
    List<CollectMaterial> findApprovedCollectMaterialByCode(String productItemCode, Date startDate, Date endDate);

    String createCollectMaterialListAccounting(List<CollectMaterial> collectMaterialList,Date starDate,Date endDate);

    /** Asiento vinculado al acopio (acopiomp.id_tmpenc); null si no tiene o no existe. */
    Voucher findVoucher(CollectMaterial collectMaterial);

    /** Antiguedad maxima en dias para revertir un acopio; 0 = sin limite. */
    int getRevertWindowDays();

    /** Saldo actual del articulo del acopio, en la unidad del inventario. */
    BigDecimal findCurrentBalance(CollectMaterial collectMaterial);

    /** Costo unitario actual del articulo del acopio. */
    BigDecimal findCurrentUnitCost(CollectMaterial collectMaterial);

    /** Costo unitario en que quedaria el articulo si se revierte este acopio. */
    BigDecimal findProjectedUnitCost(CollectMaterial collectMaterial);

    /** Revierte el acopio a PENDIENTE: deshace el inventario, marca la correccion y
     *  escribe la bitacora. Las guardas se validan antes, en la accion. */
    void revert(CollectMaterial collectMaterial, String reason);

    List<Object[]> findCollectMaterial();

    List<Object[]> findCollectMaterialByProducer();

    List<Object[]> findCollectMaterialByZone();
}
