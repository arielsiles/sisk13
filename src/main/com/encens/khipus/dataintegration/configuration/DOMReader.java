package com.encens.khipus.dataintegration.configuration;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Read, validate and parse a XML file according to <code>dataintegration-1.0.xsd</code> schema.
 *
 * @author
 */
public class DOMReader {
    private static final String SCHEMA_NAME = "dataintegration-1.0.xsd";

    private Document document;
    private Element documentElement;

    public void validate(InputStream xmlFileInputStream) throws SAXException {
        validateXmlFile(xmlFileInputStream);
    }

    public void parse(InputStream xmlFileInputStream) throws SAXException {
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);

        try {
            parser.parse(new InputSource(xmlFileInputStream));
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse xml file. ", e);
        }

        document = parser.getDocument();
        documentElement = document.getDocumentElement();
    }

    public List<Element> getChildElements(Element parentElement, String childName) {
        List<Element> result = new ArrayList<Element>();
        NodeList nodes = parentElement.getElementsByTagName(childName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            result.add((Element) node);
        }
        return result;
    }

    private void validateXmlFile(InputStream xmlFileInputStream) throws SAXException {
        Schema schema = getSchema();
        Validator validator = schema.newValidator();

        try {

            validator.validate(new StreamSource(xmlFileInputStream));

        } catch (IOException e) {
            throw new RuntimeException("Cannot validate xml file. ", e);
        }
    }


    private Schema getSchema() {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        URL schemaPath = getResource(SCHEMA_NAME, this.getClass());

        Schema schema = null;
        try {
            schema = factory.newSchema(schemaPath);
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return schema;
    }

    public Document getDocument() {
        return document;
    }

    public static URL getResource(String resourceName, Class c) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (null == url) {
            url = c.getResource(resourceName);
        }

        return url;
    }

    public Element getDocumentElement() {
        return documentElement;
    }
}
