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

package org.openvpms.component.system.common.jxpath;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.jxpath.JXPathTypeConversionException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.util.BasicTypeConverter;

/**
 * Extends the functionality in {@link BasicTypeConverter} to support
 * {@link BigDecimal} and {@link BigInteger}
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OpenVPMSTypeConverter extends BasicTypeConverter {

    /**
     * Default constructor
     */
    public OpenVPMSTypeConverter() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.jxpath.util.BasicTypeConverter#convert(java.lang.Object,
     *      java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object convert(Object object, Class toType) {
        if (object == null) {
            if (toType.isPrimitive()) {
                return convertNullToPrimitive(toType);
            }
            return null;
        }

        if (toType == Object.class) {
            if (object instanceof NodeSet) {
                return convert(((NodeSet) object).getValues(), toType);
            } else if (object instanceof Pointer) {
                return convert(((Pointer) object).getValue(), toType);
            }
            return object;
        }

        Class fromType = object.getClass();
        if (fromType.equals(toType) || toType.isAssignableFrom(fromType)) {
            return object;
        }

        if (fromType.isArray()) {
            int length = Array.getLength(object);
            if (toType.isArray()) {
                Class cType = toType.getComponentType();

                Object array = Array.newInstance(cType, length);
                for (int i = 0; i < length; i++) {
                    Object value = Array.get(object, i);
                    Array.set(array, i, convert(value, cType));
                }
                return array;
            } else if (Collection.class.isAssignableFrom(toType)) {
                Collection collection = allocateCollection(toType);
                for (int i = 0; i < length; i++) {
                    collection.add(Array.get(object, i));
                }
                return unmodifiableCollection(collection);
            } else {
                if (length > 0) {
                    Object value = Array.get(object, 0);
                    return convert(value, toType);
                } else {
                    return convert("", toType);
                }
            }
        } else if (object instanceof Collection) {
            int length = ((Collection) object).size();
            if (toType.isArray()) {
                Class cType = toType.getComponentType();
                Object array = Array.newInstance(cType, length);
                Iterator it = ((Collection) object).iterator();
                for (int i = 0; i < length; i++) {
                    Object value = it.next();
                    Array.set(array, i, convert(value, cType));
                }
                return array;
            } else if (Collection.class.isAssignableFrom(toType)) {
                Collection collection = allocateCollection(toType);
                collection.addAll((Collection) object);
                return unmodifiableCollection(collection);
            } else {
                if (length > 0) {
                    Object value;
                    if (object instanceof List) {
                        value = ((List) object).get(0);
                    } else {
                        Iterator it = ((Collection) object).iterator();
                        value = it.next();
                    }
                    return convert(value, toType);
                } else {
                    return convert("", toType);
                }
            }
        } else if (object instanceof NodeSet) {
            return convert(((NodeSet) object).getValues(), toType);
        } else if (object instanceof Pointer) {
            return convert(((Pointer) object).getValue(), toType);
        } else if (toType == String.class) {
            return object.toString();
        } else if (object instanceof Boolean) {
            if (toType == boolean.class) {
                return object;
            }
            boolean value = ((Boolean) object).booleanValue();
            return allocateNumber(toType, value ? 1 : 0);
        } else if (object instanceof BigDecimal) {
            BigDecimal value = (BigDecimal) object;
            if (toType == boolean.class || toType == Boolean.class) {
                return value == BigDecimal.ZERO ? Boolean.FALSE : Boolean.TRUE;
            }
            if (toType.isPrimitive() || Number.class.isAssignableFrom(toType)) {
                return allocateNumber(toType, value);
            }
        } else if (object instanceof BigInteger) {
            BigInteger value = (BigInteger) object;
            if (toType == boolean.class || toType == Boolean.class) {
                return value == BigInteger.ZERO ? Boolean.FALSE : Boolean.TRUE;
            }
            if (toType.isPrimitive() || Number.class.isAssignableFrom(toType)) {
                return allocateNumber(toType, value);
            }
        } else if (object instanceof Number) {
            double value = ((Number) object).doubleValue();
            if (toType == boolean.class || toType == Boolean.class) {
                return value == 0.0 ? Boolean.FALSE : Boolean.TRUE;
            }
            if (toType.isPrimitive() || Number.class.isAssignableFrom(toType)) {
                return allocateNumber(toType, value);
            }
        } else if (object instanceof Character) {
            if (toType == char.class) {
                return object;
            }
        } else if (object instanceof String) {
            Object value = convertStringToPrimitive(object, toType);
            if (value != null) {
                return value;
            }
        }

        Converter converter = ConvertUtils.lookup(toType);
        if (converter != null) {
            return converter.convert(toType, object);
        }

        throw new JXPathTypeConversionException("Cannot convert "
                + object.getClass() + " to " + toType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.jxpath.util.BasicTypeConverter#allocateNumber(java.lang.Class,
     *      double)
     */
    @Override
    protected Number allocateNumber(Class type, double value) {
        if (type == Byte.class || type == byte.class) {
            return new Byte((byte) value);
        }
        if (type == Short.class || type == short.class) {
            return new Short((short) value);
        }
        if (type == Integer.class || type == int.class) {
            return new Integer((int) value);
        }
        if (type == Long.class || type == long.class) {
            return new Long((long) value);
        }
        if (type == Float.class || type == float.class) {
            return new Float((float) value);
        }
        if (type == Double.class || type == double.class) {
            return new Double(value);
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(value);
        }
        if (type == BigInteger.class) {
            return BigInteger.valueOf((long) value);
        }
        return null;
    }

    /**
     * Convert a {@link BigDecimal} to another type
     * 
     * @param type
     *            the class to convert too
     * @param value
     *            the value to convert
     * @return Number
     *            the converted number of null.
     *            
     */
    protected Number allocateNumber(Class type, BigDecimal value) {
        if (type == Byte.class || type == byte.class) {
            return new Byte(value.byteValue());
        }
        if (type == Short.class || type == short.class) {
            return new Short(value.shortValue());
        }
        if (type == Integer.class || type == int.class) {
            return new Integer(value.intValue());
        }
        if (type == Long.class || type == long.class) {
            return new Long(value.longValue());
        }
        if (type == Float.class || type == float.class) {
            return new Float(value.floatValue());
        }
        if (type == Double.class || type == double.class) {
            return new Double(value.doubleValue());
        }
        if (type == BigDecimal.class) {
            return value;
        }
        if (type == BigInteger.class) {
            return BigInteger.valueOf(value.longValue());
        }

        return null;
    }

    /**
     * Convert a {@link BigInteger} to another type
     * 
     * @param type
     *            the class to convert too
     * @param value
     *            the value to convert
     * @return Number
     *            the converted number of null.
     *            
     */
    protected Number allocateNumber(Class type, BigInteger value) {
        if (type == Byte.class || type == byte.class) {
            return new Byte(value.byteValue());
        }
        if (type == Short.class || type == short.class) {
            return new Short(value.shortValue());
        }
        if (type == Integer.class || type == int.class) {
            return new Integer(value.intValue());
        }
        if (type == Long.class || type == long.class) {
            return new Long(value.longValue());
        }
        if (type == Float.class || type == float.class) {
            return new Float(value.floatValue());
        }
        if (type == Double.class || type == double.class) {
            return new Double(value.doubleValue());
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(value);
        }
        if (type == BigInteger.class) {
            return value;
        }

        return null;
    }

}
