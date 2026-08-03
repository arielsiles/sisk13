package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.Provider;
import com.encens.khipus.model.finances.ProviderPk;
import com.encens.khipus.model.purchases.PayConditions;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.warehouse.Warehouse;
import com.encens.khipus.service.finances.FinanceProviderService;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Holder de sesion dedicado para persistir los filtros de la lista de Ordenes de
 * Compra al volver de una OC, SIN cambiar el scope del dataModel.
 *
 * warehousePurchaseOrderDataModel es @Scope(PAGE) (convencion del sistema) y ademas
 * se comparte con accountEntriesList, por lo que no se puede pasar a SESSION sin
 * acoplar ambas vistas. En su lugar, este holder (SESSION, exclusivo de la lista de
 * OC) guarda los filtros al Buscar y los restaura al reingresar a la lista via
 * page-action, dejando el dataModel PAGE intacto.
 */
@Name("warehousePurchaseOrderFilterHolder")
@Scope(ScopeType.SESSION)
public class WarehousePurchaseOrderFilterHolder implements Serializable {

    @In
    private FinanceProviderService financeProviderService;

    private boolean saved = false;

    // criteria (orderNumber, invoiceNumber, gloss, state) se guarda como objeto
    private PurchaseOrder savedCriteria;
    // filtros que viven directamente en el dataModel
    private Date startDate;
    private Date endDate;
    /**
     * Del proveedor se guarda SOLO la clave, nunca la instancia: este holder es de
     * SESSION y los entity manager de lista (listEntityManager /
     * businessUnitListEntityManager) son de EVENT. Una entidad retenida entre requests
     * queda desacoplada, y su asociacion lazy 'entity' (FinancesEntity) revienta con
     * LazyInitializationException al leerse en el render del filtro.
     */
    private ProviderPk providerId;
    private Warehouse warehouse;
    private CollectionDocumentType documentType;
    private PayConditions payConditions;

    private WarehousePurchaseOrderDataModel model() {
        return (WarehousePurchaseOrderDataModel)
                Component.getInstance("warehousePurchaseOrderDataModel", true);
    }

    /**
     * Recarga el proveedor con el entity manager del request actual. El named query
     * Provider.findById hace 'left join fetch p.entity', de modo que la instancia
     * devuelta trae la entidad ya inicializada y se puede leer aunque despues se
     * desacople.
     */
    private Provider loadProvider() {
        if (providerId == null) {
            return null;
        }
        try {
            return financeProviderService.findById(providerId);
        } catch (EntryNotFoundException e) {
            return null;
        }
    }

    /** Accion del boton Buscar: ejecuta la busqueda y guarda los filtros en sesion. */
    public void searchAndSave() {
        WarehousePurchaseOrderDataModel m = model();
        m.search();
        savedCriteria = m.getCriteria();
        startDate = m.getStartDate();
        endDate = m.getEndDate();
        providerId = m.getProvider() != null ? m.getProvider().getId() : null;
        warehouse = m.getWarehouse();
        documentType = m.getDocumentType();
        payConditions = m.getPayConditions();
        saved = true;
    }

    /**
     * Accion del boton Limpiar: limpia todos los filtros pero deja el rango de fechas
     * en el valor por defecto (1-ene del anio actual a hoy), y re-consulta. Olvida los
     * filtros guardados en sesion.
     */
    public void clearAndSave() {
        WarehousePurchaseOrderDataModel m = model();
        m.clear();
        applyDefaultDates(m);
        m.updateAndSearch();
        saved = false;
        savedCriteria = null;
        startDate = null;
        endDate = null;
        providerId = null;
        warehouse = null;
        documentType = null;
        payConditions = null;
    }

    /**
     * Page-action al entrar a la lista (acceso inicial o redirect al volver de una OC).
     * Restaura los filtros guardados o, si es la primera vez, aplica el rango por
     * defecto (1-ene del anio actual a hoy); luego re-consulta con datos frescos.
     *
     * Se fuerza getRowCount() primero para que QueryDataModel inicialice su entityQuery
     * y su criteria: de lo contrario el primer render recrearia el criteria (en
     * initEntityQuery) y perderia lo restaurado.
     */
    public void restoreAndSearch() {
        WarehousePurchaseOrderDataModel m = model();
        m.getRowCount();
        if (saved) {
            if (savedCriteria != null) {
                m.setCriteria(savedCriteria);
            }
            m.setStartDate(startDate);
            m.setEndDate(endDate);
            m.setProvider(loadProvider());
            m.setWarehouse(warehouse);
            m.setDocumentType(documentType);
            m.setPayConditions(payConditions);
        } else {
            applyDefaultDates(m);
        }
        m.updateAndSearch();
    }

    /** Rango por defecto: del 1 de enero del anio actual a la fecha actual. */
    private void applyDefaultDates(WarehousePurchaseOrderDataModel m) {
        Calendar cal = Calendar.getInstance();
        m.setEndDate(cal.getTime());
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        m.setStartDate(cal.getTime());
    }
}
