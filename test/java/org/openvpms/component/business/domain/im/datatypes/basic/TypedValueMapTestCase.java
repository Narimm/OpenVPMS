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

import com.thoughtworks.xstream.converters.extended.SqlTimestampConverter;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link TypedValueMap} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TypedValueMapTestCase {

    /**
     * Verifies that addition of objects to a {@link TypedValueMap} are
     * reflected in the underlying map.
     */
    @Test
    public void testMap() {
        Map<String, TypedValue> underlying = new HashMap<String, TypedValue>();
        TypedValueMap map = new TypedValueMap(underlying);
        Date date = new Date();

        map.put("a", true);
        map.put("b", 1);
        map.put("c", "astring");
        map.put("d", BigDecimal.ZERO);
        map.put("e", date);

        check(map, underlying, "a", true);
        check(map, underlying, "b", 1);
        check(map, underlying, "c", "astring");
        check(map, underlying, "d", BigDecimal.ZERO);
        check(map, underlying, "e", date);
    }

    /**
     * Tests conversion of TypedValue populated with strings to their
     * corresponding Objects via TypedValueMap.
     */
    @Test
    public void testConversion() {
        SqlTimestampConverter converter = new SqlTimestampConverter();
        Date date = new Timestamp(System.currentTimeMillis());
        String dateStr = converter.toString(date);

        Map<String, TypedValue> underlying = new HashMap<String, TypedValue>();
        underlying.put("a", new TypedValue("boolean", "false"));
        underlying.put("b", new TypedValue("int", "2"));
        underlying.put("c", new TypedValue("string", "astring"));
        underlying.put("d", new TypedValue("big-decimal", "10.0"));
        underlying.put("e", new TypedValue("sql-timestamp", dateStr));

        TypedValueMap map = new TypedValueMap(underlying);
        check(map, underlying, "a", false);
        check(map, underlying, "b", 2);
        check(map, underlying, "c", "astring");
        check(map, underlying, "d", new BigDecimal("10.0"));
        check(map, underlying, "e", date);
    }

    /**
     * Tests the {@link TypedValueMap#remove} method.
     */
    @Test
    public void testRemove() {
        TypedValueMap map = new TypedValueMap();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("a"));
        assertFalse(map.containsValue(true));

        map.put("a", true);

        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsValue(true));

        assertEquals(true, map.remove("a"));

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("a"));
        assertFalse(map.containsValue(true));
    }

    /**
     * Tests the {@link TypedValueMap#putAll} method.
     */
    @Test
    public void testPutAll() {
        Map<String, TypedValue> underlying = new HashMap<String, TypedValue>();
        TypedValueMap map = new TypedValueMap(underlying);
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("a", true);
        source.put("b", 1);
        source.put("c", "astring");
        map.putAll(source);

        check(map, underlying, "a", true);
        check(map, underlying, "b", 1);
        check(map, underlying, "c", "astring");
    }

    /**
     * Tests the {@link TypedValueMap#clear()} method.
     */
    @Test
    public void testClear() {
        TypedValueMap map = new TypedValueMap();
        map.put("b", false);
        assertEquals(1, map.size());
        map.clear();
        assertEquals(0, map.size());
        assertFalse(map.containsKey("b"));
        assertFalse(map.containsValue(false));
    }

    /**
     * Tests the {@link TypedValueMap#keySet()} method.
     */
    @Test
    public void testKeySet() {
        TypedValueMap map = new TypedValueMap();
        map.put("a", true);
        map.put("b", 1);
        map.put("c", "astring");
        Set<String> keys = map.keySet();
        assertEquals(map.size(), keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
        assertFalse(keys.contains("d"));
        assertTrue(keys.remove("a"));
        assertFalse(map.containsKey("a"));
    }

    /**
     * Tests the {@link TypedValueMap#entrySet()} method.
     */
    @Test
    public void testEntrySet() {
        TypedValueMap map = new TypedValueMap();
        map.put("a", true);
        map.put("b", 1);
        map.put("c", "astring");
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        assertEquals(3, entries.size());
        checkEntry(entries, "a", true);
        checkEntry(entries, "b", 1);
        checkEntry(entries, "c", "astring");

        Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            ++removed;
        }
        assertEquals(3, removed);
        assertTrue(map.isEmpty());
    }

    /**
     * Tests the {@link TypedValueMap#values()} method.
     */
    @Test
    public void testValues() {
        TypedValueMap map = new TypedValueMap();
        map.put("a", true);
        map.put("b", 1);
        map.put("c", "astring");
        Collection<Object> values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains(true));
        assertTrue(values.contains(1));
        assertTrue(values.contains("astring"));
        assertFalse(values.contains(false));

        Iterator<Object> iterator = values.iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            ++removed;
        }
        assertEquals(3, removed);
        assertTrue(map.isEmpty());
    }

    /**
     * Tests the {@link TypedValueMap#create} method.
     */
    @Test
    public void testCreate() {
        Map<String, Object> source = new HashMap<String, Object>();
        source.put("a", true);
        source.put("b", 1);
        source.put("c", "astring");

        Map<String, TypedValue> map = TypedValueMap.create(source);
        assertEquals(3, map.size());
        check(map.get("a"), "boolean", "true");
        check(map.get("b"), "int", "1");
        check(map.get("c"), "string", "astring");
    }

    /**
     * Checks a {@link TypedValue}.
     *
     * @param typedValue the value to check
     * @param type       the expected type
     * @param value      the expected string value
     */
    private void check(TypedValue typedValue, String type, String value) {
        assertNotNull(typedValue);
        assertEquals(type, typedValue.getType());
        assertEquals(value, typedValue.getValue());
    }

    /**
     * Checks a set of entries for a particular key and value.
     *
     * @param entries the entries to check
     * @param key     the expected key
     * @param value   the expected value
     */
    private void checkEntry(Set<Map.Entry<String, Object>> entries,
                            String key, Object value) {
        boolean found = false;
        for (Map.Entry<String, Object> entry : entries) {
            if (entry.getKey().equals(key) && entry.getValue().equals(value)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    /**
     * Checks a {@link TypedValueMap} and underlying map for an expected key
     * and value.
     *
     * @param map        the map
     * @param underlying the undelying map
     * @param key        the expected key
     * @param value      the expected value
     */
    private void check(TypedValueMap map, Map<String, TypedValue> underlying,
                       String key, Object value) {
        assertTrue(map.containsKey(key));
        assertTrue(map.containsValue(value));
        assertEquals(value, map.get(key));

        assertTrue(underlying.containsKey(key));
        assertTrue(underlying.containsValue(new TypedValue(value)));
        assertEquals(value, underlying.get(key).getObject());
    }
}
