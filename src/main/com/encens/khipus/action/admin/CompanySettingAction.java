package com.encens.khipus.action.admin;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.exception.finances.CompanyConfigurationNotFoundException;
import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CompanyConfiguration;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinanceUser;
import com.encens.khipus.service.fixedassets.CompanyConfigurationService;
import com.encens.khipus.util.ImageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.international.StatusMessage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Base64;

/**
 * CompanySettingAction
 *
 * @author
 * @version 2.26
 */
@Name("companySettingAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{s:hasPermission('COMPANYSETTING','VIEW')}")
public class CompanySettingAction extends GenericAction<CompanyConfiguration> {

    /**
     * Campo (asociacion CashAccount) que disparo la apertura del modal compartido
     * de cuentas. Se fija en prepareAccountSelection() al abrir el popup y lo lee
     * assignSelectedAccount() al elegir. Existe un unico modal para las ~51
     * cuentas porque cashAccountDataModel es @Scope(PAGE): instanciarlo una vez
     * por campo haria que todos compartan criterios y resultados.
     */
    private String accountTargetProperty;

    /**
     * Holders del upload. El s:fileUpload NUNCA debe escribir directo sobre
     * instance.headerLogo/loginLogo: un submit sin archivo pondria el campo en
     * null y borraria el logo guardado. Se copia al entity solo si trae bytes.
     */
    private File headerLogoHolder = new File();
    private File loginLogoHolder = new File();

    @In
    private CompanyConfigurationService companyConfigurationService;

    @In(create = true)
    private CompanyLogoHolder companyLogoHolder;

    @Factory(value = "companySetting", scope = ScopeType.STATELESS)
    @Restrict("#{s:hasPermission('COMPANYSETTING','VIEW')}")
    public CompanyConfiguration initCompanyConfiguration() {
        return getInstance();
    }

    @Create
    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    public void loadCompanySettings() {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        CompanyConfiguration companyConfiguration = null;
        try {
            companyConfiguration = companyConfigurationService.findCompanyConfiguration();
        } catch (CompanyConfigurationNotFoundException e) {
        }
        if (companyConfiguration != null) {
            setOp(OP_UPDATE);
            setInstance(companyConfiguration);
        }
    }

    @Restrict("#{s:hasPermission('COMPANYSETTING','UPDATE')}")
    public String saveChanges() {
        if (!applyLogos()) {
            return Outcome.REDISPLAY;
        }
        String outcome;
        if (isManaged()) {
            outcome = update();
        } else {
            outcome = create();
            if (Outcome.SUCCESS.equals(outcome)) {
                refreshInstance();
                setOp(OP_UPDATE);
            }
        }
        if (Outcome.SUCCESS.equals(outcome) || Outcome.REDISPLAY.equals(outcome)) {
            // El logo del login se sirve cacheado en scope APPLICATION; sin esto
            // la pantalla de login seguiria mostrando el logo anterior.
            companyLogoHolder.invalidate();
        }
        return outcome;
    }

    /* =============== Logos =============== */

    /**
     * Copia los logos subidos al entity, reescalados. Devuelve false si alguna
     * imagen no es legible, para no guardar el resto del formulario a medias.
     */
    private boolean applyLogos() {
        if (!applyLogo(headerLogoHolder, true)) {
            return false;
        }
        return applyLogo(loginLogoHolder, false);
    }

    private boolean applyLogo(File holder, boolean header) {
        byte[] raw = holder.getValue();
        if (raw == null || raw.length == 0) {
            return true; // sin archivo nuevo: el logo actual queda intacto
        }
        try {
            byte[] resized = header
                    ? ImageUtils.resizeToFit(raw, CompanyConfiguration.HEADER_LOGO_MAX_WIDTH,
                    CompanyConfiguration.HEADER_LOGO_MAX_HEIGHT)
                    : ImageUtils.resizeToFit(raw, CompanyConfiguration.LOGIN_LOGO_MAX_WIDTH,
                    CompanyConfiguration.LOGIN_LOGO_MAX_HEIGHT);

            CompanyConfiguration configuration = getInstance();
            File target = header ? configuration.getHeaderLogo() : configuration.getLoginLogo();
            if (target == null) {
                target = new File();
                if (header) {
                    configuration.setHeaderLogo(target);
                } else {
                    configuration.setLoginLogo(target);
                }
            }
            target.setValue(resized);
            target.setName(holder.getName());
            target.setSize(resized.length);
            target.setContentType("image/png"); // resizeToFit siempre emite PNG

            if (header) {
                headerLogoHolder = new File();
            } else {
                loginLogoHolder = new File();
            }
        } catch (IOException e) {
            log.error("Procesando logo de compania", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "CompanyConfiguration.logo.invalidImage");
            return false;
        }
        return true;
    }

    public void removeHeaderLogo() {
        getInstance().setHeaderLogo(null);
        headerLogoHolder = new File();
    }

    public void removeLoginLogo() {
        getInstance().setLoginLogo(null);
        loginLogoHolder = new File();
    }

    public String getHeaderLogoDataUri() {
        return toDataUri(getInstance().getHeaderLogo());
    }

    public String getLoginLogoDataUri() {
        return toDataUri(getInstance().getLoginLogo());
    }

    /**
     * Imagen embebida como data-URI Base64. Se sirve inline y no por el resource
     * servlet de Seam, que no es confiable en estas pantallas de edicion.
     */
    private String toDataUri(File file) {
        if (file == null) {
            return null;
        }
        byte[] value = file.getValue();
        if (value == null || value.length == 0) {
            return null;
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = "image/png";
        }
        return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(value);
    }

    public File getHeaderLogoHolder() {
        return headerLogoHolder;
    }

    public File getLoginLogoHolder() {
        return loginLogoHolder;
    }

    /* =============== Cuentas contables (modal compartido) =============== */

    /**
     * Se invoca desde el selectAction del app:selectPopUp, antes de que el modal
     * se re-renderice y se muestre. Deja anotado a que campo hay que asignar la
     * cuenta que el usuario elija.
     */
    public void prepareAccountSelection(String property) {
        this.accountTargetProperty = property;
    }

    /**
     * Div que debe re-renderizar el boton de asignar del modal compartido. Apunta
     * solo al campo que disparo la seleccion: re-renderizar el fieldset completo
     * descartaria lo que el usuario tenga tipeado sin guardar en los otros campos.
     */
    public String getAccountTargetDivId() {
        return accountTargetProperty == null ? "" : accountTargetProperty + "Div";
    }

    public void assignSelectedAccount(CashAccount cashAccount) {
        if (accountTargetProperty == null) {
            return;
        }
        try {
            cashAccount = getService().findById(CashAccount.class, cashAccount.getId());
        } catch (EntryNotFoundException e) {
            entryNotFoundLog();
        }
        setAccount(accountTargetProperty, cashAccount);
    }

    public void clearAccount(String property) {
        setAccount(property, null);
    }

    /**
     * Asigna por reflexion sobre el setter de la asociacion (no sobre el campo
     * *Code): el setter de CompanyConfiguration sincroniza el codigo, que es la
     * unica columna escribible del par -- la asociacion es insertable/updatable
     * false por la FK compuesta con no_cia.
     */
    private void setAccount(String property, CashAccount cashAccount) {
        String setter = "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
        try {
            Method method = CompanyConfiguration.class.getMethod(setter, CashAccount.class);
            method.invoke(getInstance(), cashAccount);
        } catch (Exception e) {
            // Si esto salta es un desalineo entre el nombre pasado por la pantalla
            // y el setter de la entidad, no un error de datos del usuario.
            log.error("No se pudo asignar la cuenta al campo '" + property + "'", e);
        }
    }

    /* =============== Usuarios de finanzas =============== */

    public void assignFinanceUser(String property, FinanceUser financeUser) {
        setFinanceUser(property, financeUser);
    }

    public void clearFinanceUser(String property) {
        setFinanceUser(property, null);
    }

    private void setFinanceUser(String property, FinanceUser financeUser) {
        String setter = "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
        try {
            Method method = CompanyConfiguration.class.getMethod(setter, FinanceUser.class);
            method.invoke(getInstance(), financeUser);
        } catch (Exception e) {
            log.error("No se pudo asignar el usuario al campo '" + property + "'", e);
        }
    }

    /* =============== Centro de costo =============== */

    public void clearExchangeRateBalanceCostCenter() {
        getInstance().setExchangeRateBalanceCostCenter(null);
    }

    public void assignCostCenter(CostCenter costCenter) {
        getInstance().setExchangeRateBalanceCostCenter(costCenter);
    }
}
