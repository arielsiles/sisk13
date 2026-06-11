package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.warehouse.CatalogApprovalState;
import com.encens.khipus.model.warehouse.ProductDescription;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * DataModel paginado del catalogo de descripciones tecnicas de producto.
 * Filtros: codigo de articulo, contenido de descripcion, estado.
 */
@Name("productDescriptionDataModel")
@Scope(ScopeType.PAGE)
public class ProductDescriptionDataModel extends QueryDataModel<Long, ProductDescription> {

    private CatalogApprovalState state;
    private String productItemCodeFilter;
    private String descriptionFilter;

    private static final String[] RESTRICTIONS = {
            "lower(productDescription.productItemCode) like concat('%', concat(lower(#{productDescriptionDataModel.productItemCodeFilter}), '%'))",
            "lower(productDescription.description) like concat('%', concat(lower(#{productDescriptionDataModel.descriptionFilter}), '%'))",
            "productDescription.state = #{productDescriptionDataModel.state}"
    };

    @Create
    public void init() {
        sortProperty = "productDescription.id";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select productDescription from ProductDescription productDescription";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public CatalogApprovalState getState() {
        return state;
    }

    public void setState(CatalogApprovalState state) {
        this.state = state;
    }

    public String getProductItemCodeFilter() {
        return productItemCodeFilter;
    }

    public void setProductItemCodeFilter(String productItemCodeFilter) {
        this.productItemCodeFilter = productItemCodeFilter;
    }

    public String getDescriptionFilter() {
        return descriptionFilter;
    }

    public void setDescriptionFilter(String descriptionFilter) {
        this.descriptionFilter = descriptionFilter;
    }
}
