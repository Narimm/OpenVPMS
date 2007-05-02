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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.domain.im.datatypes.basic;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;
import com.thoughtworks.xstream.converters.basic.BigIntegerConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.basic.ByteConverter;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.basic.DoubleConverter;
import com.thoughtworks.xstream.converters.basic.FloatConverter;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.LongConverter;
import com.thoughtworks.xstream.converters.basic.ShortConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.basic.URLConverter;
import com.thoughtworks.xstream.converters.extended.SqlDateConverter;
import com.thoughtworks.xstream.converters.extended.SqlTimeConverter;
import com.thoughtworks.xstream.converters.extended.SqlTimestampConverter;
import com.thoughtworks.xstream.core.BaseException;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Helper to convert objects to and from strings.
 * This uses converters from <tt>XStream</tt> which have clearer conversion
 * behaviours than those provided by <tt>BeanUtils</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class TypedValueConverter {

    /**
     * Map of classes to their corresponding converters.
     */
    private static final Map<Class, Pair> convertersByClass;

    /**
     * Map of type names to their corresponding converters.
     */
    private static final Map<String, SingleValueConverter> convertersByName;

    /**
     * Stream for XML serialization to string, when a converter doesn't exist.
     */
    private static final XStream stream = new XStream();


    /**
     * Serializes an object to string.
     * Simple objects are serialized using
     * <tt>SingleValueConverter.toString(Object)</tt> methods provided the
     * <tt>XStream</tt> framework. Complex objects, or those for which no
     * converter is available are serialized to XML.
     *
     * @param object the object to serialize. May be <tt>null</tt>
     * @return the serialized object, or <tt>null</tt>
     * @throws BaseException if the object cannot be serialized
     */
    public static String toString(Object object) {
        if (object == null) {
            return null;
        }
        Pair pair = convertersByClass.get(object.getClass());
        if (pair == null) {
            return stream.toXML(object);
        } else {
            return pair.converter.toString(object);
        }
    }

    /**
     * Deserializes an object from a string.
     *
     * @param string the string to convert. May be <tt>null</tt>
     * @param type   the symbolic type of the object, as returned by
     *               {@link #getType}.
     * @return the deserialized object, or <tt>null</tt>
     * @throws BaseException if the object cannot be deserialized
     */
    public static Object fromString(String string, String type) {
        if (string == null) {
            return null;
        }
        SingleValueConverter converter = convertersByName.get(type);
        if (converter != null) {
            return converter.fromString(string);
        }
        return stream.fromXML(string);
    }

    /**
     * Returns a symbolic type for an object.
     *
     * @param object the object. May be <tt>null</tt>
     * @return the symbolic type for the object
     */
    public static String getType(Object object) {
        if (object == null) {
            return null;
        }
        Pair pair = convertersByClass.get(object.getClass());
        if (pair == null) {
            return object.getClass().getName();
        }
        return pair.type;
    }

    static {
        convertersByClass = new HashMap<Class, Pair>();
        convertersByName = new HashMap<String, SingleValueConverter>();

        //  use symbolic names as per aliases in XStream
        addConverter(Integer.class, "int", new IntConverter());
        addConverter(Float.class, "float", new FloatConverter());
        addConverter(Double.class, "double", new DoubleConverter());
        addConverter(Long.class, "long", new LongConverter());
        addConverter(Short.class, "short", new ShortConverter());
        addConverter(Character.class, "char", new CharConverter());
        addConverter(Byte.class, "byte", new ByteConverter());
        addConverter(Boolean.class, "boolean", new BooleanConverter());
        addConverter(BigDecimal.class, "big-decimal",
                     new BigDecimalConverter());
        addConverter(BigInteger.class, "big-int", new BigIntegerConverter());
        addConverter(String.class, "string", new StringConverter());
        addConverter(Date.class, "date", new DateConverter());
        addConverter(URL.class, "url", new URLConverter());
        addConverter(Timestamp.class, "sql-timestamp",
                     new SqlTimestampConverter());
        addConverter(Time.class, "sql-time", new SqlTimeConverter());
        addConverter(java.sql.Date.class, "sql-date", new SqlDateConverter());
        addConverter(Money.class, "money", new MoneyConverter());
    }

    /**
     * Helper to register a converter.
     *
     * @param type      the type to convert
     * @param name      the symbolic type name
     * @param converter the converter
     */
    private static void addConverter(Class type, String name,
                                     SingleValueConverter converter) {
        convertersByClass.put(type, new Pair(name, converter));
        convertersByName.put(name, converter);
    }

    /**
     * Helper to contain a type name and its corresponding converter.
     */
    private static class Pair {
        final String type;
        final SingleValueConverter converter;

        public Pair(String type, SingleValueConverter converter) {
            this.type = type;
            this.converter = converter;
        }
    }

    /**
     * Character converter.
     */
    private static class CharConverter extends AbstractSingleValueConverter {
        public boolean canConvert(Class type) {
            return type.equals(char.class) || type.equals(Character.class);
        }

        public Object fromString(String str) {
            if (str.length() != 1) {
                throw new ConversionException(
                        "Cannot convert " + str + " to char");
            }
            return str.charAt(0);
        }
    }

    /**
     * Money converter.
     */
    private static class MoneyConverter extends AbstractSingleValueConverter {

        public boolean canConvert(Class type) {
            return type.equals(Money.class);
        }

        @Override
        public Object fromString(String str) {
            return new Money(str);
        }
    }

}
