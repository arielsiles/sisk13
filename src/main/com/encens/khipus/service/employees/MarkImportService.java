package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.MarkImportBatch;
import com.encens.khipus.util.employees.MarkImportResult;

import javax.ejb.Local;

/**
 * @author
 * @version 6.1.0
 */
@Local
public interface MarkImportService extends GenericService {

    /**
     * Lee y clasifica el archivo sin escribir nada, para que RRHH vea que va a pasar antes de
     * confirmar.
     *
     * @param content  contenido del .xlsx exportado por el biometrico
     * @param fileName nombre del archivo, solo para dejarlo registrado en el lote
     */
    MarkImportResult analyze(byte[] content, String fileName) throws Exception;

    /**
     * Persiste las marcas nuevas del analisis y deja el lote registrado.
     *
     * @return el lote creado
     */
    MarkImportBatch confirm(MarkImportResult result) throws Exception;

    /**
     * Borra las marcas que trajo el lote y lo deja anulado. No toca el marcado que el
     * dispositivo escribio directo, porque esas marcas no tienen lote.
     */
    void annul(MarkImportBatch batch) throws ConcurrencyException;

    Long countMarksByBatch(MarkImportBatch batch);
}
