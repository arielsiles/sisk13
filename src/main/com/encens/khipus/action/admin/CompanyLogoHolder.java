package com.encens.khipus.action.admin;

import com.encens.khipus.model.common.File;
import com.encens.khipus.service.admin.CompanyLogoService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;

import java.util.Base64;

/**
 * Cachea los logos de compania como data-URI para la cabecera y el login.
 * <p>
 * Vive en scope APPLICATION por dos razones:
 * <ol>
 * <li>El login es <b>anonimo</b>: no hay currentCompany ni usuario, asi que no
 * puede depender de companySettingAction, que es @Scope(CONVERSATION) y tiene
 * @Restrict('COMPANYSETTING','VIEW') -- tocarlo sin sesion lanzaria
 * NotLoggedInException.</li>
 * <li>La cabecera se renderiza en <b>cada</b> pagina y para todo usuario logueado,
 * tenga o no permiso sobre la configuracion. Leer el BLOB en cada request seria
 * gasto puro: los logos cambian una vez cada varios anios.</li>
 * </ol>
 * La pantalla de edicion llama a {@link #invalidate()} al guardar; sin eso el
 * logo viejo seguiria sirviendose hasta reiniciar JBoss.
 * <p>
 * Sin logo cargado en BD los getters devuelven null y las vistas no renderizan
 * nada (no hay fallback a los .png/.jpg de /img).
 */
@Name("companyLogoHolder")
@AutoCreate
@Synchronized
@Scope(ScopeType.APPLICATION)
public class CompanyLogoHolder {

    @In(create = true)
    private CompanyLogoService companyLogoService;

    private String headerLogoDataUri;
    private String loginLogoDataUri;
    private boolean loaded;

    public String getHeaderLogoDataUri() {
        load();
        return headerLogoDataUri;
    }

    public String getLoginLogoDataUri() {
        load();
        return loginLogoDataUri;
    }

    private void load() {
        if (loaded) {
            return;
        }
        headerLogoDataUri = toDataUri(companyLogoService.findHeaderLogo());
        loginLogoDataUri = toDataUri(companyLogoService.findLoginLogo());
        loaded = true;
    }

    /**
     * Fuerza la relectura desde la BD en el proximo render. La invoca
     * CompanySettingAction tras guardar.
     */
    public void invalidate() {
        loaded = false;
        headerLogoDataUri = null;
        loginLogoDataUri = null;
    }

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
}
