package com.encens.khipus.util;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Lector minimo de archivos .xlsx, sin dependencias externas.
 * <p/>
 * Un .xlsx es un ZIP con XML adentro: `xl/sharedStrings.xml` guarda los textos y
 * `xl/worksheets/sheetN.xml` las filas, que referencian esos textos por indice. Se lee con
 * java.util.zip y SAX, ambos del JDK.
 * <p/>
 * Se hizo asi a proposito en vez de agregar poi-ooxml: eso traia xmlbeans y dom4j al EAR, y
 * xmlbeans es fuente conocida de conflictos de classloader en JBoss 5.1. Un choque ahi
 * afectaria a todas las empresas, no solo al modulo de RRHH.
 * <p/>
 * Alcance deliberadamente acotado: devuelve el valor de cada celda como texto, sin formatos,
 * formulas ni estilos. Alcanza para los archivos planos de exportacion; no pretende ser un
 * lector general de Excel.
 *
 * @author
 * @version 6.1.0
 */
public class XlsxReader {

    private XlsxReader() {
    }

    /**
     * Una hoja del libro: su nombre y sus filas, cada fila con las celdas indexadas por
     * posicion de columna (0 = A, 1 = B, ...). Las columnas vacias vienen como cadena vacia,
     * nunca null, y todas las filas de una hoja tienen el mismo largo.
     */
    public static class Sheet {
        private String name;
        private List<String[]> rows = new ArrayList<String[]>();

        public String getName() {
            return name;
        }

        public List<String[]> getRows() {
            return rows;
        }

        /**
         * @return el valor de la celda, o cadena vacia si la fila no llega a esa columna
         */
        public String cell(String[] row, int columnIndex) {
            return (columnIndex >= 0 && columnIndex < row.length && null != row[columnIndex])
                    ? row[columnIndex] : "";
        }
    }

    /**
     * @param content contenido del archivo .xlsx
     * @return las hojas en el orden en que aparecen en el archivo
     */
    public static List<Sheet> read(byte[] content) throws IOException, SAXException {
        Map<String, byte[]> entries = unzip(content);

        List<String> sharedStrings = readSharedStrings(entries.get("xl/sharedStrings.xml"));
        Map<String, String> sheetNamesByFile = readSheetNames(entries.get("xl/workbook.xml"),
                entries.get("xl/_rels/workbook.xml.rels"));

        List<String> sheetFiles = new ArrayList<String>();
        for (String entryName : entries.keySet()) {
            if (entryName.startsWith("xl/worksheets/sheet") && entryName.endsWith(".xml")) {
                sheetFiles.add(entryName);
            }
        }
        java.util.Collections.sort(sheetFiles, new SheetFileComparator());

        List<Sheet> result = new ArrayList<Sheet>();
        for (String sheetFile : sheetFiles) {
            Sheet sheet = readSheet(entries.get(sheetFile), sharedStrings);
            sheet.name = sheetNamesByFile.containsKey(sheetFile)
                    ? sheetNamesByFile.get(sheetFile) : sheetFile;
            result.add(sheet);
        }
        return result;
    }

    /** sheet2.xml antes que sheet10.xml: el orden natural, no el alfabetico. */
    private static class SheetFileComparator implements java.util.Comparator<String> {
        public int compare(String a, String b) {
            return numberOf(a) - numberOf(b);
        }

        private int numberOf(String fileName) {
            StringBuilder digits = new StringBuilder();
            for (int i = 0; i < fileName.length(); i++) {
                char c = fileName.charAt(i);
                if (c >= '0' && c <= '9') {
                    digits.append(c);
                }
            }
            return digits.length() == 0 ? 0 : Integer.parseInt(digits.toString());
        }
    }

    private static Map<String, byte[]> unzip(byte[] content) throws IOException {
        Map<String, byte[]> entries = new HashMap<String, byte[]>();
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content));
        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = zip.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
                entries.put(entry.getName(), out.toByteArray());
                zip.closeEntry();
            }
        } finally {
            zip.close();
        }
        return entries;
    }

    private static SAXParser newParser() throws SAXException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            return factory.newSAXParser();
        } catch (Exception e) {
            throw new SAXException("No se pudo crear el parser XML", e);
        }
    }

    private static void parse(byte[] xml, DefaultHandler handler) throws IOException, SAXException {
        if (null == xml) {
            return;
        }
        InputStream in = new ByteArrayInputStream(xml);
        try {
            newParser().parse(new InputSource(in), handler);
        } finally {
            in.close();
        }
    }

    /** `xl/sharedStrings.xml`: la tabla de textos que las celdas referencian por indice. */
    private static List<String> readSharedStrings(byte[] xml) throws IOException, SAXException {
        final List<String> strings = new ArrayList<String>();
        parse(xml, new DefaultHandler() {
            private StringBuilder current = null;
            private StringBuilder item = null;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if ("si".equals(qName)) {
                    item = new StringBuilder();
                } else if ("t".equals(qName) && null != item) {
                    current = new StringBuilder();
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                if (null != current) {
                    current.append(ch, start, length);
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if ("t".equals(qName) && null != current) {
                    /* Un <si> puede traer varios <t> cuando el texto tiene formato mezclado. */
                    item.append(current);
                    current = null;
                } else if ("si".equals(qName) && null != item) {
                    strings.add(item.toString());
                    item = null;
                }
            }
        });
        return strings;
    }

    /** Relaciona cada `xl/worksheets/sheetN.xml` con el nombre visible de la hoja. */
    private static Map<String, String> readSheetNames(byte[] workbookXml, byte[] relsXml)
            throws IOException, SAXException {
        final Map<String, String> nameByRelationId = new HashMap<String, String>();
        parse(workbookXml, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if ("sheet".equals(qName)) {
                    String relationId = attributes.getValue("r:id");
                    if (null != relationId) {
                        nameByRelationId.put(relationId, attributes.getValue("name"));
                    }
                }
            }
        });

        final Map<String, String> result = new HashMap<String, String>();
        parse(relsXml, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if ("Relationship".equals(qName)) {
                    String id = attributes.getValue("Id");
                    String target = attributes.getValue("Target");
                    if (null != id && null != target && nameByRelationId.containsKey(id)) {
                        if (!target.startsWith("xl/")) {
                            target = "xl/" + (target.startsWith("/") ? target.substring(1) : target);
                        }
                        result.put(target, nameByRelationId.get(id));
                    }
                }
            }
        });
        return result;
    }

    private static Sheet readSheet(byte[] xml, final List<String> sharedStrings)
            throws IOException, SAXException {
        final Sheet sheet = new Sheet();
        parse(xml, new DefaultHandler() {
            private Map<Integer, String> currentRow = null;
            private int maxColumn = -1;
            private int cellColumn = -1;
            private String cellType = null;
            private StringBuilder value = null;
            private boolean insideValue = false;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                if ("row".equals(qName)) {
                    currentRow = new HashMap<Integer, String>();
                    maxColumn = -1;
                } else if ("c".equals(qName)) {
                    cellColumn = columnIndexOf(attributes.getValue("r"));
                    cellType = attributes.getValue("t");
                    value = new StringBuilder();
                } else if (("v".equals(qName) || "t".equals(qName)) && null != value) {
                    insideValue = true;
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                if (insideValue && null != value) {
                    value.append(ch, start, length);
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                if ("v".equals(qName) || "t".equals(qName)) {
                    insideValue = false;
                } else if ("c".equals(qName)) {
                    if (null != currentRow && cellColumn >= 0) {
                        String raw = value.toString();
                        String resolved = raw;
                        if ("s".equals(cellType)) {
                            /* referencia a la tabla de textos compartidos */
                            try {
                                int index = Integer.parseInt(raw.trim());
                                resolved = (index >= 0 && index < sharedStrings.size())
                                        ? sharedStrings.get(index) : "";
                            } catch (NumberFormatException ignored) {
                                resolved = "";
                            }
                        }
                        if (resolved.length() > 0) {
                            currentRow.put(cellColumn, resolved);
                            if (cellColumn > maxColumn) {
                                maxColumn = cellColumn;
                            }
                        }
                    }
                    value = null;
                    cellColumn = -1;
                    cellType = null;
                } else if ("row".equals(qName) && null != currentRow) {
                    String[] row = new String[maxColumn + 1];
                    for (int i = 0; i <= maxColumn; i++) {
                        row[i] = currentRow.containsKey(i) ? currentRow.get(i) : "";
                    }
                    sheet.rows.add(row);
                    currentRow = null;
                }
            }
        });
        return sheet;
    }

    /**
     * "AB12" -> 27. Convierte la referencia de celda en indice de columna base cero, para no
     * depender de la posicion: un exportador puede omitir las celdas vacias.
     */
    private static int columnIndexOf(String cellReference) {
        if (null == cellReference) {
            return -1;
        }
        int index = 0;
        boolean any = false;
        for (int i = 0; i < cellReference.length(); i++) {
            char c = Character.toUpperCase(cellReference.charAt(i));
            if (c < 'A' || c > 'Z') {
                break;
            }
            index = index * 26 + (c - 'A' + 1);
            any = true;
        }
        return any ? index - 1 : -1;
    }
}
