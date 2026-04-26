package com.encens.khipus.action.warehouse;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.exception.finances.DuplicatedFinanceAccountingDocumentException;
import com.encens.khipus.exception.finances.FinancesCurrencyNotFoundException;
import com.encens.khipus.exception.finances.FinancesExchangeRateNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseDocumentAmountException;
import com.encens.khipus.exception.purchase.PurchaseDocumentException;
import com.encens.khipus.exception.purchase.PurchaseDocumentNotFoundException;
import com.encens.khipus.exception.purchase.PurchaseDocumentStateException;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.interceptor.BusinessUnitRestrict;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CollectionDocumentType;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.FinancesEntity;
import com.encens.khipus.model.purchases.PurchaseDocument;
import com.encens.khipus.model.purchases.PurchaseDocumentState;
import com.encens.khipus.service.finances.FinancesExchangeRateService;
import com.encens.khipus.service.purchases.PurchaseDocumentService;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.ValidatorUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Action de soporte para el modal de Documento de Compra que se abre
 * directamente desde la pantalla de Orden de Compra
 * (warehousePurchaseOrder.xhtml). Coexiste con
 * {@link WarehousePurchaseDocumentAction} (flujo de pagina completa).
 *
 * Convive con la conversation de la OC sin abrir nested conversations:
 * mantiene su propia copia de PurchaseDocument que se reinicia con cada
 * apertura del modal.
 *
 * @version 6.0.72
 */
@Name("warehousePurchaseDocumentModalAction")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
@BusinessUnitRestrict
public class WarehousePurchaseDocumentModalAction implements Serializable {

    private static final long serialVersionUID = 1L;


    @Logger
    private Log log;

    @In(value = "warehousePurchaseOrderAction")
    private WarehousePurchaseOrderAction warehousePurchaseOrderAction;

    @In
    private PurchaseDocumentService purchaseDocumentService;

    @In
    private FinancesExchangeRateService financesExchangeRateService;

    @In
    protected FacesMessages facesMessages;

    /** EM de la conversation para queries directas (no cacheadas) de la grilla. */
    @In(value = "#{entityManager}")
    private EntityManager entityManager;

    /** Documento sobre el que opera el modal (nuevo o existente). */
    private PurchaseDocument instance;

    /** True si el modal abrio para crear, false si abrio para revisar uno existente. */
    private boolean creatingNew;

    /**
     * Flag consultado desde oncomplete del modal para decidir si cerrarlo.
     * Se setea al final de cada operacion (save/approve/nullify) segun haya
     * tenido exito o no. Asi, si una validacion fallo, el modal queda
     * abierto y el usuario conserva los datos cargados.
     */
    private boolean lastOperationSucceeded;

    // ------------------------------------------------------------------
    // Apertura del modal
    // ------------------------------------------------------------------

    /**
     * Inicializa una nueva instancia con valores por defecto, para que el
     * modal aparezca con la OC preseleccionada y el proveedor heredado.
     * Invocado por el boton "Registrar factura/recibo".
     */
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENTMODAL_REGISTER','VIEW')}")
    public String openForCreate() {
        instance = new PurchaseDocument();
        creatingNew = true;
        applyDefaultValues(instance);
        // Devolver null en acciones AJAX-only para evitar disparar
        // navigation rules y/o redirect al no-conversation-view-id.
        return null;
    }

    /**
     * Carga un documento existente desde DB y lo deja listo para revisar
     * o aprobar/anular en el modal. Invocado por el icono ver16.png en la
     * grilla de documentos de la pestana "Documentos" de la OC.
     */
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENTMODAL_REGISTER','VIEW')}")
    public String openForReview(PurchaseDocument purchaseDocument) {
        if (purchaseDocument == null || purchaseDocument.getId() == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "Common.error.notFound", "");
            return Outcome.FAIL;
        }
        // CRITICO: usar la instancia que viene del dataTable directamente,
        // NO recargarla via service.readDocument(id). Razon: el dataTable
        // tiene esta misma entidad en wrappedData (cache PAGE-scoped). Si
        // recargamos, terminamos operando sobre una copia distinta y las
        // mutaciones (setState en approve/nullify) no se reflejan en la
        // entidad cacheada que el grid muestra. Mismo patron que sigue
        // CustomerOrderAction.cancelOrderInvoice(customerOrder).
        instance = purchaseDocument;
        creatingNew = false;
        recoverFinancesEntityFromProvider();
        return null;
    }

    public void closeModal() {
        instance = null;
        creatingNew = false;
    }

    // ------------------------------------------------------------------
    // Operaciones
    // ------------------------------------------------------------------

    /**
     * Crea un nuevo documento o actualiza uno existente segun el estado del
     * modal. Mantiene paridad con PurchaseDocumentAction.create()/update().
     */
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENTMODAL_REGISTER','VIEW')}")
    public String save() {
        lastOperationSucceeded = false;
        if (instance == null) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "PurchaseDocument.modal.error.notAllowed");
            return Outcome.FAIL;
        }
        try {
            applyAdjustmentDefaults(instance);
            if (creatingNew || instance.getId() == null) {
                purchaseDocumentService.createDocument(instance);
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                        "PurchaseDocument.modal.success.created");
                creatingNew = false;
            } else {
                purchaseDocumentService.updateDocument(instance);
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                        "PurchaseDocument.modal.success.updated");
            }
            refreshPurchaseOrderInstance();
            lastOperationSucceeded = true;
            // null en acciones AJAX-only para evitar disparar navigation rules
            // (incluido el no-conversation-view-id que redirige al listado).
            return null;
        } catch (PurchaseDocumentException e) {
            addPurchaseDocumentErrorMessages(e.getErrorTypes());
            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentAmountException e) {
            addPurchaseDocumentAmountErrorMessage(e.getLimit());
            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentStateException e) {
            addPurchaseDocumentStateErrorMessage(e.getCurrentState());
            return Outcome.FAIL;
        } catch (PurchaseDocumentNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "Common.error.notFound", instance.getNumber());
            return Outcome.FAIL;
        } catch (ConcurrencyException e) {
            try {
                instance = purchaseDocumentService.readDocument(instance.getId());
            } catch (PurchaseDocumentNotFoundException e1) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                        "Common.error.notFound", "");
            }
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "Common.concurrency.update");
            return Outcome.REDISPLAY;
        }
    }

    /**
     * Aprueba el documento (PENDING -> APPROVED). Valida estado, calcula
     * netAmount para ajustes y delega en el servicio que ademas genera el
     * asiento contable correspondiente.
     */
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENTMODAL_APPROVE','VIEW')}")
    public String approve() {
        lastOperationSucceeded = false;
        if (instance == null || !isInstancePending()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "PurchaseDocument.modal.error.notAllowed");
            return Outcome.FAIL;
        }
        try {
            applyAdjustmentDefaults(instance);
            purchaseDocumentService.approveDocument(instance);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "PurchaseDocument.modal.success.approved",
                    instance.getNumber());
            refreshPurchaseOrderInstance();
            lastOperationSucceeded = true;
            return null;
        } catch (PurchaseDocumentStateException e) {
            addPurchaseDocumentStateErrorMessage(e.getCurrentState());
            return Outcome.FAIL;
        } catch (PurchaseDocumentAmountException e) {
            addPurchaseDocumentAmountErrorMessage(e.getLimit());
            return Outcome.REDISPLAY;
        } catch (PurchaseDocumentNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "Common.error.notFound", instance.getNumber());
            return Outcome.FAIL;
        } catch (DuplicatedFinanceAccountingDocumentException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "FinanceAccountDocument.error.duplicatedFinanceAccountingDocument",
                    e.getDuplicateId().getEntityCode(),
                    e.getDuplicateId().getInvoiceNumber(),
                    e.getDuplicateId().getAuthorizationNumber());
            return Outcome.FAIL;
        } catch (CompanyConfigurationNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "CompanyConfiguration.error.notFound");
            return Outcome.FAIL;
        }
    }

    /**
     * Anula un documento PENDING o APPROVED. Bloqueado si la OC esta
     * FINALIZED o LIQUIDATED (en esos estados los documentos quedan
     * congelados y solo pueden moverse via reversion completa de la OC).
     */
    @Restrict("#{s:hasPermission('PURCHASEDOCUMENTMODAL_NULLIFY','VIEW')}")
    public String nullify() {
        lastOperationSucceeded = false;
        if (!isCanNullify()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "PurchaseDocument.modal.error.notAllowed");
            return Outcome.FAIL;
        }
        try {
            purchaseDocumentService.nullifyDocument(instance);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "PurchaseDocument.modal.success.nullified");
            refreshPurchaseOrderInstance();
            lastOperationSucceeded = true;
            return null;
        } catch (PurchaseDocumentNotFoundException e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "Common.error.notFound", instance.getNumber());
            return Outcome.FAIL;
        } catch (PurchaseDocumentStateException e) {
            addPurchaseDocumentStateErrorMessage(e.getCurrentState());
            return Outcome.FAIL;
        }
    }

    // ------------------------------------------------------------------
    // Helpers usados por la UI (rendered)
    // ------------------------------------------------------------------

    /**
     * Indica si la OC actual permite registrar un nuevo documento.
     * Mismo criterio que el flujo de pagina (no nulificada).
     */
    public boolean isCanRegister() {
        if (warehousePurchaseOrderAction == null
                || warehousePurchaseOrderAction.getInstance() == null) {
            return false;
        }
        return !warehousePurchaseOrderAction.getInstance().isPurchaseOrderNullified();
    }

    /** True solo cuando el modal trabaja sobre un documento PENDING. */
    public boolean isInstancePending() {
        return instance != null && PurchaseDocumentState.PENDING.equals(instance.getState());
    }

    public boolean isInstanceApproved() {
        return instance != null && PurchaseDocumentState.APPROVED.equals(instance.getState());
    }

    public boolean isInstanceNullified() {
        return instance != null && PurchaseDocumentState.NULLIFIED.equals(instance.getState());
    }

    /**
     * True cuando el documento ya fue aprobado o anulado: sus campos pasan
     * a ser de solo lectura. La creacion (sin instance.id) no es read-only.
     */
    public boolean isInstanceReadOnly() {
        return isEditingExisting() && (isInstanceApproved() || isInstanceNullified());
    }

    /**
     * Indica si el boton "Anular" del modal debe estar habilitado.
     * Reglas:
     *  - El documento debe ser existente (no creando uno nuevo).
     *  - Estado del documento: PENDING o APPROVED (NULLIFIED ya esta anulado).
     *  - La OC NO debe estar FINALIZED ni LIQUIDATED (en esos estados los
     *    documentos quedan congelados; revertirlos requiere reversion completa
     *    de la OC desde el flujo dedicado).
     */
    public boolean isCanNullify() {
        if (!isEditingExisting()) return false;
        if (!isInstancePending() && !isInstanceApproved()) return false;
        if (warehousePurchaseOrderAction == null
                || warehousePurchaseOrderAction.getInstance() == null) {
            return false;
        }
        return !warehousePurchaseOrderAction.getInstance().isPurchaseOrderFinalized()
                && !warehousePurchaseOrderAction.getInstance().isPurchaseOrderLiquidated();
    }

    /** True para rendering del titulo "Nuevo documento" vs "Documento de compra". */
    public boolean isCreatingNew() {
        return creatingNew;
    }

    public boolean isEditingExisting() {
        return !creatingNew && instance != null && instance.getId() != null;
    }

    /**
     * Consultado desde el oncomplete del modal para decidir cerrarlo o no.
     * True si la ultima operacion (save/approve/nullify) tuvo exito sin
     * lanzar excepciones de validacion.
     */
    public boolean isLastOperationSucceeded() {
        return lastOperationSucceeded;
    }

    /**
     * Lazy init: el modal se renderiza al cargar la pagina aunque el usuario
     * todavia no haya hecho click. Para que las EL no exploten con NPE,
     * mantenemos siempre una instancia disponible (con defaults heredados de
     * la OC). openForCreate la reemplaza por una nueva al hacer click.
     */
    public PurchaseDocument getInstance() {
        if (instance == null) {
            instance = new PurchaseDocument();
            applyDefaultValues(instance);
            creatingNew = true;
        }
        return instance;
    }

    public void setInstance(PurchaseDocument instance) {
        this.instance = instance;
    }

    /**
     * Lista de documentos de compra de la OC actual, consultada directamente
     * a la DB en cada acceso (sin cache). Mismo patron que utiliza
     * salesAction.articleOrderList en salesBox.xhtml: el dataTable se bindea
     * a un getter del action y JSF re-evalua en cada render, asi que tras
     * cualquier mutacion (save/approve/nullify) el siguiente reRender muestra
     * el estado actual sin necesidad de gestionar caches de QueryDataModel.
     *
     * Se prefiere este patron sobre warehousePurchaseDocumentDataModel
     * (PAGE-scoped, con wrappedKeys/wrappedData/detached cacheados) que en
     * la grilla del modal mostraba estados viejos tras approve/nullify por
     * mas que se reRender el dataTable o se forzaran update/search/eviction.
     *
     * El refresco automatico tras refreshFromConversationContext o un commit
     * en la DB queda garantizado: cada render = nueva query.
     */
    @SuppressWarnings("unchecked")
    public List<PurchaseDocument> getDocumentList() {
        if (warehousePurchaseOrderAction == null
                || warehousePurchaseOrderAction.getInstance() == null
                || warehousePurchaseOrderAction.getInstance().getId() == null) {
            return Collections.emptyList();
        }
        return (List<PurchaseDocument>) entityManager.createQuery(
                "select pd from PurchaseDocument pd"
                        + " where pd.purchaseOrder.id = :ocId"
                        + " order by pd.date desc, pd.id desc")
                .setParameter("ocId", warehousePurchaseOrderAction.getInstance().getId())
                .getResultList();
    }

    public List<CollectionDocumentType> getPurchaseDocumentTypeList() {
        List<CollectionDocumentType> list = new ArrayList<CollectionDocumentType>();
        if (warehousePurchaseOrderAction.getInstance() != null
                && warehousePurchaseOrderAction.getInstance().getDocumentType() != null) {
            list.add(warehousePurchaseOrderAction.getInstance().getDocumentType());
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Acciones auxiliares (popups, AJAX onchange)
    // ------------------------------------------------------------------

    public void assignFinancesEntity(FinancesEntity financesEntity) {
        if (instance == null) return;
        instance.setFinancesEntity(financesEntity);
        if (financesEntity != null) {
            instance.setNit(financesEntity.getNitNumber());
            instance.setName(financesEntity.getAcronym());
        }
    }

    public void clearFinancesEntity() {
        if (instance == null) return;
        instance.setFinancesEntity(null);
        instance.setNit(null);
        instance.setName(null);
    }

    public void assignCashAccountAdjustment(CashAccount cashAccount) {
        if (instance == null || cashAccount == null) return;
        instance.setCashAccountAdjustment(cashAccount);
        instance.setCurrency(cashAccount.getCurrency());
        updateExchangeRate();
    }

    public void clearCashAccountAdjustment() {
        if (instance == null) return;
        instance.setCashAccountAdjustment(null);
        instance.setCurrency(null);
    }

    public void updateDocumentType() {
        if (instance == null) return;
        instance.setCurrency(FinancesCurrencyType.P);
        instance.setCashAccountAdjustment(null);
    }

    public void updateExchangeRate() {
        if (instance == null) return;
        BigDecimal exchangeRate = BigDecimal.ONE;
        if (instance.getCurrency() != null && FinancesCurrencyType.D.equals(instance.getCurrency())) {
            try {
                exchangeRate = financesExchangeRateService.findLastExchangeRateByCurrency(
                        FinancesCurrencyType.D.name());
            } catch (FinancesCurrencyNotFoundException e) {
                log.debug("Last exchange rate not loaded.");
            } catch (FinancesExchangeRateNotFoundException e) {
                log.debug("No exchange rate available.");
            }
        }
        instance.setExchangeRate(exchangeRate);
    }

    // ------------------------------------------------------------------
    // Utiles privados
    // ------------------------------------------------------------------

    private void applyDefaultValues(PurchaseDocument doc) {
        if (warehousePurchaseOrderAction.getInstance() == null) {
            return;
        }
        doc.setPurchaseOrder(warehousePurchaseOrderAction.getInstance());
        doc.setPurchaseOrderId(warehousePurchaseOrderAction.getInstance().getId());
        doc.setState(PurchaseDocumentState.PENDING);
        doc.setIce(BigDecimal.ZERO);
        doc.setExempt(BigDecimal.ZERO);
        doc.setRates(BigDecimal.ZERO);
        doc.setDiscounts(BigDecimal.ZERO);
        doc.setNoTaxCredit(BigDecimal.ZERO);
        doc.setControlCode("0");
        doc.setCurrency(FinancesCurrencyType.P);
        doc.setExchangeRate(BigDecimal.ONE);
        doc.setType(warehousePurchaseOrderAction.getInstance().getDocumentType());
        if (warehousePurchaseOrderAction.getInstance().getProvider() != null) {
            assignFinancesEntity(warehousePurchaseOrderAction.getInstance().getProvider().getEntity());
        }
    }

    /**
     * Refresca las grillas PAGE-scoped tras una operacion del modal
     * (save/approve/nullify). Estrategia: evictar los componentes PAGE
     * de TODOS los lugares donde Seam y JSF los puedan cachear:
     *
     *   1. Contexts.getPageContext() — el page context "logico" de Seam.
     *   2. UIViewRoot.getAttributes() — donde Seam materialmente persiste
     *      los componentes PAGE entre requests via JSF view state.
     *
     * Tras evictar, en el siguiente render el dataTable evalua su EL,
     * Seam no encuentra la instancia, ejecuta el factory y crea una nueva
     * con detached=false / wrappedKeys=null. walk() arranca limpio y
     * ejecuta SELECT contra DB, mostrando el estado actual.
     *
     * Esto es clave para approve/nullify: el row con MISMO ID cambia de
     * PENDING a APPROVED/NULLIFIED. Sin re-query, walk reusa el entity
     * cacheado en wrappedData con el estado viejo.
     */
    private void refreshPurchaseOrderInstance() {
        evictFromAllScopes("warehousePurchaseDocumentDataModel");
        evictFromAllScopes("warehousePurchaseOrderDetailDataModel");
    }

    private void evictFromAllScopes(String name) {
        try {
            if (Contexts.isPageContextActive()) {
                Contexts.getPageContext().remove(name);
            }
        } catch (RuntimeException ignored) {
            // page context puede no estar activo en algunos paths
        }
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null && fc.getViewRoot() != null) {
                fc.getViewRoot().getAttributes().remove(name);
            }
        } catch (RuntimeException ignored) {
        }
    }

    /**
     * Si el documento cargado para revision tiene NIT/Nombre/financesEntity
     * vacios, los recompleta a partir del proveedor de la OC. Garantiza que
     * el formulario tenga datos validos para superar las validaciones JSF
     * de aprobacion.
     */
    private void recoverFinancesEntityFromProvider() {
        if (instance == null
                || warehousePurchaseOrderAction.getInstance() == null
                || warehousePurchaseOrderAction.getInstance().getProvider() == null) {
            return;
        }
        FinancesEntity entity = warehousePurchaseOrderAction.getInstance()
                .getProvider().getEntity();
        if (entity == null) {
            return;
        }
        if (instance.getFinancesEntity() == null) {
            instance.setFinancesEntity(entity);
        }
        if (ValidatorUtil.isBlankOrNull(instance.getNit())) {
            instance.setNit(entity.getNitNumber());
        }
        if (ValidatorUtil.isBlankOrNull(instance.getName())) {
            instance.setName(entity.getAcronym());
        }
    }

    /** Igual que PurchaseDocumentAction.updateAdjustmentValues() para mantener paridad. */
    private void applyAdjustmentDefaults(PurchaseDocument doc) {
        if (doc.isAdjustmentDocument()) {
            doc.setNetAmount(doc.getAmount());
            if (ValidatorUtil.isBlankOrNull(doc.getNumber())) {
                doc.setNumber(MessageUtils.getMessage("PurchaseDocument.adjustmentNumber"));
            }
        }
    }

    private void addPurchaseDocumentErrorMessages(List<PurchaseDocumentException.ErrorType> errorTypes) {
        for (PurchaseDocumentException.ErrorType errorType : errorTypes) {
            if (PurchaseDocumentException.ErrorType.ICE_NEGATIVE_VALUE.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.iceNegative");
            } else if (PurchaseDocumentException.ErrorType.ICE_GREATER_THAN_AMOUNT.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.iceGreaterThanAmount", instance.getAmount());
            } else if (PurchaseDocumentException.ErrorType.EXEMPT_NEGATIVE_VALUE.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.exemptNegative");
            } else if (PurchaseDocumentException.ErrorType.EXEMPT_GREATER_THAN_AMOUNT.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.exemptGreaterThanAmount", instance.getAmount());
            } else if (PurchaseDocumentException.ErrorType.SUM_ICE_EXEMPT_EXCEED_AMOUNT.equals(errorType)) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "PurchaseDocument.error.sumIceExemptExceedAmount", instance.getAmount());
            }
        }
    }

    private void addPurchaseDocumentAmountErrorMessage(BigDecimal limit) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseDocument.error.amountExceedPurchaseOrderAmount", limit);
    }

    private void addPurchaseDocumentStateErrorMessage(PurchaseDocumentState state) {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                "PurchaseDocument.error.stateIsUnEditablePurchase",
                MessageUtils.getMessage(state.getResourceKey()));
    }
}
