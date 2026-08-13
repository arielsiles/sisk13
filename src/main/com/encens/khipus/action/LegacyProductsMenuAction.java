package com.encens.khipus.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Puerta del tab legacy "Procesos Productivos" (acopio, produccion, productos y sus
 * reportes). Sus opciones comparten los codigos de permiso con otros menus, por eso el
 * tab necesita un permiso propio para poder apagarse en un cliente sin afectar al resto.
 *
 * @author Encens
 * @see AbstractLegacyMenu
 */
@Name("legacyProductsMenu")
@Scope(ScopeType.SESSION)
@AutoCreate
@BypassInterceptors
public class LegacyProductsMenuAction extends AbstractLegacyMenu {

    public static final String LEGACY_PRODUCTS_MENU_PERMISSION = "LEGACYPRODUCTIONPROCESSES";

    @Override
    protected String getPermissionCode() {
        return LEGACY_PRODUCTS_MENU_PERMISSION;
    }
}
