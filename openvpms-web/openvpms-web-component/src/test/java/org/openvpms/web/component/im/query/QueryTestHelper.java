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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.collections.CollectionUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Query test case helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class QueryTestHelper {

    /**
     * Verifies that a query returns no results.
     *
     * @param query the query
     */
    public static <T extends IMObject> void checkEmpty(Query<T> query) {
        ResultSet<T> set = query.query();
        if (set != null) {
            assertEquals(0, set.getResults());
        }
    }

    /**
     * Checks that a query selects/doesn't select an object.
     *
     * @param selects if <tt>true</tt> expects the query to select the object, otherwise don't expect it
     * @param query   the query to check
     * @param value   the value to check
     */
    public static <T extends IMObject> void checkSelects(boolean selects, Query<T> query, T value) {
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
    public static <T extends IMObject> List<IMObjectReference> checkExists(T object, Query<T> query, boolean exists) {
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
    public static <T extends IMObject> void checkExists(T object, Query<T> query, List<IMObjectReference> matches,
                                                        boolean exists) {
        int cardinality = (exists) ? 1 : 0;
        assertEquals(cardinality, CollectionUtils.cardinality(object.getObjectReference(), matches));
        assertEquals(exists, query.selects(object));
        assertEquals(exists, query.selects(object.getObjectReference()));
    }

    /**
     * Helper to return all object references matching the query.
     *
     * @param query the query
     * @return the matching object's references
     */
    public static <T extends IMObject> List<IMObjectReference> getObjectRefs(Query<T> query) {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        for (T object : query) {
            result.add(object.getObjectReference());
        }
        return result;
    }

    /**
     * Helper to return all object references matching the query.
     *
     * @param set the result set
     * @return the matching object's references
     */
    public static <T extends IMObject> List<IMObjectReference> getObjectRefs(ResultSet<T> set) {
        List<IMObjectReference> result = new ArrayList<IMObjectReference>();
        ResultSetIterator<T> iterator = new ResultSetIterator<T>(set);
        while (iterator.hasNext()) {
            result.add(iterator.next().getObjectReference());
        }
        return result;
    }

}
