/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.dao.hibernate.usertype;

// java core
import java.io.Serializable;

// java sql
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

// hibernate
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

//codehaus xstream
import com.thoughtworks.xstream.XStream;

// openvpms-framework
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;

/**
 * This user type will stream an {@link PropertyMap} into an 
 * XML buffer, which will eventually be persisted
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PropertyMapUserType implements UserType, Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Define the SQL types for {@link ItemStructure
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
    public int[] sqlTypes() {
         return SQL_TYPES;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    public Class returnedClass() {
        return PropertyMap.class;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Object obj1, Object obj2) throws HibernateException {
        if (obj1 == obj2) {
            return true;
        }
        
        if (obj1 == null || obj2 == null) {
            return false;
        } else {
            return obj1.equals(obj2);
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    public int hashCode(Object obj) throws HibernateException {
        return obj.hashCode();
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
        if (rs.wasNull()) {
            return null;
        }
        
        if (rs.getString(names[0]) == null) {
            return (PropertyMap)null;
        } else {
            return (PropertyMap)new XStream()
                .fromXML(rs.getString(names[0]));
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, new XStream().toXML(value));
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object obj) throws HibernateException {
        //TODO Implment the deep copy algorithm
        return obj;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable)value;
    }

    /* (non-Javadoc)
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
