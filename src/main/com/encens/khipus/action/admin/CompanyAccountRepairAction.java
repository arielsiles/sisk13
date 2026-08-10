package com.encens.khipus.action.admin;

import com.encens.khipus.model.finances.DanglingAccount;
import com.encens.khipus.service.finances.AccountIntegrityService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Guardian de integridad de cuentas para Preferencias de compania.
 * <p>
 * Preferencias de compania se rompe al renderizar si una columna de
 * `configuracion` o `ck_ctas_bco` apunta a una cuenta que no existe en `arcgms`
 * (el proxy @ManyToOne LAZY/EAGER a CashAccount explota con
 * EntityNotFoundException). Este action se interpone: detecta las cuentas
 * colgadas con SQL nativo -- sin cargar CashAccount, asi que nunca se rompe -- y,
 * si las hay, desvia a una pantalla de reparacion en vez de dejar entrar a
 * Preferencias. Alli el usuario las anula (NULL) y recien entonces puede entrar.
 *
 * @author
 * @version 1.0
 */
@Name("companyAccountRepairAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('COMPANYSETTING','VIEW')}")
public class CompanyAccountRepairAction {

    @Logger
    private Log log;

    @In
    private AccountIntegrityService accountIntegrityService;

    @In
    private FacesMessages facesMessages;

    private List<DanglingAccount> danglingAccounts = new ArrayList<DanglingAccount>();

    /**
     * Guardian: page-action de /admin/companySetting.xhtml (on-postback="false").
     * Si hay cuentas colgadas devuelve "repair" y pages.xml desvia a la pantalla de
     * reparacion, evitando que Preferencias intente renderizar y explote. Si esta
     * todo integro devuelve null y Preferencias renderiza normal.
     * <p>
     * Se hace como page-action y NO como action de un s:link porque s:link navega
     * por su atributo view (GET), no por las reglas from-action: un s:link con
     * action pero sin view se queda en la pagina actual.
     */
    public String checkBeforeSettings() {
        return accountIntegrityService.hasDangling() ? "repair" : null;
    }

    /**
     * page-action de /admin/companyAccountRepair.xhtml (on-postback="false"): carga
     * la lista al entrar. En los postbacks (anular) no corre; la lista persiste en
     * la conversacion y la recarga nullifySelected().
     */
    public void load() {
        danglingAccounts = accountIntegrityService.findDanglingAccounts();
    }

    /**
     * Anula (NULL) las cuentas colgadas seleccionadas y recarga la lista.
     */
    @Restrict("#{s:hasPermission('COMPANYSETTING','UPDATE')}")
    public void nullifySelected() {
        int done = 0;
        int failed = 0;
        int selected = 0;
        for (DanglingAccount item : danglingAccounts) {
            if (item.isSelected()) {
                selected++;
                if (accountIntegrityService.nullify(item)) {
                    done++;
                } else {
                    failed++;
                }
            }
        }
        log.info("nullifySelected: lista=#0 seleccionadas=#1 anuladas=#2 fallidas=#3",
                danglingAccounts.size(), selected, done, failed);

        if (done == 0 && failed == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "AccountIntegrity.nothingSelected");
        } else {
            if (done > 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                        "AccountIntegrity.nullified", done);
            }
            if (failed > 0) {
                // p.ej. columna NOT NULL en la BD: no se pudo anular.
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                        "AccountIntegrity.nullifyFailed", failed);
            }
        }

        danglingAccounts = accountIntegrityService.findDanglingAccounts();
    }

    /**
     * Continua a Preferencias de compania. Solo tiene sentido cuando ya no quedan
     * cuentas colgadas. La conversacion la cierra pages.xml (<end-conversation/>).
     */
    public String goToSettings() {
        return "settings";
    }

    public List<DanglingAccount> getDanglingAccounts() {
        return danglingAccounts;
    }

    public boolean isClean() {
        return danglingAccounts != null && danglingAccounts.isEmpty();
    }

    public int getDanglingCount() {
        return danglingAccounts == null ? 0 : danglingAccounts.size();
    }
}
