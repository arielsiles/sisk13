package com.encens.khipus.action.employees;

import com.encens.khipus.model.common.File;
import com.encens.khipus.model.employees.MarkImportBatch;
import com.encens.khipus.model.employees.MarkImportBatchState;
import com.encens.khipus.service.employees.MarkImportService;
import com.encens.khipus.util.employees.MarkImportResult;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

/**
 * Importacion de marcaciones desde el archivo del biometrico.
 * <p/>
 * El flujo es en dos pasos a proposito: primero se analiza y se muestra que va a pasar
 * -cuantas marcas nuevas, cuantas repetidas, que codigos no cruzan con ningun empleado- y
 * recien despues se confirma. Nada se escribe en el analisis.
 *
 * @author
 * @version 6.1.0
 */
@Name("markImportAction")
@Scope(ScopeType.CONVERSATION)
public class MarkImportAction {

    @Logger
    private Log log;

    @In
    private FacesMessages facesMessages;

    @In
    private MarkImportService markImportService;

    /* Se usa la entidad File solo como contenedor del archivo subido: no se persiste. */
    private File file = new File();

    private MarkImportResult result;

    @Begin(join = true)
    @Restrict("#{s:hasPermission('MARKIMPORT','CREATE')}")
    public void analyze() {
        result = null;
        if (null == file.getValue() || file.getValue().length == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MarkImport.error.fileRequired");
            return;
        }
        try {
            result = markImportService.analyze(file.getValue(), file.getName());
        } catch (Exception e) {
            log.error("No se pudo leer el archivo de marcaciones", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MarkImport.error.unreadableFile");
            return;
        }
        if (0 == result.getReadRows()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MarkImport.error.noRows");
        }
    }

    @Restrict("#{s:hasPermission('MARKIMPORT','CREATE')}")
    public void confirm() {
        if (null == result || !result.hasSomethingToImport()) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MarkImport.error.nothingToImport");
            return;
        }
        try {
            MarkImportBatch batch = markImportService.confirm(result);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "MarkImport.info.imported", batch.getNewMarks(), batch.getFileName());
            file = new File();
            result = null;
            /* Se cierra la conversacion recien aqui: si la confirmacion falla, la vista previa
               debe seguir en pantalla para poder reintentar. */
            Conversation.instance().end();
        } catch (Exception e) {
            log.error("No se pudo confirmar la importacion de marcaciones", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MarkImport.error.confirmFailed");
        }
    }

    @Restrict("#{s:hasPermission('MARKIMPORT','DELETE')}")
    public void annul(MarkImportBatch batch) {
        if (MarkImportBatchState.ANNULLED.equals(batch.getState())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MarkImport.error.alreadyAnnulled");
            return;
        }
        try {
            Long marks = markImportService.countMarksByBatch(batch);
            markImportService.annul(batch);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                    "MarkImport.info.annulled", marks, batch.getFileName());
        } catch (Exception e) {
            log.error("No se pudo anular el lote de marcaciones", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR,
                    "MarkImport.error.annulFailed");
        }
    }

    @End
    public void clear() {
        file = new File();
        result = null;
    }

    public boolean isAnalyzed() {
        return null != result;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public MarkImportResult getResult() {
        return result;
    }
}
