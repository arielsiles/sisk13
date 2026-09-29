package com.encens.khipus.service.employees;

import com.encens.khipus.exception.ConcurrencyException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.employees.MarkImportBatch;
import com.encens.khipus.model.employees.MarkImportBatchState;
import com.encens.khipus.model.employees.RH_Mark;
import com.encens.khipus.util.XlsxReader;
import com.encens.khipus.util.employees.MarkImportResult;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Importacion de marcaciones desde el archivo que exporta el biometrico.
 *
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("markImportService")
@AutoCreate
public class MarkImportServiceBean extends GenericServiceBean implements MarkImportService {

    /* Encabezados del export. Se buscan por nombre y no por posicion, para que un cambio de
       orden de columnas en el dispositivo no rompa la carga. */
    private static final String HEADER_EMPLOYEE_ID = "id del empleado";
    private static final String HEADER_DATE = "fecha";
    private static final String HEADER_TIME = "hora";
    private static final String HEADER_MARK_TYPE = "tipo de marcaci";

    /* `control` en rh_marcado: la base ya usa esta codificacion. */
    private static final int CONTROL_ENTRY = 1;
    private static final int CONTROL_EXIT = 3;

    private static final String IMPORT_SOURCE = "IMPORT";

    @In(required = false)
    private User currentUser;

    public MarkImportResult analyze(byte[] content, String fileName) throws Exception {
        MarkImportResult result = new MarkImportResult();
        result.setFileName(fileName);

        Set<String> markCodes = findEmployeeMarkCodes();
        /* Duplicados dentro del propio archivo: el dispositivo emite la misma marca dos veces. */
        Set<String> seenInFile = new HashSet<String>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setLenient(false);

        for (XlsxReader.Sheet sheet : XlsxReader.read(content)) {
            int[] columns = findColumns(sheet);
            if (null == columns) {
                continue; /* hoja sin encabezado reconocible: no es de marcaciones */
            }
            int headerRow = columns[4];

            for (int i = headerRow + 1; i < sheet.getRows().size(); i++) {
                String[] row = sheet.getRows().get(i);
                String code = sheet.cell(row, columns[0]).trim();
                String dateText = sheet.cell(row, columns[1]).trim();
                String timeText = sheet.cell(row, columns[2]).trim();
                String typeText = sheet.cell(row, columns[3]).trim();

                if (code.length() == 0 && dateText.length() == 0 && timeText.length() == 0) {
                    continue; /* fila en blanco */
                }
                result.addReadRow();

                Date markDate;
                Date markTime;
                Integer markPerId;
                try {
                    markDate = dateFormat.parse(dateText);
                    markTime = timeFormat.parse(timeText);
                    markPerId = Integer.valueOf(code);
                } catch (ParseException e) {
                    result.addUnreadableRow(describeRow(sheet.getName(), i, code, dateText, timeText));
                    continue;
                } catch (NumberFormatException e) {
                    result.addUnreadableRow(describeRow(sheet.getName(), i, code, dateText, timeText));
                    continue;
                }

                /* Se importa igual aunque el codigo no cruce con ningun empleado: la marca es
                   un hecho del dispositivo y no debe perderse. Queda registrada y se vincula
                   sola en cuanto se corrija el codigo de marcacion del empleado. Solo se
                   reporta, para que RRHH vea que codigos hay que asignar. */
                if (!markCodes.contains(code)) {
                    result.addWithoutEmployee(code);
                }

                /* Tampoco se descarta una marca repetida. El sistema no decide que marcacion
                   sobra: tiene que reflejar el archivo tal cual para que la carga sea auditable,
                   y que el export traiga repetidos es responsabilidad de quien lo exporta. Se
                   cuentan nada mas para avisar antes de confirmar. */
                String key = code + "|" + dateText + "|" + timeText;
                if (!seenInFile.add(key) || existsMark(markPerId, markDate, markTime)) {
                    result.addDuplicated();
                }

                RH_Mark mark = new RH_Mark();
                mark.setMarPerId(markPerId);
                mark.setMarDate(markDate);
                mark.setMarTime(markTime);
                mark.setControl(controlOf(typeText));
                mark.setMarIpPc(IMPORT_SOURCE);
                result.addNewMark(mark);
            }
        }
        return result;
    }

    public MarkImportBatch confirm(MarkImportResult result) throws Exception {
        MarkImportBatch batch = new MarkImportBatch();
        batch.setFileName(result.getFileName());
        batch.setUploadDate(new Date());
        batch.setUser(currentUser);
        batch.setFromDate(result.getFromDate());
        batch.setToDate(result.getToDate());
        batch.setReadRows(result.getReadRows());
        batch.setNewMarks(result.getNewMarkCount());
        batch.setDuplicatedMarks(result.getDuplicatedMarks());
        batch.setMarksWithoutEmployee(result.getMarksWithoutEmployee());
        batch.setCodesWithoutEmployee(result.getCodesWithoutEmployeeAsText());
        batch.setState(MarkImportBatchState.CONFIRMED);

        getEntityManager().persist(batch);
        getEntityManager().flush();

        int flushCount = 0;
        for (RH_Mark mark : result.getNewMarks()) {
            mark.setImportBatchId(batch.getId());
            getEntityManager().persist(mark);
            /* Se vacia el contexto cada tanto: un archivo puede traer miles de marcas y
               mantenerlas todas en la sesion de Hibernate degrada la carga. */
            if (++flushCount % 200 == 0) {
                getEntityManager().flush();
                getEntityManager().clear();
            }
        }
        getEntityManager().flush();
        return batch;
    }

    public void annul(MarkImportBatch batch) throws ConcurrencyException {
        getEntityManager().createQuery("delete from RH_Mark m where m.importBatchId = :importBatchId")
                .setParameter("importBatchId", batch.getId())
                .executeUpdate();
        batch.setState(MarkImportBatchState.ANNULLED);
        getEntityManager().merge(batch);
        getEntityManager().flush();
    }

    public Long countMarksByBatch(MarkImportBatch batch) {
        return (Long) getEntityManager().createNamedQuery("MarkImportBatch.countMarksByBatch")
                .setParameter("importBatchId", batch.getId())
                .getSingleResult();
    }

    /**
     * Ubica las columnas por el texto del encabezado.
     *
     * @return {codigo, fecha, hora, tipo, fila del encabezado}, o null si la hoja no tiene
     *         un encabezado de marcaciones
     */
    private int[] findColumns(XlsxReader.Sheet sheet) {
        for (int i = 0; i < sheet.getRows().size() && i < 20; i++) {
            String[] row = sheet.getRows().get(i);
            int codeColumn = -1, dateColumn = -1, timeColumn = -1, typeColumn = -1;
            for (int c = 0; c < row.length; c++) {
                String header = normalize(row[c]);
                if (header.startsWith(HEADER_EMPLOYEE_ID)) {
                    codeColumn = c;
                } else if (header.equals(HEADER_DATE)) {
                    dateColumn = c;
                } else if (header.equals(HEADER_TIME)) {
                    timeColumn = c;
                } else if (header.startsWith(HEADER_MARK_TYPE)) {
                    typeColumn = c;
                }
            }
            if (codeColumn >= 0 && dateColumn >= 0 && timeColumn >= 0) {
                return new int[]{codeColumn, dateColumn, timeColumn, typeColumn, i};
            }
        }
        return null;
    }

    private String normalize(String value) {
        return null == value ? "" : value.trim().toLowerCase();
    }

    private Integer controlOf(String typeText) {
        String type = normalize(typeText);
        if (type.startsWith("salida")) {
            return CONTROL_EXIT;
        }
        if (type.startsWith("entrada")) {
            return CONTROL_ENTRY;
        }
        return null;
    }

    private String describeRow(String sheetName, int rowIndex, String code, String date, String time) {
        return sheetName + " fila " + (rowIndex + 1) + ": " + code + " / " + date + " / " + time;
    }

    @SuppressWarnings({"unchecked"})
    private Set<String> findEmployeeMarkCodes() {
        List<String> codes = getEntityManager()
                .createQuery("select e.markCode from Employee e where e.markCode is not null")
                .getResultList();
        Set<String> result = new HashSet<String>();
        for (String code : codes) {
            if (null != code) {
                result.add(code.trim());
            }
        }
        return result;
    }

    private boolean existsMark(Integer markPerId, Date markDate, Date markTime) {
        Long count = (Long) getEntityManager().createQuery(
                "select count(m) from RH_Mark m where m.marPerId = :marPerId"
                        + " and m.marDate = :marDate and m.marTime = :marTime")
                .setParameter("marPerId", markPerId)
                .setParameter("marDate", markDate)
                .setParameter("marTime", markTime)
                .getSingleResult();
        return null != count && count > 0;
    }
}
