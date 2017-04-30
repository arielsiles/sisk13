package com.encens.khipus.util;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.util.Iterator;

/**
 * JSF Utilities
 *
 * @author
 * @version 1.0
 */
public class JSFUtil {
    /**
     * Looks for a UIComponent given a simple id
     *
     * @param component the component which starts the search
     * @param id        the Id of the component we are looking for
     * @return the component found
     */
    public static UIComponent findComponent(UIComponent component, String id) {
        UIComponent target = null;
        UIComponent parent = component;
        UIComponent root = component;
        while (null == target && null != parent) {
            target = parent.findComponent(id);
            root = parent;
            parent = parent.getParent();
        }
        if (target != null) {
            return target;
        } else {
            return findUIComponentBelow(root, id);
        }
    }

    /**
     * Looks for a EditableValueHolder given a simple id
     *
     * @param component the component which starts the search
     * @param id        the Id of the component we are looking for
     * @return the editable component found
     */
    public static EditableValueHolder findEditableComponent(UIComponent component, String id) {
        UIComponent editableComponent = findComponent(component, id);
        if (editableComponent == null) {
            throw new FacesException("Unable to find component " + id);
        }
        if (!(editableComponent instanceof EditableValueHolder)) {
            throw new FacesException("Component '" + editableComponent.getId() + "' does not implement EditableValueHolder");
        }
        return (EditableValueHolder) editableComponent;
    }

    /**
     * Looks inside a component tree node.
     *
     * @param root the root component
     * @param id   the id to search
     * @return the component
     */
    private static UIComponent findUIComponentBelow(UIComponent root, String id) {
        UIComponent target = null;
        for (Iterator iter = root.getFacetsAndChildren(); iter.hasNext(); ) {
            UIComponent child = (UIComponent) iter.next();
            if (child instanceof NamingContainer) {
                try {
                    target = child.findComponent(id);
                } catch (IllegalArgumentException iae) {
                    continue;
                }
            }
            if (target == null && child.getChildCount() > 0) {
                target = findUIComponentBelow(child, id);
            }
            if (target != null) {
                break;
            }
        }
        return target;
    }

    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public static HttpSession getHttpSession() {
        return getHttpSession(true);
    }

    public static HttpSession getHttpSession(Boolean isOnSesssion) {
        return (HttpSession) getFacesContext().getExternalContext().getSession(isOnSesssion);
    }

    public static <ObjectResultType extends Object> ObjectResultType getSessionAttribute(Class<ObjectResultType> resultClass, String atrributeName) {
        return (ObjectResultType) getHttpSession().getAttribute(atrributeName);
    }

    public static void setSessionAttribute(String atrributeName, Object attributeValue) {
        getHttpSession().setAttribute(atrributeName, attributeValue);
    }

    public static void removeSessionAttribute(String attributeName) {
        getHttpSession().removeAttribute(attributeName);
    }

    public static HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) getFacesContext().getExternalContext().getRequest();
    }

    public static <ObjectResultType extends Object> ObjectResultType getRequestAttribute(Class<ObjectResultType> resultClass, String atrributeName) {
        return (ObjectResultType) getHttpServletRequest().getAttribute(atrributeName);
    }

    public static String getRequestParameter(String atrributeName) {
        return getHttpServletRequest().getParameter(atrributeName);
    }

    public static void setRequestAttribute(String atrributeName, Object attributeValue) {
        getHttpServletRequest().setAttribute(atrributeName, attributeValue);
    }

    public static void removeRequestAttribute(String attributeName) {
        getHttpServletRequest().removeAttribute(attributeName);
    }

    public static HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) getFacesContext().getExternalContext().getResponse();
    }

    public static Application getFacesApplication() {
        return getFacesContext().getApplication();
    }

    public static ExternalContext getExternalContext() {
        return getFacesContext().getExternalContext();
    }

    public static ServletContext getServletContext() {
        return (ServletContext) getExternalContext().getContext();
    }

    public static ELContext getELContext() {
        return getFacesContext().getELContext();
    }

    public static String getFacesRequestParameter(String name) {
        return getExternalContext().getRequestParameterMap().get(name);
    }


    public static String getRealPath(String url) {
        return getServletContext().getRealPath(url);
    }

    public static InputStream getResourceAsStream(String url) {
        return getServletContext().getResourceAsStream(url);
    }

    public static ValueExpression createValueExpression(String elExpression) {
        if (ValidatorUtil.isBlankOrNull(elExpression)) {
            return null;
        }

        return getFacesApplication().getExpressionFactory().createValueExpression(
                getELContext(),
                normalizeELExpression(elExpression),
                Object.class);
    }

    public static Class getValueExpressionType(String elExpression) {
        return createValueExpression(elExpression).getType(getELContext());
    }

    public static Object getValueExpression(String elExpression) {
        return createValueExpression(elExpression).getValue(getELContext());
    }

    public static void setValueExpression(String elExpression, Object value) {
        createValueExpression(elExpression).setValue(getELContext(), value);
    }

    public static MethodExpression createMethodExpression(String elExpression,
                                                          Class<?> expectedReturnType,
                                                          Class<?>[] expectedParamTypes) {
        if (ValidatorUtil.isBlankOrNull(elExpression)) {
            return null;
        }

        return getFacesApplication().getExpressionFactory().createMethodExpression(getELContext(),
                normalizeELExpression(elExpression),
                expectedReturnType,
                expectedParamTypes);
    }

    public static String normalizeELExpression(String elExpression) {
        if (!elExpression.startsWith("#")) {
            elExpression = "#{" + elExpression + "}";
        }
        return elExpression;
    }
}

