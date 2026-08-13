package com.encens.khipus.action;

import com.encens.khipus.util.Constants;
import org.jboss.seam.security.Identity;

import java.io.Serializable;

/**
 * Base de los tabs legacy del menu principal.
 * <p/>
 * Un tab legacy agrupa opciones de varios modulos que se iran migrando a sus tabs
 * definitivos. Mientras dure la migracion cada tab se gobierna con un unico permiso:
 * quien lo tiene ve el tab con TODAS sus opciones habilitadas; quien no lo tiene no ve
 * el tab. Esto permite prender o apagar el tab por cliente aunque sus opciones compartan
 * los codigos de permiso con otros menus.
 * <p/>
 * Cada opcion conserva dentro del menu el codigo de su permiso real via {@link #can(String)}:
 * ese es el mapa de migracion. Al mover una opcion a su tab definitivo se le devuelve su
 * <code>s:hasPermission(...)</code> original.
 *
 * @author Encens
 */
public abstract class AbstractLegacyMenu implements Serializable {

    /**
     * @return codigo de la funcionalidad que abre el tab legacy completo
     */
    protected abstract String getPermissionCode();

    /**
     * @return true si el usuario tiene el permiso del tab legacy
     */
    public boolean isEnabled() {
        return Identity.instance().hasPermission(getPermissionCode(), Constants.VIEW_PERMISSION);
    }

    /**
     * @param functionCode codigo de la funcionalidad propia de la opcion
     * @return true si el tab legacy esta habilitado o si el usuario tiene el permiso propio
     */
    public boolean can(String functionCode) {
        return can(functionCode, Constants.VIEW_PERMISSION);
    }

    public boolean can(String functionCode, String action) {
        return isEnabled() || Identity.instance().hasPermission(functionCode, action);
    }
}
