package com.encens.khipus.converter;


import com.encens.khipus.util.URLCipher;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Cipher converters for diferent types to be cipher/decipher from page params
 *
 * @author
 * @version 1.0
 */
//TODO: this throws an exception when using in conjunction with redirect.returnToCapturedView
@Name("cipherConverter")
//@Restrict("#{identity.loggedIn}")
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class CipherConverter {


    /**
     * Converter for java.lang.Long
     *
     * @return the Converter responsible to cipher/decipher
     */
    public Converter getForLong() {
        return new Converter() {
            public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {

                return Long.valueOf(((URLCipher) Component.getInstance("cipher")).decrypt(value));
            }

            public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {

                return ((URLCipher) Component.getInstance("cipher")).encrypt(String.valueOf(value));
            }
        };
    }

    /**
     * Converter for java.lang.String
     *
     * @return the Converter responsible to cipher/decipher
     */
    public Converter getForString() {

        return new Converter() {

            public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
                return ((URLCipher) Component.getInstance("cipher")).decrypt(value);
            }

            public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {

                return ((URLCipher) Component.getInstance("cipher")).encrypt((String) value);
            }
        };
    }

}
