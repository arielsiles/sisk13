package com.encens.khipus.tag;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletException;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.20
 */
public class RestrictionTagHandler extends TagHandler {
    public static final String LIST_NAME = "restrictionList";

    private final TagAttribute condition;

    public RestrictionTagHandler(TagConfig config) {
        super(config);
        condition = this.getAttribute("condition");
    }

    @SuppressWarnings(value = "unchecked")
    public void apply(FaceletContext ctx,
                      UIComponent parent) throws IOException, FacesException, FaceletException, ELException {
        List<String> searchParameters = (List<String>) parent.getAttributes().get(LIST_NAME);
        if (null == searchParameters) {
            searchParameters = new ArrayList<String>();
        }

        String condition = getCondition(ctx);
        if (!searchParameters.contains(condition)) {
            searchParameters.add(condition);
        }

        parent.getAttributes().put(LIST_NAME, searchParameters);
        this.nextHandler.apply(ctx, parent);
    }

    private String getCondition(FaceletContext ctx) {
        ValueExpression attributeValueExpression = condition.getValueExpression(ctx, String.class);
        String attributeValue = attributeValueExpression.getExpressionString();
        return RestrictionTagUtil.i.encodeRestriction(attributeValue);
    }
}
