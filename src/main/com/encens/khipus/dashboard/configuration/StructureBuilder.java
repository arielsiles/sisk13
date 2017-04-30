package com.encens.khipus.dashboard.configuration;

import com.encens.khipus.dashboard.configuration.structure.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.encens.khipus.dashboard.configuration.XmlConstants.IntervalTag;
import static com.encens.khipus.dashboard.configuration.XmlConstants.WidgetTag;

/**
 * @author
 * @version 2.26
 */
public class StructureBuilder {

    private DOMReader reader = new DOMReader();

    public void validate(InputStream xmlFileInputStream) throws SAXException {
        reader.validate(xmlFileInputStream);
    }

    public XmlDashboard buildDashboard(InputStream xmlFileInputStream) {
        try {
            reader.parse(xmlFileInputStream);
        } catch (SAXException e) {
            throw new RuntimeException("Cannot parse xml file. ", e);
        }

        return buildDashboardElement(reader.getDocumentElement());
    }

    private XmlDashboard buildDashboardElement(Element dashboardElement) {
        XmlDashboard xmlDashboard = new XmlDashboard();

        List<XmlWidget> xmlWidgets = new ArrayList<XmlWidget>();
        List<Element> widgetElementList = reader.getChildElements(dashboardElement, WidgetTag.TAG_NAME.getConstant());

        for (Element element : widgetElementList) {
            XmlWidget xmlWidget = buildWidgetElement(element);
            xmlWidgets.add(xmlWidget);
        }

        xmlDashboard.setWidgets(xmlWidgets);

        return xmlDashboard;
    }

    private XmlWidget buildWidgetElement(Element widgetElement) {
        XmlWidget xmlWidget = new XmlWidget();

        xmlWidget.setId(widgetElement.getAttribute(WidgetTag.ATTR_ID.getConstant()));
        xmlWidget.setTitle(widgetElement.getAttribute(WidgetTag.ATTR_TITLE.getConstant()));
        xmlWidget.setComponentName(widgetElement.getAttribute(WidgetTag.ATTR_COMPONENT_NAME.getConstant()));
        xmlWidget.setArea(widgetElement.getAttribute(WidgetTag.ATTR_AREA.getConstant()));
        xmlWidget.setModule(widgetElement.getAttribute(WidgetTag.ATTR_MODULE.getConstant()));
        xmlWidget.setFunction(widgetElement.getAttribute(WidgetTag.ATTR_FUNCTION.getConstant()));

        String verification = widgetElement.getAttribute(WidgetTag.ATTR_VERIFICATION.getConstant());
        xmlWidget.setVerification(XmlVerification.valueOf(verification));

        String unit = widgetElement.getAttribute(WidgetTag.ATTR_UNIT.getConstant());
        xmlWidget.setUnit(XmlUnit.valueOf(unit));

        List<XmlFilter> xmlFilters = new ArrayList<XmlFilter>();
        List<Element> intervalElementList = reader.getChildElements(widgetElement, IntervalTag.TAG_NAME.getConstant());

        for (Element element : intervalElementList) {
            XmlInterval xmlInterval = buildIntervalElement(element);
            xmlFilters.add(xmlInterval);
        }

        xmlWidget.setFilters(xmlFilters);

        return xmlWidget;
    }

    private XmlInterval buildIntervalElement(Element intervalElement) {
        XmlInterval xmlInterval = new XmlInterval();

        xmlInterval.setName(intervalElement.getAttribute(IntervalTag.ATTR_NAME.getConstant()));
        xmlInterval.setDescription(intervalElement.getAttribute(IntervalTag.ATTR_DESCRIPTION.getConstant()));
        xmlInterval.setIndex(Integer.valueOf(intervalElement.getAttribute(IntervalTag.ATTR_INDEX.getConstant())));
        xmlInterval.setColor(Integer.valueOf(intervalElement.getAttribute(IntervalTag.ATTR_COLOR.getConstant())));
        xmlInterval.setMinValue(Integer.valueOf(intervalElement.getAttribute(IntervalTag.ATTR_MIN_VALUE.getConstant())));
        xmlInterval.setMaxValue(Integer.valueOf(intervalElement.getAttribute(IntervalTag.ATTR_MAX_VALUE.getConstant())));

        return xmlInterval;
    }
}
