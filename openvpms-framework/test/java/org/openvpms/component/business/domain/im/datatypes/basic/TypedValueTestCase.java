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

import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.extended.SqlTimestampConverter;
import com.thoughtworks.xstream.io.StreamException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tests the {@link TypedValue} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TypedValueTestCase {

    /**
     * Tests conversion of simple objects.
     *
     * @throws Exception for any error
     */
    @Test
    public void testObjectConversion() throws Exception {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        Time time = Time.valueOf("12:15:45");
        java.sql.Date sqlDate = java.sql.Date.valueOf("2006-06-12");

        check("int", 1, "1");
        check("float", 10.0f, "10.0");
        check("double", 20.0d, "20.0");
        check("long", 30l, "30");
        check("short", (short) 40, "40");
        check("char", 'a', "a");
        check("byte", (byte) 0x01, "1");
        check("boolean", true, "true");
        check("big-decimal", BigDecimal.ZERO, "0");
        check("big-int", BigInteger.TEN, "10");
        check("string", "astring", "astring");
        check("date", date, new DateConverter().toString(date));
        check("url", new URL("http://localhost:8080"), "http://localhost:8080");
        check("sql-timestamp", timestamp,
              new SqlTimestampConverter().toString(timestamp));
        check("sql-time", time, "12:15:45");
        check("sql-date", sqlDate, "2006-06-12");
        check("money", new Money("0.0"), "0.0");
    }

    /**
     * Tests the behaviour of the {@link TypedValue#setObject} method.
     */
    @Test
    public void testSetObject() {
        TypedValue value = new TypedValue(15);
        assertEquals("int", value.getType());

        value.setObject("astring");
        assertEquals("string", value.getType());

        value.setObject(null);
        assertEquals(null, value.getType());
    }

    /**
     * Tests the behaviour of the {@link TypedValue#setValue} method.
     */
    @Test
    public void testSetValue() {
        TypedValue value = new TypedValue("int", "15");
        assertEquals(15, value.getObject());

        value.setValue("34");
        assertEquals(34, value.getObject());

        value.setValue("astring");
        try {
            value.getObject();
            fail("Expected conversion to fail with NumberFormatException");
        } catch (NumberFormatException expected) {
            // the expected behaviour
        }
        value.setType("string");
        assertEquals("astring", value.getObject());
    }

    /**
     * Tests handling of nulls.
     */
    @Test
    public void testNull() {
        TypedValue value1 = new TypedValue(null);
        assertNull(value1.getType());
        assertNull(value1.getValue());
        assertNull(value1.getObject());

        TypedValue value2 = new TypedValue(null, null);
        assertNull(value2.getType());
        assertNull(value2.getValue());
        assertNull(value2.getObject());

        assertEquals(value1, value2);

        // the following usages don't make much sense, but verify that they
        // don't break
        TypedValue value3 = new TypedValue("int", null);
        assertEquals("int", value3.getType());
        assertNull(value3.getValue());
        assertNull(value3.getObject());

        TypedValue value4 = new TypedValue(null, "abc");
        assertNull(value4.getType());
        assertEquals("abc", value4.getValue());

        try {
            value4.getObject();
            fail("Expected conversion to fail with a StreamException");
        } catch (StreamException expected) {
            // expected behaviour
        }
    }

    /**
     * Tests that a complex object can be held and converted by serialization
     * to string.
     */
    @Test
    public void testSerialization() {
        List<String> list1 = new ArrayList<String>();
        list1.add("a");
        list1.add("b");
        list1.add("c");
        TypedValue value1 = new TypedValue(list1);
        assertEquals(ArrayList.class.getName(), value1.getType());

        // create a new typed value from the serialized string
        TypedValue value2 = new TypedValue(value1.getType(), value1.getValue());

        // verify that the object can be deserialized
        List<String> list2 = (List<String>) value2.getObject();
        assertEquals(list1, list2);
    }

    /**
     * Test behaviour of invalid types.
     */
    @Test
    public void testInvalidType() {
        TypedValue value = new TypedValue("AnInvalidType", "serializedvalue");
        try {
            value.getObject();
            fail("Expected conversion to fail with a StreamException");
        } catch (StreamException exception) {
            // the expected behaviour
        }
    }

    /**
     * Creates and check the contents of a {@link TypedValue}.
     *
     * @param type   the expected type
     * @param object the expected object
     * @param value  the expected value string
     */
    private void check(String type, Object object, String value) {
        // create using the object constructor
        TypedValue v1 = new TypedValue(object);
        assertEquals(type, v1.getType());
        assertEquals(object, v1.getObject());
        assertEquals(value, v1.getValue());

        // create using the serialized value constructor
        TypedValue v2 = new TypedValue(type, value);
        assertEquals(type, v2.getType());
        assertEquals(object, v2.getObject());
        assertEquals(value, v2.getValue());
    }
}
