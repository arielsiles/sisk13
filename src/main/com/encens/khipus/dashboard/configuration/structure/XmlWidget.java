package com.encens.khipus.dashboard.configuration.structure;

import java.util.List;

/**
 * @author
 * @version 2.26
 */
public class XmlWidget {

    private String id;
    private String title;
    private String componentName;
    private String area;
    private String module;
    private String function;
    private XmlVerification verification;
    private XmlUnit unit;
    private List<XmlFilter> filters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public XmlVerification getVerification() {
        return verification;
    }

    public void setVerification(XmlVerification verification) {
        this.verification = verification;
    }

    public XmlUnit getUnit() {
        return unit;
    }

    public void setUnit(XmlUnit unit) {
        this.unit = unit;
    }

    public List<XmlFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<XmlFilter> filters) {
        this.filters = filters;
    }
}