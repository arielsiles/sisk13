package com.encens.khipus.action.customers;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.customers.AccountType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Arrays;
import java.util.List;

/**
 * Listado del ABM de tipos de cuenta.
 *
 * @author
 */
@Name("accountTypeDataModel")
@Scope(ScopeType.PAGE)
public class AccountTypeDataModel extends QueryDataModel<Long, AccountType> {

    /**
     * El filtro de activos va como campo propio y no sobre criteria: la entidad nace
     * con active = TRUE, asi que usar criteria dejaria el listado filtrado a activos
     * sin que el usuario lo haya pedido y sin manera de ver los inactivos.
     * <p/>
     * Se guarda como String y no como Boolean porque el converter estandar de JSF
     * convierte la opcion vacia del combo a FALSE, no a null, y "Todos" dejaria de
     * traer los activos.
     */
    private String activeFilter;

    private static final String[] RESTRICTIONS = {
            "lower(accountType.name) like concat('%', concat(lower(#{accountTypeDataModel.criteria.name}), '%'))",
            "accountType.savingType = #{accountTypeDataModel.criteria.savingType}",
            "accountType.active = #{accountTypeDataModel.active}"
    };

    @Create
    public void init() {
        sortProperty = "accountType.name";
    }

    @Override
    public String getEjbql() {
        return "select accountType from AccountType accountType";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    /** Null = todos, y entonces Seam descarta la restriccion. */
    public Boolean getActive() {
        if ("1".equals(activeFilter)) {
            return Boolean.TRUE;
        }
        if ("0".equals(activeFilter)) {
            return Boolean.FALSE;
        }
        return null;
    }

    public String getActiveFilter() {
        return activeFilter;
    }

    public void setActiveFilter(String activeFilter) {
        this.activeFilter = activeFilter;
    }
}
