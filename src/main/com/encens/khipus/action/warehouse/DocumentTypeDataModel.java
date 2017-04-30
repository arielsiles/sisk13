package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.DocumentTypePK;
import com.encens.khipus.model.warehouse.WarehouseDocumentType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @version 2.0
 */
@Name("warehouseDocumentTypeDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('WAREHOUSEDOCUMENTTYPE','VIEW')}")
public class DocumentTypeDataModel extends QueryDataModel<DocumentTypePK, WarehouseDocumentType> {

    private static final String[] RESTRICTIONS =
            {"lower(documentType.name) like concat('%', concat(lower(#{warehouseDocumentTypeDataModel.criteria.name}), '%'))"};

    @Create
    public void init() {
        sortProperty = "documentType.name";
    }

    @Override
    public String getEjbql() {
        return "select documentType from WarehouseDocumentType documentType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }
}
