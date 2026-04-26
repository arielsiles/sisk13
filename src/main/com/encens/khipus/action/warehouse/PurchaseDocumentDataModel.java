package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.purchases.PurchaseDocument;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SerializableDataModel;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.25
 */
@Name("warehousePurchaseDocumentDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('PURCHASEDOCUMENT','VIEW')}")
public class PurchaseDocumentDataModel extends QueryDataModel<Long, PurchaseDocument> {
    private static final String[] RESTRICTIONS =
            {"purchaseDocument.purchaseOrder = #{warehousePurchaseOrder}"};

    @Create
    public void init() {
        sortProperty = "purchaseDocument.date";
    }

    @Override
    public String getEjbql() {
        return "select purchaseDocument from PurchaseDocument purchaseDocument";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    /**
     * No serializar el data model entre requests. La grilla muestra
     * documentos cuyo estado puede cambiar (PENDING -> APPROVED/NULLIFIED)
     * desde el modal de la misma pagina; si el wrappedData se reusara
     * entre requests, los cambios de estado no se reflejarian hasta cambiar
     * de pestania. Forzando getSerializableModel a devolver null, cada
     * render arranca con detached=false y walk() ejecuta un nuevo SELECT.
     * El costo es despreciable (la grilla por OC tiene pocos rows).
     */
    @Override
    public SerializableDataModel getSerializableModel(Range range) {
        return null;
    }
}
