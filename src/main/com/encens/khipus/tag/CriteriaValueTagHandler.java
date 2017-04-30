package com.encens.khipus.tag;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 2.22
 */
public class CriteriaValueTagHandler extends TagHandler {
    public static final String MAP_NAME = "criteriaValuesMap";

    private final TagAttribute fieldName;

    private final TagAttribute value;

    public CriteriaValueTagHandler(TagConfig config) {
        super(config);
        fieldName = this.getAttribute("fieldName");
        value = this.getAttribute("value");
    }

    @SuppressWarnings(value = "unchecked")
    public void apply(FaceletContext faceletContext,
                      UIComponent parent) throws IOException, FacesException, FaceletException, ELException {

        Map<String, Object> mapValues = (Map<String, Object>) parent.getAttributes().get(MAP_NAME);
        if (null == mapValues) {
            mapValues = new HashMap<String, Object>();
        }

        String fieldNameKey = fieldName.getValue();
        Object fieldValue = value.getObject(faceletContext);
        if (null != fieldNameKey && null != fieldValue) {
            mapValues.put(fieldNameKey, fieldValue);
        }

        parent.getAttributes().put(MAP_NAME, mapValues);
        this.nextHandler.apply(faceletContext, parent);
    }
}
