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

    /* Buffer del upload entrante - se redimensiona y se mueve a la entidad al guardar. */
    private byte[] uploadedMapBytes;
    private String uploadedMapName;
    private String uploadedMapContentType;

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
     * Si hay bytes nuevos en el buffer, validar tamano, redimensionar y mover
     * a la entidad. Si no hay upload nuevo, se conserva la imagen actual.
     * Retorna false con mensaje al usuario si la imagen no es procesable.
     */
    private boolean validateAndProcessUpload() {
        if (uploadedMapBytes == null || uploadedMapBytes.length == 0) {
            return true;
        }
        if (uploadedMapBytes.length > MAP_MAX_INPUT_BYTES) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchRoute.error.imageTooLarge");
            return false;
        }
        try {
            byte[] resized = ImageUtils.resizeAndCompress(uploadedMapBytes, MAP_MAX_WIDTH_PX);
            DispatchRoute r = getInstance();
            r.setMapImage(resized);
            r.setMapImageContentType("image/jpeg");
        } catch (IOException e) {
            log.error("Procesando imagen de ruta", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "DispatchRoute.error.imageInvalid");
            return false;
        } finally {
            uploadedMapBytes = null;
            uploadedMapName = null;
            uploadedMapContentType = null;
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

    /* =============== Buffer del upload =============== */

    public byte[] getUploadedMapBytes() {
        return uploadedMapBytes;
    }

    public void setUploadedMapBytes(byte[] uploadedMapBytes) {
        this.uploadedMapBytes = uploadedMapBytes;
    }

    public String getUploadedMapName() {
        return uploadedMapName;
    }

    public void setUploadedMapName(String uploadedMapName) {
        this.uploadedMapName = uploadedMapName;
    }

    public String getUploadedMapContentType() {
        return uploadedMapContentType;
    }

    public void setUploadedMapContentType(String uploadedMapContentType) {
        this.uploadedMapContentType = uploadedMapContentType;
    }
}
