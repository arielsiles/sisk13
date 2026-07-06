package com.encens.khipus.action.sales;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.sales.CommercialCostCenter;
import com.encens.khipus.model.sales.Incoterm;
import com.encens.khipus.model.sales.SalesOrder;
import com.encens.khipus.model.sales.SalesOrderDetail;
import com.encens.khipus.model.sales.SalesOrderNote;
import com.encens.khipus.model.warehouse.ProductItem;
import com.encens.khipus.service.sales.SalesOrderService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

/**
 * Accion CRUD y flujo de la Orden de Venta.
 *
 * @author
 * @version 1.0
 */
@Name("salesOrderAction")
@Scope(ScopeType.CONVERSATION)
public class SalesOrderAction extends GenericAction<SalesOrder> {

    @In
    private SalesOrderService salesOrderService;

    @In(value = "#{entityManager}")
    private EntityManager entityManager;

    @Factory(value = "salesOrder", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('SALESORDER','VIEW')}")
    public SalesOrder initSalesOrder() {
        return getInstance();
    }

    @Override
    public String getDisplayNameProperty() {
        return "orderNumber";
    }

    /* ============================ CRUD ============================ */

    /**
     * Crea la orden y se QUEDA en modo edicion (guardar-y-seguir). Retorna SUCCESS
     * + setOp(UPDATE); en pages.xml la regla de create redirige a la MISMA pantalla
     * (limite de transaccion limpio: evita StaleObjectState/Lock timeout). La
     * conversacion sigue viva para seguir modificando.
     */
    @Override
    @Restrict("#{s:hasPermission('SALESORDER','CREATE')}")
    public String create() {
        try {
            salesOrderService.createSalesOrder(getInstance());
            addCreatedMessage();
            setOp(OP_UPDATE);
            return Outcome.SUCCESS;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.FAIL;
        }
    }

    /** Actualiza y se QUEDA en la misma pagina (REDISPLAY, sin cerrar conversacion). */
    @Override
    @Restrict("#{s:hasPermission('SALESORDER','UPDATE')}")
    public String update() {
        if (!getInstance().isDraft()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "SalesOrder.notEditable");
            return Outcome.REDISPLAY;
        }
        try {
            salesOrderService.updateSalesOrder(getInstance());
            addUpdatedMessage();
            return Outcome.REDISPLAY;
        } catch (EntryDuplicatedException e) {
            addDuplicatedMessage();
            return Outcome.REDISPLAY;
        }
    }

    /* ==================== datos del comprador ==================== */

    /** Copia el snapshot del comprador al seleccionar/cambiar el cliente. */
    public void updateBuyerData() {
        getInstance().copyBuyerDataFromClient();
    }

    /** Asigna el cliente (desde el modal) y copia el snapshot del comprador. */
    public void assignClient(com.encens.khipus.model.customers.Client client) {
        getInstance().setClient(client);
        updateBuyerData();
    }

    /** Limpia el cliente seleccionado. */
    public void clearClient() {
        getInstance().setClient(null);
    }

    /* ========================= lineas ========================= */

    public void addProductItems(List<ProductItem> productItems) {
        if (productItems == null) {
            return;
        }
        for (ProductItem productItem : productItems) {
            SalesOrderDetail detail = new SalesOrderDetail();
            detail.setSalesOrder(getInstance());
            detail.setProductItem(productItem);
            detail.setDescription(productItem.getName());
            detail.setMeasureUnit(resolveDefaultMeasureCode(productItem));
            detail.setQuantity(BigDecimal.ONE);
            detail.setUnitPrice(productItem.getSalePrice() != null ? productItem.getSalePrice() : BigDecimal.ZERO);
            getInstance().getDetailList().add(detail);
        }
        recalculate();
    }

    public void removeDetail(SalesOrderDetail detail) {
        getInstance().getDetailList().remove(detail);
        recalculate();
    }

    /** Recalcula totales de linea y de la orden (para reRender en la grilla). */
    public void recalculate() {
        salesOrderService.computeTotals(getInstance());
    }

    /** Unidad por defecto = Tonelada (busca en el catalogo por nombre/codigo); si no
     *  existe, cae a la unidad del articulo. */
    @SuppressWarnings("unchecked")
    private String resolveDefaultMeasureCode(ProductItem productItem) {
        java.util.List<com.encens.khipus.model.finances.MeasureUnit> units =
                entityManager.createQuery("select m from MeasureUnit m where m.companyNumber = :cia")
                        .setParameter("cia", com.encens.khipus.util.Constants.defaultCompanyNumber)
                        .getResultList();
        for (com.encens.khipus.model.finances.MeasureUnit m : units) {
            String name = m.getName() != null ? m.getName().toUpperCase() : "";
            String code = m.getId().getMeasureUnitCode();
            if (name.contains("TONELADA")
                    || "TM".equalsIgnoreCase(code) || "TON".equalsIgnoreCase(code) || "TN".equalsIgnoreCase(code)) {
                return code;
            }
        }
        return productItem != null ? productItem.getUsageMeasureCode() : null;
    }

    /** Unidades de medida disponibles (codigo - nombre) para el combo de linea. */
    @SuppressWarnings("unchecked")
    public java.util.List<javax.faces.model.SelectItem> getMeasureUnitSelectItems() {
        java.util.List<com.encens.khipus.model.finances.MeasureUnit> units =
                entityManager.createQuery("select m from MeasureUnit m where m.companyNumber = :cia order by m.measureUnitCode")
                        .setParameter("cia", com.encens.khipus.util.Constants.defaultCompanyNumber)
                        .getResultList();
        java.util.List<javax.faces.model.SelectItem> items = new java.util.ArrayList<javax.faces.model.SelectItem>();
        for (com.encens.khipus.model.finances.MeasureUnit m : units) {
            String code = m.getId().getMeasureUnitCode();
            items.add(new javax.faces.model.SelectItem(code, code + " - " + m.getName()));
        }
        return items;
    }

    /* -------- comentarios: lineas independientes -------- */

    private String newComment;

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    public void clearNewComment() {
        this.newComment = null;
    }

    /** Agrega el texto tecleado como una nueva linea de comentario. */
    public void addComment() {
        if (newComment != null && newComment.trim().length() > 0) {
            String c = getInstance().getComments();
            getInstance().setComments((c == null || c.trim().length() == 0)
                    ? newComment.trim() : c + "\n" + newComment.trim());
        }
        newComment = null;
    }

    /** Lineas de comentario (una por salto de linea). */
    public java.util.List<String> getCommentLines() {
        java.util.List<String> lines = new java.util.ArrayList<String>();
        String c = getInstance().getComments();
        if (c != null) {
            for (String l : c.split("\n")) {
                if (l.trim().length() > 0) {
                    lines.add(l);
                }
            }
        }
        return lines;
    }

    public void removeCommentAt(int index) {
        java.util.List<String> lines = getCommentLines();
        if (index >= 0 && index < lines.size()) {
            lines.remove(index);
            StringBuilder sb = new StringBuilder();
            for (String l : lines) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(l);
            }
            getInstance().setComments(sb.toString());
        }
    }

    /* ==================== transiciones de estado ==================== */

    @End(ifOutcome = Outcome.SUCCESS)
    @Restrict("#{s:hasPermission('SALESORDERSEND','VIEW')}")
    public String sendToReview() {
        if (!getInstance().isDraft()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "SalesOrder.notEditable");
            return Outcome.REDISPLAY;
        }
        salesOrderService.sendToReview(getInstance());
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "SalesOrder.message.sent",
                getInstance().getOrderNumber());
        return Outcome.SUCCESS;
    }

    @End(ifOutcome = Outcome.SUCCESS)
    @Restrict("#{s:hasPermission('SALESORDERAPPROVE','VIEW')}")
    public String approve() {
        if (!getInstance().isInReview()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "SalesOrder.notInReview");
            return Outcome.REDISPLAY;
        }
        salesOrderService.approve(getInstance(), getCurrentUser());
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "SalesOrder.message.approved",
                getInstance().getOrderNumber());
        return Outcome.SUCCESS;
    }

    @End(ifOutcome = Outcome.SUCCESS)
    @Restrict("#{s:hasPermission('SALESORDERAPPROVE','VIEW')}")
    public String reject() {
        if (!getInstance().isInReview()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN, "SalesOrder.notInReview");
            return Outcome.REDISPLAY;
        }
        salesOrderService.reject(getInstance(), getInstance().getManagementObservations());
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "SalesOrder.message.rejected",
                getInstance().getOrderNumber());
        return Outcome.SUCCESS;
    }

    @End(ifOutcome = Outcome.SUCCESS)
    @Restrict("#{s:hasPermission('SALESORDERNULLIFY','VIEW')}")
    public String nullify() {
        if (getInstance().isNullified()) {
            return Outcome.REDISPLAY;
        }
        salesOrderService.nullify(getInstance());
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "SalesOrder.message.nullified",
                getInstance().getOrderNumber());
        return Outcome.SUCCESS;
    }

    /* ========================= helpers de vista ========================= */

    /** Editable solo cuando es nueva o esta en BORRADOR. */
    public boolean isEditable() {
        return !isManaged() || getInstance().isDraft();
    }

    public FinancesCurrencyType[] getCurrencyTypes() {
        return new FinancesCurrencyType[]{FinancesCurrencyType.P, FinancesCurrencyType.D};
    }

    public com.encens.khipus.model.sales.SalesOrderState[] getStateList() {
        return com.encens.khipus.model.sales.SalesOrderState.values();
    }

    @SuppressWarnings("unchecked")
    public List<Incoterm> getIncotermList() {
        return entityManager.createNamedQuery("Incoterm.findActive")
                .setParameter("active", Boolean.TRUE).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<CommercialCostCenter> getCostCenterList() {
        return entityManager.createNamedQuery("CommercialCostCenter.findActive")
                .setParameter("active", Boolean.TRUE).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<SalesOrderNote> getNoteList() {
        return entityManager.createNamedQuery("SalesOrderNote.findActive")
                .setParameter("active", Boolean.TRUE).getResultList();
    }

    /* -------- notas: seleccion de a una -------- */

    private SalesOrderNote selectedNote;

    public SalesOrderNote getSelectedNote() {
        return selectedNote;
    }

    public void setSelectedNote(SalesOrderNote selectedNote) {
        this.selectedNote = selectedNote;
    }

    /** Agrega la nota elegida a la orden (si no estaba). */
    public void addSelectedNote() {
        if (selectedNote != null && !containsNote(selectedNote.getId())) {
            getInstance().getSelectedNotes().add(selectedNote);
        }
        selectedNote = null;
    }

    /** Agrega directamente una nota (desde el modal de notas predefinidas). */
    public void addNote(SalesOrderNote note) {
        if (note != null && !containsNote(note.getId())) {
            getInstance().getSelectedNotes().add(note);
        }
    }

    public void removeSelectedNote(SalesOrderNote note) {
        SalesOrderNote toRemove = null;
        for (SalesOrderNote n : getInstance().getSelectedNotes()) {
            if (n.getId().equals(note.getId())) {
                toRemove = n;
                break;
            }
        }
        if (toRemove != null) {
            getInstance().getSelectedNotes().remove(toRemove);
        }
    }

    /** Notas activas que aun no fueron agregadas a la orden. */
    public List<SalesOrderNote> getAvailableNotes() {
        java.util.List<SalesOrderNote> result = new java.util.ArrayList<SalesOrderNote>();
        for (SalesOrderNote n : getNoteList()) {
            if (!containsNote(n.getId())) {
                result.add(n);
            }
        }
        return result;
    }

    /** Terminos activos para el modal (se elige UNO; al elegir reemplaza). */
    @SuppressWarnings("unchecked")
    public java.util.List<com.encens.khipus.model.sales.SalesOrderTerm> getActiveTerms() {
        return entityManager.createNamedQuery("SalesOrderTerm.findActive")
                .setParameter("active", Boolean.TRUE).getResultList();
    }

    /** Copia (snapshot) el texto del termino elegido a la orden, reemplazando el anterior. */
    public void addHeaderTerm(com.encens.khipus.model.sales.SalesOrderTerm term) {
        if (term != null) {
            getInstance().setHeaderTerm(term.getText());
        }
    }

    public void clearHeaderTerm() {
        getInstance().setHeaderTerm(null);
    }

    private boolean containsNote(Long id) {
        for (SalesOrderNote n : getInstance().getSelectedNotes()) {
            if (n.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private String getCurrentUser() {
        return Identity.instance().isLoggedIn() ? Identity.instance().getPrincipal().getName() : "unknown";
    }
}
