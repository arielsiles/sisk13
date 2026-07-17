package com.encens.khipus.service.admin;

import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.common.File;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author
 * @version 1.0
 */
@Name("companyLogoService")
@Stateless
@AutoCreate
public class CompanyLogoServiceBean extends GenericServiceBean implements CompanyLogoService {

    public File findHeaderLogo() {
        return findLogo("select logo from CompanyConfiguration c join c.headerLogo logo");
    }

    public File findLoginLogo() {
        return findLogo("select logo from CompanyConfiguration c join c.loginLogo logo");
    }

    /**
     * Devuelve el primer resultado en lugar de getSingleResult() a proposito: el
     * login corre sin currentCompany, y en ese caso Seam no habilita companyFilter
     * (components.xml lo declara enabled="#{not empty currentCompany}"), asi que
     * la consulta sale sin filtrar por compania. Con getSingleResult() una segunda
     * compania haria explotar la pantalla de login.
     */
    @SuppressWarnings("unchecked")
    private File findLogo(String query) {
        List<File> logos = getEntityManager().createQuery(query).setMaxResults(1).getResultList();
        if (logos.isEmpty()) {
            return null;
        }
        File logo = logos.get(0);
        // value es @Lob @Basic(LAZY): forzar la carga dentro de la sesion, porque
        // el holder lo consume despues de cerrada.
        if (logo.getValue() == null) {
            return null;
        }
        return logo;
    }
}
