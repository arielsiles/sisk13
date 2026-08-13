package com.encens.khipus.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Puerta del tab legacy "Gestion Administrativa Financiera" (contabilidad, almacenes,
 * activos fijos, presupuestos y fondos rotatorios).
 *
 * @author Encens
 * @see AbstractLegacyMenu
 */
@Name("legacyMenu")
@Scope(ScopeType.SESSION)
@AutoCreate
@BypassInterceptors
public class LegacyMenuAction extends AbstractLegacyMenu {

    public static final String LEGACY_MENU_PERMISSION = "LEGACYFINANCIALADMINMANAGEMENT";

    @Override
    protected String getPermissionCode() {
        return LEGACY_MENU_PERMISSION;
    }
}
