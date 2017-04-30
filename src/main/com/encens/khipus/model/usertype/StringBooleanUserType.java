package com.encens.khipus.model.usertype;

import com.encens.khipus.util.ValidatorUtil;
import org.apache.commons.lang.BooleanUtils;
import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 * The class StringBooleanUserType managed a boolean attribute of and entity
 * like a string value inside the database. E.j. true=S and false=N
 *
 * @author
 * @version 1.0.18
 */
public class StringBooleanUserType implements UserType, ParameterizedType {
    public static final String NAME = "StringBoolean";
    public static final String TRUE_PARAMETER = "IntegerBooleanUserType.trueParameter";
    public static final String FALSE_PARAMETER = "IntegerBooleanUserType.falseParameter";
    public static final String ACRONYM_TRUE_VALUE = "S";
    public static final String ACRONYM_FALSE_VALUE = "N";
    public static final String TRUE_VALUE = "SI";
    public static final String FALSE_VALUE = "NO";
    private String trueValue = "1";
    private String falseValue = "0";

    public int[] sqlTypes() {
        return new int[]{Types.VARCHAR};
    }

    public Class returnedClass() {
        return Boolean.class;
    }

    public Object nullSafeGet(ResultSet resultSet,
                              String[] names, Object owner)
            throws HibernateException, SQLException {

        if (names[0] != null && names[0].length() > 0) {
            String stringValue = resultSet.getString(names[0]);
            if (!ValidatorUtil.isBlankOrNull(stringValue)) {
                return BooleanUtils.toBoolean(stringValue, getTrueValue(), getFalseValue());
            }
        }

        return null;
    }


    public void nullSafeSet(PreparedStatement statement,
                            Object value, int index)
            throws HibernateException, SQLException {
        if (!ValidatorUtil.isBlankOrNull(String.valueOf(value))) {
            statement.setString(index, BooleanUtils.toString((Boolean) value, getTrueValue(), getFalseValue()));
        } else {
            statement.setNull(index, Types.VARCHAR);
        }
    }

    public boolean isMutable() {
        return false;
    }

    public Object deepCopy(Object value) {
        return value;
    }

    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    public boolean equals(Object x, Object y) {
        return (x == null && y == null) || (!(x == null || y == null) && x.equals(y));
    }

    public int hashCode(Object x) {
        return x.hashCode();
    }


    public void setParameterValues(Properties properties) {
        if (properties != null && properties.getProperty(TRUE_PARAMETER) != null
                && properties.getProperty(FALSE_PARAMETER) != null) {
            setTrueValue(properties.getProperty(TRUE_PARAMETER));
            setFalseValue(properties.getProperty(FALSE_PARAMETER));
        }
    }

    public String getTrueValue() {
        return trueValue;
    }

    public void setTrueValue(String trueValue) {
        this.trueValue = trueValue;
    }

    public String getFalseValue() {
        return falseValue;
    }

    public void setFalseValue(String falseValue) {
        this.falseValue = falseValue;
    }

}
