package com.encens.khipus.action.warehouse;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
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
 * La imagen del mapa se redimensiona+recomprime (JPEG ~85% calidad, max
 * 1024px) al guardar para acotar el tamano en BD. Patron: una imagen por
 * ruta, NUNCA replicada al despacho.
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

    /*
     * El archivo del mapa se sube DIRECTO a la entidad (data="#{dispatchRoute.mapImage}").
     * Este campo solo actua como senal: s:fileUpload setea el fileName unicamente
     * cuando el usuario adjunta un archivo nuevo, asi sabemos cuando redimensionar
     * sin re-comprimir la imagen ya existente en cada guardado.
     */
    private String uploadedMapName;

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
        return super.create();
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
        return super.update();
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
     * Si el usuario adjunto un archivo nuevo (uploadedMapName presente), validar
     * tamano y redimensionar los bytes ya cargados en la entidad, reemplazandolos
     * por la version JPEG redimensionada. Si no hubo upload nuevo, se conserva la
     * imagen actual sin re-comprimir. Retorna false con mensaje si no es procesable.
     */
    private boolean validateAndProcessUpload() {
        if (uploadedMapName == null || uploadedMapName.trim().isEmpty()) {
            return true; // no se adjunto archivo nuevo en este guardado
        }
        DispatchRoute r = getInstance();
        byte[] raw = r.getMapImage();
        if (raw == null || raw.length == 0) {
            uploadedMapName = null;
            return true;
        }
        if (raw.length > MAP_MAX_INPUT_BYTES) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchRoute.error.imageTooLarge");
            return false;
        }
        try {
            byte[] resized = ImageUtils.resizeAndCompress(raw, MAP_MAX_WIDTH_PX);
            r.setMapImage(resized);
            r.setMapImageContentType("image/jpeg");
        } catch (IOException e) {
            log.error("Procesando imagen de ruta", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchRoute.error.imageInvalid");
            return false;
        } finally {
            uploadedMapName = null;
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
     * Se sirve inline (no via el resource servlet de Seam) para que se muestre de
     * forma confiable durante la edicion. Retorna null si no hay imagen.
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

    /* =============== Senal de upload nuevo =============== */

    public String getUploadedMapName() {
        return uploadedMapName;
    }

    public void setUploadedMapName(String uploadedMapName) {
        this.uploadedMapName = uploadedMapName;
    }
}
