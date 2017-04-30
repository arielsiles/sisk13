package com.encens.khipus.action.production;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.Provide;
import com.encens.khipus.model.production.GestionTax;
import com.encens.khipus.model.warehouse.ProductItem;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 2.2
 */

@Name("gestionTaxSearchDataModel")
@Scope(ScopeType.PAGE)
public class GestionTaxSearchDataModel extends QueryDataModel<Long, GestionTax> {

    private Date startDate;
    private Date endDate;

    private static final String[] RESTRICTIONS =
            {
                    "gestionTax.startDate = #{gestionTaxSearchDataModel.startDate} ",
                    "gestionTax.endDate = #{gestionTaxSearchDataModel.endDate} "
            };

    @Create
    public void init() {
        sortProperty = "gestionTax.endDate";
    }

    @Override
    public String getEjbql() {
        return "select gestionTax from GestionTax gestionTax";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<GestionTax> getSelectedGestionTaxs() {
        List ids = super.getSelectedIdList();

        List<GestionTax> result = new ArrayList<GestionTax>();
        for (Object id : ids) {
            result.add(getEntityManager().find(GestionTax.class, id));
        }

        return result;
    }
}
