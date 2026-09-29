package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.SIPContributionRegime;
import com.encens.khipus.service.employees.SIPContributionRegimeService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.List;

/**
 * Publica la lista de regimenes activos para el combo del contrato.
 * <p/>
 * Va en un componente propio, sin @Restrict, porque quien edita contratos no tiene por que
 * tener permiso sobre el catalogo. No se usa una framework:entity-query para evitar un
 * literal booleano en HQL contra IntegerBooleanUserType: el named query pasa el valor como
 * parametro, que es el patron que ya usan AFPRate, CNSRate, IVARate y SMNRate.
 *
 * @author
 * @version 6.0.129
 */
@Name("sipContributionRegimeListAction")
@Scope(ScopeType.EVENT)
public class SIPContributionRegimeListAction {

    @In
    private SIPContributionRegimeService sipContributionRegimeService;

    @Factory(value = "sipContributionRegimeList", scope = ScopeType.PAGE)
    public List<SIPContributionRegime> getSipContributionRegimeList() {
        return sipContributionRegimeService.findActiveList();
    }
}
