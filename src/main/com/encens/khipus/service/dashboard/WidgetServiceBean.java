package com.encens.khipus.service.dashboard;

import com.encens.khipus.dashboard.configuration.DashboardConfiguration;
import com.encens.khipus.dashboard.configuration.structure.XmlFilter;
import com.encens.khipus.dashboard.configuration.structure.XmlInterval;
import com.encens.khipus.dashboard.configuration.structure.XmlWidget;
import com.encens.khipus.dashboard.util.DashboardConfigurationInitializer;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.dashboard.*;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.26
 */
@Stateless
@Name("widgetService")
@AutoCreate
public class WidgetServiceBean extends GenericServiceBean implements WidgetService {

    @In("#{entityManager}")
    private EntityManager em;

    @In
    private DashboardConfigurationInitializer dashboardConfigurationInitializer;

    public Widget findByXmlId(String xmlId) {
        try {
            return (Widget) em.createNamedQuery("Widget.findByXmlId")
                    .setParameter("xmlId", xmlId).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Widget loadWidget(String xmlId) {
        Widget widget = new Widget();
        XmlWidget xmlWidget = DashboardConfiguration.INSTANCE.getWidget(xmlId);

        widget.setXmlId(xmlWidget.getId());
        widget.setTitle(MessageUtils.getMessage(xmlWidget.getTitle()));
        widget.setComponentName(xmlWidget.getComponentName());
        widget.setArea(xmlWidget.getArea());
        widget.setModule(xmlWidget.getModule());
        widget.setFunction(xmlWidget.getFunction());

        switch (xmlWidget.getVerification()) {
            case MONTH_END:
                widget.setVerification(Verification.MONTH_END);
                break;
            case ON_LINE:
                widget.setVerification(Verification.ON_LINE);
                break;
        }

        switch (xmlWidget.getUnit()) {
            case DAYS:
                widget.setUnit(Unit.DAYS);
                break;
            case UNITS:
                widget.setUnit(Unit.UNITS);
                break;
            case PERCENT:
                widget.setUnit(Unit.PERCENT);
                break;
        }

        List<Filter> filters = new ArrayList<Filter>();
        for (XmlFilter xmlFilter : xmlWidget.getFilters()) {
            if (xmlFilter instanceof XmlInterval) {
                XmlInterval xmlInterval = (XmlInterval) xmlFilter;
                Interval interval = new Interval();

                interval.setName(xmlInterval.getName());
                interval.setDescription(MessageUtils.getMessage(xmlInterval.getDescription()));
                interval.setIndex(xmlInterval.getIndex());
                interval.setColor(xmlInterval.getColor());
                interval.setMinValue(xmlInterval.getMinValue());
                interval.setMaxValue(xmlInterval.getMaxValue());

                filters.add(interval);
            }
        }

        widget.setFilters(filters);

        return widget;
    }
}
