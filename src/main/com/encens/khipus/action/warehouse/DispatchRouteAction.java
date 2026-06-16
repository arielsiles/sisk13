package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.warehouse.CatalogApprovalState;
import com.encens.khipus.model.warehouse.DispatchRoute;
import com.encens.khipus.util.ImageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Base64;

/**
 * CRUD del catalogo de Rutas de Despacho.
 *
 * Ciclo de estados: BORRADOR -> APROBADO -> INACTIVO (sin vuelta atras).
 * APROBADO/INACTIVO son inmutables.
 *
 * Flujo de pantalla (igual que el Vale de Despacho):
 *  - Guardar (create) -> persiste y redirige a la MISMA pantalla en modo edicion
 *    (pages.xml propaga la conversacion, op=UPDATE preservado).
 *  - Guardar (update) -> persiste y se queda (Outcome.REDISPLAY).
 *  - Aprobar / Inactivar / Borrar / Cancelar -> vuelven al listado.
 *
 * Imagen del mapa: el upload escribe en el holder {@link #mapFile} (patron de la
 * foto de Activos Fijos), NUNCA en la entidad; la imagen de la entidad solo se
 * reemplaza cuando hay un archivo nuevo, por lo que guardar sin re-subir jamas la
 * pone en null. Se redimensiona+recomprime a JPEG (max 1024px) al guardar.
 */
@Name("dispatchRouteAction")
@Scope(ScopeType.CONVERSATION)
public class DispatchRouteAction extends GenericAction<DispatchRoute> {

    /** Ancho maximo en pixeles aplicado al guardar (mapas tipicos ~1024 ok). */
    private static final int MAP_MAX_WIDTH_PX = 1024;

    /** Limite duro del archivo de entrada antes de redimensionar (5 MB). */
    private static final int MAP_MAX_INPUT_BYTES = 5 * 1024 * 1024;

    @In(value = "#{entityManager}")
    private EntityManager em;

    /**
     * Holder del upload. El s:fileUpload escribe aqui (no en la entidad). Su
     * value queda en null mientras no se adjunte un archivo nuevo, lo que sirve
     * de senal para saber cuando reemplazar la imagen de la ruta.
     */
    private File mapFile = new File();

    @Factory(value = "dispatchRoute", scope = ScopeType.STATELESS)
    public DispatchRoute initDispatchRoute() {
        return getInstance();
    }

    @Override
    public String create() {
        DispatchRoute r = getInstance();
        if (r.getState() == null) {
            r.setState(CatalogApprovalState.BORRADOR);
        }
        if (!validateAndProcessUpload()) {
            return Outcome.REDISPLAY;
        }
        if (!validateUniqueness(null)) {
            return Outcome.REDISPLAY;
        }
        String outcome = super.create();
        if (Outcome.SUCCESS.equals(outcome)) {
            // Quedar en modo edicion tras el redirect-to-self (ver pages.xml):
            // la conversacion se propaga y op=UPDATE muestra botones e imagen.
            setOp(OP_UPDATE);
        }
        return outcome;
    }

    @Override
    public String update() {
        DispatchRoute r = getInstance();
        if (!r.isEditable()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "DispatchRoute.error.notEditable");
            return Outcome.REDISPLAY;
        }
        if (!validateAndProcessUpload()) {
            return Outcome.REDISPLAY;
        }
        if (!validateUniqueness(r.getId())) {
            return Outcome.REDISPLAY;
        }
        String outcome = super.update();
        // Guardar y permanecer en la pantalla (igual que el Vale de Despacho).
        return Outcome.SUCCESS.equals(outcome) ? Outcome.REDISPLAY : outcome;
    }

    @Override
    public String delete() {
        if (!getInstance().isEditable()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "DispatchRoute.error.notDeletable");
            return Outcome.REDISPLAY;
        }
        return super.delete();
    }

    /** Transicion BORRADOR -> APROBADO. */
    public String approve() {
        DispatchRoute r = getInstance();
        if (r.getState() != CatalogApprovalState.BORRADOR) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "DispatchRoute.error.cannotApprove");
            return Outcome.REDISPLAY;
        }
        if (!validateAndProcessUpload()) {
            return Outcome.REDISPLAY;
        }
        if (!validateUniqueness(r.getId())) {
            return Outcome.REDISPLAY;
        }
        r.setState(CatalogApprovalState.APROBADO);
        return super.update();
    }

    /** Transicion APROBADO -> INACTIVO. */
    public String inactivate() {
        DispatchRoute r = getInstance();
        if (r.getState() != CatalogApprovalState.APROBADO) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "DispatchRoute.error.cannotInactivate");
            return Outcome.REDISPLAY;
        }
        r.setState(CatalogApprovalState.INACTIVO);
        return super.update();
    }

    /**
     * Si se adjunto un archivo nuevo (mapFile.value presente), validar tamano,
     * redimensionar y reemplazar la imagen de la entidad. Si no hay archivo
     * nuevo, no se toca la imagen actual (se conserva). Retorna false con mensaje
     * si la imagen no es procesable.
     */
    private boolean validateAndProcessUpload() {
        byte[] raw = mapFile.getValue();
        if (raw == null || raw.length == 0) {
            return true; // sin archivo nuevo: la imagen actual queda intacta
        }
        if (raw.length > MAP_MAX_INPUT_BYTES) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchRoute.error.imageTooLarge");
            return false;
        }
        try {
            byte[] resized = ImageUtils.resizeAndCompress(raw, MAP_MAX_WIDTH_PX);
            DispatchRoute r = getInstance();
            r.setMapImage(resized);
            r.setMapImageContentType("image/jpeg");
            mapFile = new File(); // limpiar el buffer
        } catch (IOException e) {
            log.error("Procesando imagen de ruta", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchRoute.error.imageInvalid");
            return false;
        }
        return true;
    }

    private boolean validateUniqueness(Long currentId) {
        DispatchRoute r = getInstance();
        if (r.getName() == null || r.getName().trim().isEmpty()) {
            return true; // @NotNull lo cubre
        }
        Long count;
        if (currentId == null) {
            count = (Long) em.createNamedQuery("DispatchRoute.countByName")
                    .setParameter("name", r.getName().trim())
                    .getSingleResult();
        } else {
            count = (Long) em.createNamedQuery("DispatchRoute.countByNameAndDifferent")
                    .setParameter("name", r.getName().trim())
                    .setParameter("id", currentId)
                    .getSingleResult();
        }
        if (count != null && count > 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchRoute.error.duplicated", r.getName());
            return false;
        }
        return true;
    }

    @Override
    protected String getDisplayNameProperty() {
        return "name";
    }

    /**
     * Imagen del mapa embebida como data-URI Base64 para el preview en pantalla.
     * Se sirve inline (no via el resource servlet de Seam) para mostrarla de forma
     * confiable. Retorna null si no hay imagen.
     */
    public String getMapImageDataUri() {
        DispatchRoute r = getInstance();
        byte[] img = r.getMapImage();
        if (img == null || img.length == 0) {
            return null;
        }
        String contentType = r.getMapImageContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = "image/jpeg";
        }
        return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(img);
    }

    /* =============== Holder del upload =============== */

    public File getMapFile() {
        return mapFile;
    }

    public void setMapFile(File mapFile) {
        this.mapFile = mapFile;
    }
}
