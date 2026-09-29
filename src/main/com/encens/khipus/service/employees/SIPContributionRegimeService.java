package com.encens.khipus.service.employees;

import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.SIPContributionRegime;
import com.encens.khipus.model.finances.Contract;

import javax.ejb.Local;
import java.util.List;

/**
 * @author
 * @version 6.0.129
 */
@Local
public interface SIPContributionRegimeService extends GenericService {

    /**
     * @return el regimen marcado por defecto, o null si no hay ninguno configurado
     */
    SIPContributionRegime findDefault();

    List<SIPContributionRegime> findActiveList();

    /**
     * Resuelve el regimen que corresponde aplicar a un contrato: el suyo si lo tiene,
     * el regimen por defecto si no. Puede devolver null cuando el catalogo esta vacio,
     * en cuyo caso el calculo cobra todos los aportes (comportamiento historico).
     */
    SIPContributionRegime resolveRegime(Contract contract, SIPContributionRegime defaultRegime);

    Long countByName(String name);

    Long countByNameButThis(String name, Long id);

    Long countContractsByRegime(SIPContributionRegime regime);
}
