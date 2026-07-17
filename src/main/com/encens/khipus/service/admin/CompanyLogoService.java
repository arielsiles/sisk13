package com.encens.khipus.service.admin;

import com.encens.khipus.model.common.File;

import javax.ejb.Local;

/**
 * Lectura de los logos de compania para renderizarlos en la cabecera y en el
 * login.
 * <p>
 * Va aparte de CompanyConfigurationService a proposito: el login es anonimo, y
 * este servicio debe poder invocarse sin usuario ni currentCompany en contexto.
 */
@Local
public interface CompanyLogoService {

    /**
     * @return el logo de la cabecera, o null si no hay ninguno cargado.
     */
    File findHeaderLogo();

    /**
     * @return el logo del login, o null si no hay ninguno cargado.
     */
    File findLoginLogo();
}
