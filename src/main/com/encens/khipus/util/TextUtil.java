package com.encens.khipus.util;

import com.encens.khipus.model.common.Text;

/**
 * TextUtil
 *
 * @author
 * @version 1.2.3
 */
public class TextUtil {
    private TextUtil() {
    }

    public static Boolean isEmpty(Text text) {
        return text == null || ValidatorUtil.isBlankOrNull(text.getValue());
    }

    public static Text createOrUpdate(Text text, String value) {
        if (ValidatorUtil.isBlankOrNull(value)) {
            return null;
        }
        if (isEmpty(text)) {
            text = new Text();
        }
        text.setValue(value);
        return text;
    }

    public static String getTextValue(Text text) {
        if (!isEmpty(text)) {
            return text.getValue();
        }
        return "";
    }

}
