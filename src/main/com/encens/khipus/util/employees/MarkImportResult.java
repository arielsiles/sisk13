package com.encens.khipus.util.employees;

import com.encens.khipus.model.employees.RH_Mark;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resultado de analizar un archivo de marcaciones, antes de confirmarlo.
 * <p/>
 * El analisis no escribe nada: clasifica lo leido para que RRHH vea que va a pasar y recien
 * despues confirme. Las marcas nuevas quedan armadas y listas para persistir.
 *
 * @author
 * @version 6.1.0
 */
public class MarkImportResult {

    private String fileName;
    private int readRows = 0;

    /** Todas las marcas legibles del archivo: no se descarta ninguna. */
    private List<RH_Mark> newMarks = new ArrayList<RH_Mark>();

    /** Cuantas vienen repetidas -del propio archivo o ya cargadas antes-. Se cargan igual:
     *  es solo el aviso previo a confirmar. */
    private int duplicatedMarks = 0;

    /** Filas cuyo codigo de marcacion no corresponde a ningun empleado. */
    private int marksWithoutEmployee = 0;

    /** Los codigos que no cruzaron, sin repetir, para poder reportarlos. */
    private Set<String> codesWithoutEmployee = new LinkedHashSet<String>();

    /** Filas que no se pudieron leer: fecha u hora con formato invalido. */
    private List<String> unreadableRows = new ArrayList<String>();

    private Date fromDate;
    private Date toDate;

    public boolean hasSomethingToImport() {
        return !newMarks.isEmpty();
    }

    public boolean hasWarnings() {
        return marksWithoutEmployee > 0 || !unreadableRows.isEmpty();
    }

    public void addNewMark(RH_Mark mark) {
        newMarks.add(mark);
        if (null == fromDate || mark.getMarDate().before(fromDate)) {
            fromDate = mark.getMarDate();
        }
        if (null == toDate || mark.getMarDate().after(toDate)) {
            toDate = mark.getMarDate();
        }
    }

    public void addDuplicated() {
        duplicatedMarks++;
    }

    public void addWithoutEmployee(String code) {
        marksWithoutEmployee++;
        if (codesWithoutEmployee.size() < 100) {
            codesWithoutEmployee.add(code);
        }
    }

    public void addUnreadableRow(String detail) {
        if (unreadableRows.size() < 50) {
            unreadableRows.add(detail);
        }
    }

    public String getCodesWithoutEmployeeAsText() {
        StringBuilder result = new StringBuilder();
        for (String code : codesWithoutEmployee) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(code);
        }
        return result.length() > 2000 ? result.substring(0, 2000) : result.toString();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getReadRows() {
        return readRows;
    }

    public void setReadRows(int readRows) {
        this.readRows = readRows;
    }

    public void addReadRow() {
        this.readRows++;
    }

    public List<RH_Mark> getNewMarks() {
        return newMarks;
    }

    public int getNewMarkCount() {
        return newMarks.size();
    }

    public int getDuplicatedMarks() {
        return duplicatedMarks;
    }

    public int getMarksWithoutEmployee() {
        return marksWithoutEmployee;
    }

    public Set<String> getCodesWithoutEmployee() {
        return codesWithoutEmployee;
    }

    public List<String> getUnreadableRows() {
        return unreadableRows;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }
}
