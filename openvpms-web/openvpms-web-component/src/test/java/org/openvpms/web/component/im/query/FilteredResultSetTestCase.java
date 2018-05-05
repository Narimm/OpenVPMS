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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.junit.Test;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link FilteredResultSet}.
 *
 * @author Tim Anderson
 */
public class FilteredResultSetTestCase {

    /**
     * Tests the behaviour of iterating an empty set.
     */
    @Test
    public void testEmpty() {
        FilteredResultSet<Integer> set = createOddFilteredResultSet(new ArrayList<Integer>(), 20);
        assertFalse(set.hasNext());
        assertFalse(set.hasPrevious());
        assertEquals(0, set.getPages());
        assertEquals(0, set.getResults());
        assertNull(set.getPage(0));
        assertNull(set.getPage(1));
        assertEquals(20, set.getPageSize());
        assertTrue(set.isSortedAscending());
        assertTrue(set.getSortConstraints() != null && set.getSortConstraints().length == 0);
    }

    /**
     * Tests an {link FilteredResultSet} of integers where odd integers are filtered.
     */
    @Test
    public void testFilter() {
        List<Integer> objects = new ArrayList<>();
        for (int i = 0; i < 25; ++i) {
            objects.add(i);
        }
        FilteredResultSet<Integer> set = createOddFilteredResultSet(objects, 5);

        assertEquals(0, set.getEstimatedResults());
        assertTrue(set.hasNext());

        assertEquals(5, set.getEstimatedResults());
        assertFalse(set.isEstimatedActual());
        checkPage(set.next(), 5, 0, 0, 2, 4, 6, 8);

        assertTrue(set.hasNext());
        assertTrue(set.hasPrevious());

        assertEquals(10, set.getEstimatedResults());
        assertFalse(set.isEstimatedActual());
        checkPage(set.next(), 5, 5, 10, 12, 14, 16, 18);

        assertTrue(set.hasNext());
        assertTrue(set.hasPrevious());
        assertEquals(13, set.getEstimatedResults());
        assertTrue(set.isEstimatedActual());
        checkPage(set.next(), 5, 10, 20, 22, 24);

        // now page back
        assertFalse(set.hasNext());
        assertTrue(set.hasPrevious());
        assertEquals(13, set.getEstimatedResults());
        assertTrue(set.isEstimatedActual());
        checkPage(set.previous(), 5, 10, 20, 22, 24);
        checkPage(set.previous(), 5, 5, 10, 12, 14, 16, 18);
        checkPage(set.previous(), 5, 0, 0, 2, 4, 6, 8);

        assertTrue(set.hasNext());
        assertFalse(set.hasPrevious());

        assertEquals(13, set.getEstimatedResults());
        assertTrue(set.isEstimatedActual());

        // now check iteration
        set.reset();
        int count = 0;
        int expected = 0;
        ResultSetIterator<Integer> iter = new ResultSetIterator<>(set);
        while (iter.hasNext()) {
            assertEquals(expected, iter.next().intValue());
            expected += 2;
            count++;
        }
        assertEquals(13, count);
    }

    /**
     * Tests a {@link FilteredResultSet} where the filter inserts elements.
     */
    @Test
    public void testInsertingFilter() {
        List<Integer> objects = Arrays.asList(0, 10, 20, 30, 40);
        FilteredResultSet<Integer> set = new FilteredResultSet<Integer>(createSet(objects, 3)) {
            @Override
            protected void filter(Integer object, List<Integer> results) {
                if (object == 0) {
                    // fills the first page
                    results.addAll(Arrays.asList(0, 1, 2));
                } else if (object == 20) {
                    // starts in the 2nd page, ends on the 4th page
                    results.addAll(Arrays.asList(20, 21, 22, 23, 24, 25, 26));
                } else {
                    results.add(object);
                }
            }
        };

        checkPage(set.next(), 3, 0, 0, 1, 2);
        checkPage(set.next(), 3, 3, 10, 20, 21);
        checkPage(set.next(), 3, 6, 22, 23, 24);
        checkPage(set.next(), 3, 9, 25, 26, 30);
        checkPage(set.next(), 3, 12, 40);
        assertFalse(set.hasNext());

        checkPage(set.previous(), 3, 12, 40);
        checkPage(set.previous(), 3, 9, 25, 26, 30);
        checkPage(set.previous(), 3, 6, 22, 23, 24);
        checkPage(set.previous(), 3, 3, 10, 20, 21);
        checkPage(set.previous(), 3, 0, 0, 1, 2);
        assertFalse(set.hasPrevious());

        // now test random access. Reset the set so the internal cache is cleared
        set.reset();
        checkPage(set.getPage(2), 3, 6, 22, 23, 24);
        checkPage(set.getPage(4), 3, 12, 40);
        checkPage(set.getPage(0), 3, 0, 0, 1, 2);
    }

    /**
     * Verifies that sorting the set resets it.
     */
    @Test
    public void testSort() {
        List<Integer> objects = new ArrayList<>();
        for (int i = 0; i < 25; ++i) {
            objects.add(i);
        }
        FilteredResultSet<Integer> set = createOddFilteredResultSet(objects, 5);

        checkPage(set.next(), 5, 0, 0, 2, 4, 6, 8);
        checkPage(set.next(), 5, 5, 10, 12, 14, 16, 18);
        set.sort(new SortConstraint[]{});

        checkPage(set.next(), 5, 0, 0, 2, 4, 6, 8);
    }

    /**
     * Tests when the page size is {@link ArchetypeQuery#ALL_RESULTS}.
     */
    @Test
    public void testAllResults() {
        List<Integer> objects = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            objects.add(i);
        }
        FilteredResultSet<Integer> set = createOddFilteredResultSet(objects, ArchetypeQuery.ALL_RESULTS);
        checkPage(set.next(), -1, 0, 0, 2, 4, 6, 8);
        assertFalse(set.hasNext());
        assertTrue(set.hasPrevious());
    }

    /**
     * Verifies that a page matches that expected.
     *
     * @param page     the page
     * @param size     the expected page size
     * @param offset   the expected page offset
     * @param expected the expected values
     */
    private void checkPage(IPage<Integer> page, int size, int offset, int... expected) {
        assertEquals(offset, page.getFirstResult());
        assertEquals(size, page.getPageSize());
        List<Integer> values = page.getResults();
        assertEquals(expected.length, values.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], values.get(i).intValue());
        }
    }

    /**
     * Creates an {@link FilteredResultSet} the excludes odd results.
     *
     * @param objects  the objects to filter
     * @param pageSize the page size
     * @return a new set
     */
    protected FilteredResultSet<Integer> createOddFilteredResultSet(List<Integer> objects, int pageSize) {
        ListResultSet<Integer> set = createSet(objects, pageSize);
        return new FilteredResultSet<Integer>(set) {
            @Override
            protected void filter(Integer object, List<Integer> results) {
                if (object % 2 == 0) {
                    results.add(object);
                }
            }
        };
    }

    /**
     * Creates a new {@link ListResultSet}.
     *
     * @param objects  the objects
     * @param pageSize the page size
     * @return a new set
     */
    private ListResultSet<Integer> createSet(List<Integer> objects, int pageSize) {
        return new ListResultSet<>(objects, pageSize);
    }
}
