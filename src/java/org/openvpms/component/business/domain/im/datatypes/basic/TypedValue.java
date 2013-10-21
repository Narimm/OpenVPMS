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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.datatypes.basic;

import com.thoughtworks.xstream.core.BaseException;
import org.apache.commons.lang.ObjectUtils;

import java.sql.Timestamp;
import java.util.Date;


/**
 * <tt>TypedValue</tt> manages the serialized (string) representation of an
 * object and/or tbe object itself, in order to make objects persistent
 * as strings. For performance, it performs lazy serialization and
 * deserialization.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TypedValue {

    /**
     * The symbolic type of the object.
     */
    private String type;

    /**
     * The serialized value.
     */
    private String value;

    /**
     * The object.
     */
    private Object object;

    /**
     * The current state, used to determine if (de)serialization needs to be
     * performed.
     */
    private State state;

    private enum State {
        OBJECT_DIRTY,  // indicates serialization needs to be perfomed
        VALUE_DIRTY    // indicates deserialization needs to be performed
    }


    /**
     * Constructs a new <tt>TypedValue</tt>.
     */
    public TypedValue() {
    }

    /**
     * Constructs a new <tt>TypedValue</tt>.
     *
     * @param object the object
     */
    public TypedValue(Object object) {
        setObject(object);
    }

    /**
     * Constructs a new <tt>TypedValue</tt>.
     *
     * @param type  the symbolic type
     * @param value the value string
     */
    public TypedValue(String type, String value) {
        this.type = type;
        setValue(value);
    }

    /**
     * Returns the symbolic type.
     *
     * @return the symbolic type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the symbolic type.
     *
     * @param type the symbolic type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the stringified value of the object, serializing it if required.
     *
     * @return the stringified value
     * @throws BaseException if the object cannot be serialized
     * @see TypedValueConverter
     */
    public String getValue() {
        if (state == State.OBJECT_DIRTY) {
            value = TypedValueConverter.toString(object);
            state = null;
        }
        return value;
    }

    /**
     * Sets the serialized value of the object.
     *
     * @param value the serialized value
     */
    public void setValue(String value) {
        this.value = value;
        state = State.VALUE_DIRTY;
    }

    /**
     * Returns the object, deserializing it from the string if required.
     *
     * @return the object
     * @throws BaseException if the object cannot be deserialized
     */
    public Object getObject() {
        if (state == State.VALUE_DIRTY) {
            object = TypedValueConverter.fromString(value, type);
            state = null;
        }
        return object;
    }

    /**
     * Sets the object.
     *
     * @param object the object
     */
    public void setObject(Object object) {
        if (object != null && object.getClass() == Date.class) {
            object = new Timestamp(((Date) object).getTime());
        }
        this.object = object;
        type = TypedValueConverter.getType(object);
        state = State.OBJECT_DIRTY;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        if (type != null) {
            hash = type.hashCode();
        }
        getValue();
        if (value != null) {
            hash += value.hashCode();
        }
        return hash;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (!equals) {
            if (obj instanceof TypedValue) {
                TypedValue other = (TypedValue) obj;
                if (ObjectUtils.equals(type, other.getType())
                    && ObjectUtils.equals(getValue(), other.getValue())) {
                    equals = true;
                }
            }
        }
        return equals;
    }
}
