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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.openoffice;

import com.sun.star.frame.XComponentLoader;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test case for {@link OOConnectionPool}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractOOConnectionPoolTestCase {

    /**
     * The bootstrap service.
     */
    private OOBootstrapService service;


    /**
     * Tests allocation and release of connections.
     */
    @Test
    public void testConnectionPool() {
        OOConnectionPool pool = createPool();
        List<OOConnection> connections = new ArrayList<OOConnection>();
        for (int i = 0; i < pool.getCapacity(); ++i) {
            OOConnection connection = pool.getConnection();
            checkConnection(connection);
            connections.add(connection);
        }

        // should not be able to get another connection until one is released
        OOConnection none = pool.getConnection(1000);
        assertNull(none);
        for (OOConnection connection : connections) {
            connection.close();
        }

        OOConnection connection = pool.getConnection(1000);
        checkConnection(connection);
        connection.close();
    }

    /**
     * Returns the bootstrap service.
     *
     * @return the bootstrap service
     */
    public OOBootstrapService getService() {
        return service;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        service = createService();
        if (!service.isActive()) {
            service.start();
        }
    }

    /**
     * Cleans up the test case.
     */
    @After
    public void tearDown() {
        service.stop();
    }

    /**
     * Creates a new bootstrap service.
     *
     * @return a new bootstrap service
     * @throws OpenOfficeException for any error
     */
    protected abstract OOBootstrapService createService();

    /**
     * Creates a new connection pool.
     *
     * @return a new connection pool
     */
    protected abstract OOConnectionPool createPool();

    /**
     * Checks that methods can be invoked on a connection.
     *
     * @param connection the connection
     */
    protected void checkConnection(OOConnection connection) {
        assertNotNull(connection);
        XComponentLoader loader = connection.getComponentLoader();
        assertNotNull(loader);
    }
}
