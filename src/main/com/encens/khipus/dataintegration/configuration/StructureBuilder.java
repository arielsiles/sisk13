package com.encens.khipus.dataintegration.configuration;

import com.encens.khipus.dataintegration.configuration.structure.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.encens.khipus.dataintegration.configuration.XmlConstants.*;

/**
 * Build <code>XmlConfiguration</code> object according to xml parsed file. Uses <code>DOMReader</code>
 * object to read, validate and parse the xml file.
 *
 * @author
 */
public class StructureBuilder {
    private DOMReader reader = new DOMReader();

    public void validate(InputStream xmlFileInputStream) throws SAXException {
        reader.validate(xmlFileInputStream);
    }

    public XmlConfiguration buildConfiguration(InputStream xmlFileInputStream) {
        try {
            reader.parse(xmlFileInputStream);
        } catch (SAXException e) {
            throw new RuntimeException("Cannot parse xml file. ", e);
        }

        return buildConfiguration(reader.getDocumentElement(), reader);
    }

    private XmlConfiguration buildConfiguration(Element documentElement, DOMReader reader) {
        XmlConfiguration xmlConfiguration = new XmlConfiguration();

        List<IntegrationElement> result = new ArrayList<IntegrationElement>();

        List<Element> integrationNodeList = reader.getChildElements(documentElement, IntegrationTag.TAG_NAME.getConstant());

        for (Element element : integrationNodeList) {
            IntegrationElement integrationElement = buildIntegrationElement(element);
            result.add(integrationElement);
        }

        xmlConfiguration.setLocalDataSource(documentElement.getAttribute(IntegrationsTag.ATTR_LOCAL_DATA_SOURCE.getConstant()));
        xmlConfiguration.setTimerInterval(Long.valueOf(documentElement.getAttribute(IntegrationsTag.ATTR_TIMER_INTERVAL.getConstant())));

        xmlConfiguration.setIntegrationElements(result);

        return xmlConfiguration;
    }

    private IntegrationElement buildIntegrationElement(Element integrationNode) {
        IntegrationElement integrationElement = new IntegrationElement();

        integrationElement.setApplicationId(integrationNode.getAttribute(IntegrationTag.ATTR_APPLICATION_ID.getConstant()));
        integrationElement.setServiceSeamName(integrationNode.getAttribute(IntegrationTag.ATTR_SERVICE_SEAM_NAME.getConstant()));
        integrationElement.setDataSource(integrationNode.getAttribute(IntegrationTag.ATTR_DATA_SOURCE.getConstant()));

        Table sourceTable = buildSourceTable(integrationNode);

        Table targetTable = buildTargetTable(integrationNode, sourceTable);

        integrationElement.setTargetTable(targetTable);
        integrationElement.setSourceTable(sourceTable);

        return integrationElement;
    }

    private Table buildSourceTable(Element integrationNode) {
        List<Element> tableNodes = reader.getChildElements(integrationNode, SourceTableTag.TAG_NAME.getConstant());
        Element tableNode = tableNodes.get(0);
        Table table = new Table();
        table.setAlias(tableNode.getAttribute(SourceTableTag.ATTR_ALIAS.getConstant()));
        table.setDbTableName(tableNode.getAttribute(SourceTableTag.ATTR_DB_TABLE_NAME.getConstant()));
        table.setSchema(tableNode.getAttribute(SourceTableTag.ATTR_DB_SCHEMA_NAME.getConstant()));

        List<Column> columns = buildColumns(tableNode);

        table.setColumns(columns);
        return table;
    }

    private Table buildTargetTable(Element integrationNode, Table sourceTable) {
        List<Element> tableNodes = reader.getChildElements(integrationNode, TargetTableTag.TAG_NAME.getConstant());
        Element tableNode = tableNodes.get(0);

        Table table = new Table();
        table.setAlias(tableNode.getAttribute(TargetTableTag.ATTR_ALIAS.getConstant()));
        table.setDbTableName(tableNode.getAttribute(TargetTableTag.ATTR_DB_TABLE_NAME.getConstant()));
        table.setSchema(tableNode.getAttribute(TargetTableTag.ATTR_DB_SCHEMA_NAME.getConstant()));

        List<Column> columns = buildMappedColumns(tableNode, sourceTable);

        table.setColumns(columns);
        return table;
    }

    private List<Column> buildColumns(Element tableNode) {
        List<Element> columNodes = reader.getChildElements(tableNode, ColumnTag.TAG_NAME.getConstant());
        List<Column> result = new ArrayList<Column>();
        for (Element columnNode : columNodes) {
            Column column = new Column();
            column.setAlias(columnNode.getAttribute(ColumnTag.ATTR_ALIAS.getConstant()));
            column.setDbColumnName(columnNode.getAttribute(ColumnTag.ATTR_DB_COLUMN_NAME.getConstant()));
            column.setDataType(columnNode.getAttribute(ColumnTag.ATTR_DATA_TYPE.getConstant()));

            String isPrimaryKey = columnNode.getAttribute(ColumnTag.ATTR_IS_PRIMARY_KEY.getConstant());
            if (null != isPrimaryKey && "true".equals(isPrimaryKey)) {
                column.setPrimaryKey(true);
            }
            result.add(column);
        }

        return result;
    }

    private List<Column> buildMappedColumns(Element tableNode, Table sourceTable) {
        List<Element> mappedColumNodes = reader.getChildElements(tableNode, MappedColumnTag.TAG_NAME.getConstant());
        List<Column> result = new ArrayList<Column>();
        for (Element columnNode : mappedColumNodes) {
            MappedColumn mappedColumn = new MappedColumn();
            mappedColumn.setAlias(columnNode.getAttribute(MappedColumnTag.ATTR_ALIAS.getConstant()));
            mappedColumn.setDbColumnName(columnNode.getAttribute(MappedColumnTag.ATTR_DB_COLUMN_NAME.getConstant()));
            mappedColumn.setDataType(columnNode.getAttribute(MappedColumnTag.ATTR_DATA_TYPE.getConstant()));

            String isPrimaryKey = columnNode.getAttribute(MappedColumnTag.ATTR_IS_PRIMARY_KEY.getConstant());
            if (null != isPrimaryKey && "true".equals(isPrimaryKey)) {
                mappedColumn.setPrimaryKey(true);
            }

            String sourceColumnAlias = columnNode.getAttribute(MappedColumnTag.ATTR_SOURCE_COLUMN_ALIAS.getConstant());

            Column sourceColumn = sourceTable.findColumn(sourceColumnAlias);

            mappedColumn.setSourceColumn(sourceColumn);

            result.add(mappedColumn);
        }

        return result;
    }
}
