package com.encens.khipus.util;

import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.SeamResourceBundle;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * MessageUtils
 *
 * @author
 * @version 1.2.3
 */
public class MessageUtils {

    public static String getMessage(String resourceKey) {
        return getMessage(resourceKey, new Object[0]);
    }

    public static String getMessage(String resourceKey, Object... params) {
        if (params == null) {
            params = new Object[0];
        }
        String messageTemplate = getDefaultMessage(resourceKey);
        ResourceBundle resourceBundle = SeamResourceBundle.getBundle();
        if (resourceBundle != null) {
            try {
                String bundleMessage = resourceBundle.getString(resourceKey);
                if (bundleMessage != null) {
                    //todo this part could be used if it's necessary send more than 10 parameters and isn't necessary use EL expressions, must be commented the down line
//                    messageTemplate = MessageFormat.format(bundleMessage, params);
                    messageTemplate = Interpolator.instance().interpolate(bundleMessage, params);
                }
            }
            catch (MissingResourceException mre) {
            }
        }
        return messageTemplate;
    }

    private static String getDefaultMessage(String resourceKey) {
        return "¿¿¿" + resourceKey + "???";
    }
}
