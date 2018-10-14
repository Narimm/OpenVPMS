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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.dao.hibernate.usertype;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This type handles the persistence of {@link Money} class.
 *
 * @author Jim Alateras
 */
public class MoneyUserType implements UserType, Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Define the SQL type
     */
    private static final int[] SQL_TYPES = {Types.NUMERIC};

    /**
     * Default constructor
     */
    public MoneyUserType() {
        super();
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @Override
    public Class returnedClass() {
        return Money.class;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean equals(Object obj1, Object obj2) throws HibernateException {
        return obj1 == obj2 || !(obj1 == null || obj2 == null) && obj1.equals(obj2);

    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    @Override
    public int hashCode(Object obj) throws HibernateException {
        return obj.hashCode();
    }

    /**
     * Retrieve an instance of the mapped class from a JDBC resultset. Implementors
     * should handle possibility of null values.
     *
     * @param rs      a JDBC result set
     * @param names   the column names
     * @param session the session
     * @param owner   the containing entity
     * @return the object. May be {@code null}
     * @throws HibernateException
     * @throws SQLException       for any SQL error
     */
    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        BigDecimal value = rs.getBigDecimal(names[0]);
        return (value != null) ? new Money(value) : null;
    }

    /**
     * Write an instance of the mapped class to a prepared statement. Implementors
     * should handle possibility of null values. A multi-column type should be written
     * to parameters starting from <tt>index</tt>.
     *
     * @param st      a JDBC prepared statement
     * @param value   the object to write
     * @param index   statement parameter index
     * @param session the session
     * @throws HibernateException
     * @throws SQLException
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.NUMERIC);
        } else {
            st.setBigDecimal(index, (BigDecimal) value);
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
        return false;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
