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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.test.TestHelper;

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
        createObject(getUniqueValue(), true);
        createObject(getUniqueValue(), true);

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
        assertFalse(query.selects(object));
        TestHelper.save(object);
        assertTrue(query.selects(object));
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
}
