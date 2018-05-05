/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.dao.hibernate.usertype;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This user type will stream an {@link PropertyMap} into an XML buffer, which will eventually be persisted.
 *
 * @author Jim Alateras
 */
public class PropertyMapUserType implements UserType, Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Define the SQL types for {@link #sqlTypes()}.
     */
    private static final int[] SQL_TYPES = {Types.VARCHAR};

    /**
     * Default constructor
     */
    public PropertyMapUserType() {
        super();
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @Override
    public Class returnedClass() {
        return PropertyMap.class;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean equals(Object obj1, Object obj2) throws HibernateException {
        return ObjectUtils.equals(obj1, obj2);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    public int hashCode(Object obj) throws HibernateException {
        return obj.hashCode();
    }

    /**
     * Retrieve an instance of the mapped class from a JDBC resultset. Implementors
     * should handle possibility of null values.
     *
     * @param set     a JDBC result set
     * @param names   the column names
     * @param session the session
     * @param owner   the containing entity
     * @return the object. May be {@code null}
     * @throws SQLException for an SQL error
     */
    @Override
    public Object nullSafeGet(ResultSet set, String[] names, SessionImplementor session, Object owner) throws SQLException {
        String value = set.getString(names[0]);
        return (value != null) ? new XStream().fromXML(value) : null;
    }

    /**
     * Write an instance of the mapped class to a prepared statement. Implementors
     * should handle possibility of null values. A multi-column type should be written
     * to parameters starting from {@code index}.
     *
     * @param statement a JDBC prepared statement
     * @param value     the object to write
     * @param index     statement parameter index
     * @param session   the session
     * @throws SQLException for any SQL error
     */
    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, new XStream().toXML(value));
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    @Override
    public Object deepCopy(Object obj) throws HibernateException {
        return obj;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    @Override
    public boolean isMutable() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
