package com.encens.khipus.action.finances;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.finances.ExchangeKind;
import com.encens.khipus.model.finances.FinancesExchangeRate;
import com.encens.khipus.model.finances.FinancesExchangeRatePk;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Listado de tipos de cambio de contabilidad (tabla <code>arcgtc</code>).
 * <p/>
 * No usar el <code>criteria</code> heredado para filtrar: {@link FinancesExchangeRatePk}
 * inicializa <code>date</code> con <code>new Date()</code>, con lo que la instancia de
 * criterio saldria siempre con una fecha puesta y el listado arrancaria filtrado.
 * Por eso los filtros son campos propios de este DataModel.
 *
 * @author
 */
@Name("financesExchangeRateDataModel")
@Scope(ScopeType.PAGE)
public class FinancesExchangeRateDataModel extends QueryDataModel<FinancesExchangeRatePk, FinancesExchangeRate> {

    private String exchangeKindCode;
    private Date startDate;
    private Date endDate;

    private static final String[] RESTRICTIONS =
            {"financesExchangeRate.id.exchangeKind = #{financesExchangeRateDataModel.exchangeKindCode}",
             "financesExchangeRate.id.date >= #{financesExchangeRateDataModel.startDate}",
             "financesExchangeRate.id.date <= #{financesExchangeRateDataModel.endDate}"};

    @Create
    public void init() {
        sortProperty = "financesExchangeRate.id.date";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select financesExchangeRate from FinancesExchangeRate financesExchangeRate";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    /**
     * El <code>search()</code> base no limpia el cache de <code>rowCount</code>, con lo que
     * el datascroller queda mostrando el conteo del filtro anterior.
     */
    @Override
    public void search() {
        super.search();
        update();
    }

    @Override
    public void clear() {
        setExchangeKindCode(null);
        setStartDate(null);
        setEndDate(null);
        super.clear();
    }

    /**
     * Clases de cambio para el combo del filtro. El valor del item es el codigo
     * (<code>clase_cambio</code>), que es lo que compara la restriccion.
     */
    @SuppressWarnings("unchecked")
    public List<SelectItem> getExchangeKindSelectItems() {
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        List<ExchangeKind> exchangeKindList =
                getEntityManager().createQuery("select o from ExchangeKind o order by o.id asc").getResultList();
        for (ExchangeKind exchangeKind : exchangeKindList) {
            selectItems.add(new SelectItem(exchangeKind.getId(),
                    exchangeKind.getId() + " - " + exchangeKind.getDescription()));
        }
        return selectItems;
    }

    public String getExchangeKindCode() {
        return exchangeKindCode;
    }

    public void setExchangeKindCode(String exchangeKindCode) {
        this.exchangeKindCode = exchangeKindCode;
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
}
