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

package org.openvpms.web.component.im.query;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Tests the {@link ResultSetIterator} class over a {@link ListResultSet}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ListResultSetIteratorTestCase {

    /**
     * Tests the behaviour of iterating an empty result set.
     */
    @Test
    public void testEmpty() {
        List<Object> objects = new ArrayList<Object>();
        ListResultSet<Object> set = new ListResultSet<Object>(objects, 10);
        ResultSetIterator<Object> iterator = new ResultSetIterator<Object>(set);
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasPrevious());
        try {
            iterator.next();
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException expected) {
            // expected behaviour
        }
        try {
            iterator.previous();
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException expected) {
            // expected behaviour
        }
    }

    /**
     * Tests iteration where the no. of objects equals the page size.
     */
    @Test
    public void testIterationForCountEqualsPageSize() {
        checkIteration(10, 10);
    }

    /**
     * Tests iteration where the no. of objects is less than the page size.
     */
    @Test
    public void testIterationForCountLessThanPageSize() {
        checkIteration(7, 10);
    }

    /**
     * Tests iteration where the no. of objects is greater than the page size.
     */
    @Test
    public void testIterationForCountGreaterThanPageSize() {
        checkIteration(20, 7);
    }

    /**
     * Tests iteration part way through a result set, in both directions.
     */
    @Test
    public void testPartialIteration() {
        List<Integer> objects = new ArrayList<Integer>();
        int count = 3;
        for (int i = 0; i < count; ++i) {
            objects.add(i);
        }
        ResultSet<Integer> set = new ListResultSet<Integer>(objects, 20);
        ResultSetIterator<Integer> iterator = new ResultSetIterator<Integer>(set);
        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.next().intValue());
        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next().intValue());
        assertTrue(iterator.hasPrevious());
        assertEquals(1, iterator.previous().intValue());
        assertTrue(iterator.hasPrevious());
        assertEquals(0, iterator.previous().intValue());
        assertFalse(iterator.hasPrevious());
    }
    /**
     * Verifies iteration for the specified object count and page size.
     *
     * @param count the no. of objects to test with
     * @param pageSize the page size
     */
    private void checkIteration(int count, int pageSize) {
        List<Integer> objects = new ArrayList<Integer>();
        for (int i = 0; i < count; ++i) {
            objects.add(i);
        }

        ResultSet<Integer> set = new ListResultSet<Integer>(objects, pageSize);
        ResultSetIterator<Integer> iterator = new ResultSetIterator<Integer>(set);
        int expected = -1;
        while (iterator.hasNext()) {
            ++expected;
            assertEquals(expected, iterator.nextIndex());
            assertEquals(expected, iterator.next().intValue());
        }
        assertEquals(count - 1, expected);

        while (iterator.hasPrevious()) {
            assertEquals(expected, iterator.previousIndex());
            assertEquals(expected, iterator.previous().intValue());
            --expected;
        }
        assertEquals(-1, expected);
    }

}