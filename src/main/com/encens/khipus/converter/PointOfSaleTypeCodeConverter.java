package com.encens.khipus.converter;

import com.encens.khipus.model.rest.PointOfSaleTypeCode;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.util.List;

public class PointOfSaleTypeCodeConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        List<PointOfSaleTypeCode> pointOfSaleTypeCodeList = (List<PointOfSaleTypeCode>) component.getAttributes().get("pointOfSaleTypeCodeList");

        if (pointOfSaleTypeCodeList != null) {
            for (PointOfSaleTypeCode code : pointOfSaleTypeCodeList) {
                if (code.getCodigoClasificador().toString().equals(value)) {
                    return code;
                }
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        if (object == null || !(object instanceof PointOfSaleTypeCode)) {
            return "";
        }
        return ((PointOfSaleTypeCode) object).getCodigoClasificador().toString();
    }
}
