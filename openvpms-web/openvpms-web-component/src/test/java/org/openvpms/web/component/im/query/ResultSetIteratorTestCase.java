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

package org.openvpms.web.component.im.query;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;


/**
 * Tests the {@link ResultSetIterator} class.
 *
 * @author Tim Anderson
 */
public class ResultSetIteratorTestCase {

    /**
     * Tests the behaviour of iterating an empty result set.
     */
    @Test
    public void testEmpty() {
        ResultSet<Integer> set = createResultSet(0, 10);
        Iterator<Integer> iterator = new ResultSetIterator<Integer>(set);
        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException expected) {
            // expected behaviour
        }
    }

    /**
     * Tests forward iteration.
     */
    @Test
    public void testForwardIteration() {
        ResultSet<Integer> resultSet = createResultSet(50, 20);
        ResultSetIterator<Integer> iterator = new ResultSetIterator<Integer>(resultSet);
        int count = 0;
        while (iterator.hasNext()) {
            int next = iterator.next();
            assertEquals(count, next);
            assertEquals(count, iterator.lastIndex());
            assertEquals(count + 1, iterator.nextIndex());
            ++count;
        }
        assertEquals(resultSet.getResults(), iterator.nextIndex());
        assertEquals(resultSet.getResults(), count);
    }

    /**
     * Tests reverse iteration.
     */
    @Test
    public void testReverseIteration() {
        int size = 50;
        int pageSize = 20;
        ResultSet<Integer> resultSet = createResultSet(size, pageSize);
        ResultSetIterator<Integer> iterator = new ResultSetIterator<Integer>(resultSet);
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            ++count;
        }
        assertEquals(size, count);
        assertEquals(size, iterator.nextIndex());

        while (iterator.hasPrevious()) {
            --count;
            assertEquals(count, iterator.previousIndex());
            int previous = iterator.previous();
            assertEquals(count, previous);
        }
    }

    /**
     * Creates a new result set.
     *
     * @param size     the size of the set
     * @param pageSize the page size
     * @return a new result set
     */
    private ListResultSet<Integer> createResultSet(int size, int pageSize) {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < size; ++i) {
            result.add(i);
        }
        return new ListResultSet<Integer>(result, pageSize);
    }
}
