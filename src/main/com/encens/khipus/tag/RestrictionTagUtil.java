package com.encens.khipus.tag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 2.20
 */
public class RestrictionTagUtil {
    public static final String ENCODE_TOKEN = "@";
    public static final String EL_TOKEN = "#";

    public static final RestrictionTagUtil i = new RestrictionTagUtil();

    private RestrictionTagUtil() {
    }

    public String encodeRestriction(String restriction) {
        return restriction.replace(EL_TOKEN, ENCODE_TOKEN);
    }

    public String decodeRestriction(String restriction) {
        return restriction.replace(ENCODE_TOKEN, EL_TOKEN);
    }

    public List<String> decodeRestrictions(List<String> encodedRestrictions) {
        List<String> result = new ArrayList<String>();
        if (null != encodedRestrictions) {
            for (String encodedRestriction : encodedRestrictions) {
                result.add(decodeRestriction(encodedRestriction));
            }
        }

        return result;
    }
}
