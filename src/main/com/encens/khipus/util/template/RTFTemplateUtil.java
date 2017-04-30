package com.encens.khipus.util.template;

import com.lowagie.text.rtf.document.RtfDocument;
import net.sourceforge.rtf.IRTFDocumentTransformer;
import net.sourceforge.rtf.RTFTemplate;
import net.sourceforge.rtf.document.RTFDocument;
import net.sourceforge.rtf.document.RTFElement;
import net.sourceforge.rtf.handler.RTFDocumentHandler;
import net.sourceforge.rtf.template.velocity.RTFVelocityTransformerImpl;
import net.sourceforge.rtf.template.velocity.VelocityTemplateEngineImpl;
import org.apache.velocity.app.VelocityEngine;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * Util to merge rtf document templates
 *
 * @author
 * @version $Id: RTFTemplateUtil.java  15-mar-2010 19:33:38$
 */
public class RTFTemplateUtil {
    private static Log log = Logging.getLog(RTFTemplateUtil.class);
    private RTFTemplate rtfTemplate;

    private static String RTF_PAGEBREAK = "\\page";
    private static String RTF_STARTGROUP = "{";
    private static String RTF_ENDGROUP = "}";

    public RTFTemplateUtil(byte[] rtfDocument) {
        initializeRtfTemplateConfigutation();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rtfDocument);
        rtfTemplate.setTemplate(byteArrayInputStream);
    }

    /**
     * initialize configuration with velocity implementation
     */
    private void initializeRtfTemplateConfigutation() {
        rtfTemplate = new RTFTemplate();
        // Parser
        RTFDocumentHandler parser = new RTFDocumentHandler();
        rtfTemplate.setParser(parser);

        // Transformer
        IRTFDocumentTransformer transformer = new RTFVelocityTransformerImpl();
        rtfTemplate.setTransformer(transformer);

        // Template engine
        VelocityTemplateEngineImpl velocityTemplateEngine = new VelocityTemplateEngineImpl();
        velocityTemplateEngine.setVelocityEngine(new VelocityEngine());
        rtfTemplate.setTemplateEngine(velocityTemplateEngine);
    }

    /**
     * add pair "variable" and "value" to merge the template
     *
     * @param variableValueMap variable value
     */
    public void addVariableValues(Map<String, Object> variableValueMap) {
        for (String variableKey : variableValueMap.keySet()) {
            Object valueObject = variableValueMap.get(variableKey);

            if (valueObject instanceof List) {
                addVariableValues((List<Map>) valueObject, variableKey);
            } else {

                if (valueObject instanceof String) {
                    valueObject = getEncodedRTFString(valueObject.toString());
                }
                rtfTemplate.put(variableKey, valueObject);
            }
        }
    }

    /**
     * add values as list in this variable name
     *
     * @param variableValueMapList list
     * @param listVariableName     variable list name
     */
    public void addVariableValues(List<Map> variableValueMapList, String listVariableName) {
        //encode String objects to rtf encode
        for (Map variableValueMap : variableValueMapList) {
            for (Object variableKey : variableValueMap.keySet()) {
                Object valueObject = variableValueMap.get(variableKey);
                if (valueObject instanceof String) {
                    variableValueMap.put(variableKey, getEncodedRTFString(valueObject.toString()));
                }
            }
        }

        rtfTemplate.put(listVariableName, variableValueMapList);
    }

    /**
     * Merge the template with the variable values defined
     *
     * @return byte[]
     * @throws Exception
     */
    public byte[] mergeTemplate() throws Exception {
        log.debug("Executing merge document.....");
        CharArrayWriter ch = new CharArrayWriter();

        rtfTemplate.merge(ch);
        byte[] mergeDocument = ch.toString().getBytes();
        ch.close();
        return mergeDocument;
    }

    public void mergeTemplate(File rtfFile) throws Exception {
        log.debug("Executing merge document in file.....");
        rtfTemplate.merge(rtfFile);
    }

    /**
     * iText util to encode special characters like ñ,ó,í as rtf string
     *
     * @param sentence sentence
     * @return String
     */
    public String getEncodedRTFString(String sentence) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            new RtfDocument().filterSpecialChar(baos, sentence, true, true);
        } catch (IOException e) {
            // will never happen for ByteArrayOutputStream
            log.debug("Error in encode special characters to rtf..", e);
        }
        return new String(baos.toByteArray());
    }

    /**
     * Join list of RTF documents in only one document
     *
     * @param rtfDocumentList list of RTF documents as byte[]
     * @return JoinDocument
     * @throws IOException
     */
    public static byte[] joinRtfDocuments(List<byte[]> rtfDocumentList) throws IOException {
        byte[] mergeDocument = null;
        RTFDocument joinRtfDocument = new RTFDocument();

        if (rtfDocumentList.size() > 0) {
            for (int i = 0; i < rtfDocumentList.size(); i++) {
                byte[] document = rtfDocumentList.get(i);
                boolean isFirtsDocument = (i == 0);
                boolean isLastDocument = (i == rtfDocumentList.size() - 1);

                ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(document);

                //parse the document
                RTFDocumentHandler rtfDocumentHandler = new RTFDocumentHandler();
                rtfDocumentHandler.parse(arrayInputStream);
                arrayInputStream.close();

                RTFDocument rtfDocument = rtfDocumentHandler.getRTFDocument();
                List elementList = rtfDocument.getElementList();

                for (int j = 0; j < elementList.size(); j++) {
                    Object o = elementList.get(j);
                    if (o instanceof RTFElement) {
                        joinRtfDocument.addRTFElement((RTFElement) o);
                    } else {
                        //fix rtf characters
                        String elementString = o.toString();

                        if (j == 0 && !isFirtsDocument) {
                            elementString = removeRtfHeader(elementString);
                        }
                        if (j == (elementList.size() - 1) && !isLastDocument) {
                            elementString = removeRtfFooter(elementString);
                        }
                        joinRtfDocument.addRTFString(elementString);
                    }
                }

                //add page break in join document
                if (!isLastDocument) {
                    joinRtfDocument.addRTFString(RTF_PAGEBREAK);
                }
            }

            //get the joined document
            CharArrayWriter charArrayWriter = new CharArrayWriter();
            joinRtfDocument.save(charArrayWriter);
            charArrayWriter.close();

            mergeDocument = charArrayWriter.toString().getBytes();
        }
        return mergeDocument;
    }

    /**
     * Join all rtf documents in this directory
     *
     * @param directoryFile directory
     * @param fileWriter    file writer to the join document
     * @throws IOException
     */
    public static void joinAllRtfDocumentsInDirentory(File directoryFile, FileWriter fileWriter) throws IOException {
        log.debug("Executing join rtf documents..." + directoryFile);

        RTFDocument joinRtfDocument = new RTFDocument();

        if (directoryFile.exists() && directoryFile.isDirectory()) {
            //find all rtf documents names in this directory
            String[] children = directoryFile.list(new RTFFileNameFilter());

            for (int i = 0; i < children.length; i++) {
                File childFile = new File(directoryFile, children[i]);
                boolean isFirtsDocument = (i == 0);
                boolean isLastDocument = (i == children.length - 1);

                FileInputStream fileInputStream = new FileInputStream(childFile);

                //parse the document
                RTFDocumentHandler rtfDocumentHandler = new RTFDocumentHandler();
                rtfDocumentHandler.parse(fileInputStream);
                fileInputStream.close();

                RTFDocument rtfDocument = rtfDocumentHandler.getRTFDocument();
                List elementList = rtfDocument.getElementList();

                for (int j = 0; j < elementList.size(); j++) {
                    Object o = elementList.get(j);
                    if (o instanceof RTFElement) {
                        joinRtfDocument.addRTFElement((RTFElement) o);
                    } else {
                        //fix rtf characters
                        String elementString = o.toString();

                        if (j == 0 && !isFirtsDocument) {
                            elementString = removeRtfHeader(elementString);
                        }
                        if (j == (elementList.size() - 1) && !isLastDocument) {
                            elementString = removeRtfFooter(elementString);
                        }
                        joinRtfDocument.addRTFString(elementString);
                    }
                }

                //add page break in join document
                if (!isLastDocument) {
                    joinRtfDocument.addRTFString(RTF_PAGEBREAK);
                }
            }

            //save el joiend document
            joinRtfDocument.save(fileWriter);
        }
    }

    private static boolean isRtfDocument(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".rtf");
    }

    /**
     * Remove the start group braces "{"
     *
     * @param rtfHeaderElement rtf element
     * @return String
     */
    private static String removeRtfHeader(String rtfHeaderElement) {
        String newHeader = rtfHeaderElement;
        if (rtfHeaderElement != null) {
            int firstBraceIndex = rtfHeaderElement.indexOf(RTF_STARTGROUP);
            if (firstBraceIndex != -1) {
                newHeader = rtfHeaderElement.substring(firstBraceIndex + 1);
            }
        }
        return newHeader;
    }

    /**
     * Remove the end group braces "}"
     *
     * @param rtfFooterElement rtf element
     * @return String
     */
    private static String removeRtfFooter(String rtfFooterElement) {
        String newFooter = rtfFooterElement;
        if (rtfFooterElement != null) {
            int lastBraceIndex = rtfFooterElement.lastIndexOf(RTF_ENDGROUP);
            if (lastBraceIndex != -1) {
                newFooter = rtfFooterElement.substring(0, lastBraceIndex);
            }
        }
        return newFooter;
    }

    public static void main(String[] args) {
        //exmaple to merge rtf templates
        RTFTemplate rtfTemplate = new RTFTemplate();
        // Parser
        RTFDocumentHandler parser = new RTFDocumentHandler();
        rtfTemplate.setParser(parser);

        // Transformer
        IRTFDocumentTransformer transformer = new RTFVelocityTransformerImpl();
        rtfTemplate.setTransformer(transformer);

        // Template engine
//        ITemplateEngine templateEngine = new VelocityTemplateEngineImpl();
        VelocityTemplateEngineImpl velocityTemplateEngine = new VelocityTemplateEngineImpl();
        velocityTemplateEngine.setVelocityEngine(new VelocityEngine());
//        rtfTemplate.setTemplateEngine(templateEngine);
        rtfTemplate.setTemplateEngine(velocityTemplateEngine);

        System.out.println("Start RTFTemplateWithSpringConfig...");
        try {
            File fileA = new File("D:/ariel/ariel KHIPUS/KHIPUS/src/main/com/encens/khipus/util/template/jakartavelocitymodel.rtf");

            // 3. Set the RTF model source
            rtfTemplate.setTemplate(fileA);

            // 4. Put the context
            rtfTemplate.put("header_developer_name", "Name acción");
            rtfTemplate.put("header_developer_email", "Email");
            rtfTemplate.put("header_developer_roles22", "Roles");

            // 5. Merge the RTF sourec model and the context
            String rtfTarget = "spring_config.rtf";
            rtfTemplate.merge(rtfTarget);

            System.out.println("End RTFTemplateWithSpringConfig...");
        } catch (Exception e) {
            System.out.println("Error while using RTFTemplate with RTFTemplateWithSpringConfig : " + e + "--"
                    + e.getMessage());
        }
    }
}
