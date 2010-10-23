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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.query;

import org.apache.commons.collections.CollectionUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.test.AbstractAppTest;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base class for {@link Query} test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractQueryTest<T extends IMObject> extends AbstractAppTest {

    /**
     * Tests the behaviour of constraining the query using the {@link Query#setValue(String)} method.
     */
    @Test
    public void testQueryByValue() {
        Query<T> query = createQuery();
        String value = getUniqueValue();
        query.setValue(value);
        ResultSet<T> results = query.query();
        assertNotNull(results);
        assertEquals(0, results.getResults());
        T object = createObject(value, true);

        results = query.query();
        List<T> list = results.getPage(0).getResults();
        assertEquals(1, list.size());
        assertEquals(object, list.get(0));
    }

    /**
     * Verifies that the query returns the expected no. of results.
     */
    @Test
    public void testGetResults() {
        Query<T> query = createQuery();
        ResultSet<T> results = query.query();
        assertNotNull(results);
        int size = results.getResults();
        createObject(true);
        createObject(true);

        results = query.query();
        assertEquals(size + 2, results.getResults());
    }

    /**
     * Tests the {@link Query#selects} method.
     */
    @Test
    public void testSelects() {
        Query<T> query = createQuery();

        T object = createObject(getUniqueValue(), false);
        checkSelects(false, query, object);
        save(object);
        checkSelects(true, query, object);
    }

    /**
     * Checks that a query selects/doesn't select an object.
     *
     * @param selects if <tt>true</tt> expects the query to select the object, otherwise don't expect it
     * @param query   the query to check
     * @param value   the value to check
     */
    protected void checkSelects(boolean selects, Query<T> query, T value) {
        assertEquals(selects, query.selects(value));
        assertEquals(selects, query.selects(value.getObjectReference()));
    }

    /**
     * Checks to see if an objects is returned by a query and that the {@link Query#selects} method agrees.
     *
     * @param object the object
     * @param query  the query
     * @param exists determines if the object should exist or not
     * @return the list of the references that matched the query
     */
    protected List<IMObjectReference> checkExists(T object, Query<T> query, boolean exists) {
        List<IMObjectReference> matches = getObjectRefs(query);
        checkExists(object, query, matches, exists);
        return matches;
    }

    /**
     * Checks to see if an objects exists in a list of matches and that the {@link Query#selects} method agrees.
     *
     * @param object  the object
     * @param query   the query
     * @param matches the query results
     * @param exists  determines if the object should exist or not
     */
    protected void checkExists(T object, Query<T> query, List<IMObjectReference> matches, boolean exists) {
        int cardinality = (exists) ? 1 : 0;
        assertEquals(cardinality, CollectionUtils.cardinality(object.getObjectReference(), matches));
        assertEquals(exists, query.selects(object));
        assertEquals(exists, query.selects(object.getObjectReference()));
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected abstract Query<T> createQuery();

    /**
     * Creates a new object, selected by the query.
     *
     * @param save if <tt>true</tt> save the object, otherwise don't save it
     * @return the new object
     */
    protected T createObject(boolean save) {
        return createObject(getUniqueValue(), save);
    }

    /**
     * Creates a new object, selected by the query.
     *
     * @param value a value that can be used to uniquely identify the object
     * @param save  if <tt>true</tt> save the object, otherwise don't save it
     * @return the new object
     */
    protected abstract T createObject(String value, boolean save);

    /**
     * Generates a unique value which may be used for querying objects on.
     *
     * @return a unique value
     */
    protected abstract String getUniqueValue();

    /**
     * Helper to generate a unique value, based on the current system time and a prefix.
     *
     * @param prefix a prefix for the value
     * @return an unque value
     */
    protected String getUniqueValue(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }

    /**
     * Helper to return all object references matching the query.
     *
     * @param query the query
     * @return the matching object's references
     */
    protected List<IMObjectReference> getObjectRefs(Query<T> query) {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        for (T object : query) {
            result.add(object.getObjectReference());
        }
        return result;
    }
}
