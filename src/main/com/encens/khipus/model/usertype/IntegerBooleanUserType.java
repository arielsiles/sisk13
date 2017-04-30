package com.encens.khipus.model.usertype;

import com.encens.khipus.util.ValidatorUtil;
import org.apache.commons.lang.BooleanUtils;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * The class IntegerBooleanUserType managed a boolean attribute of and entity
 * like a integer value inside the database. E.j. true=1 and false=0
 *
 * @author
 * @version 1.0.18
 */
public class IntegerBooleanUserType implements UserType {
    public static final String NAME = "IntegerBoolean";

    public int[] sqlTypes() {
        return new int[]{Types.INTEGER};
    }

    public Class returnedClass() {
        return Boolean.class;
    }

    public Object nullSafeGet(ResultSet resultSet,
                              String[] names, Object owner)
            throws HibernateException, SQLException {
        if (resultSet != null && !ValidatorUtil.isBlankOrNull(names[0]) && !ValidatorUtil.isBlankOrNull(resultSet.getString(names[0]))) {
            int booleanAsInt = resultSet.getInt(names[0]);
            return BooleanUtils.toBoolean(booleanAsInt);
        }

        return null;
    }

    public void nullSafeSet(PreparedStatement statement,
                            Object value, int index)
            throws HibernateException, SQLException {
        if (value != null) {
            statement.setInt(index, BooleanUtils.toInteger((Boolean) value));
        } else {
            statement.setNull(index, Types.INTEGER);
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
}
