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

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * Abstract test case for {@link OOBootstrapConnectionPool}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractOOBootstrapConnectionPoolTestCase
        extends AbstractOOConnectionPoolTestCase {

    /**
     * Verifies that the OpenOffice service is restarted after N uses if
     * {@link OOBootstrapConnectionPool#setReuseCount(int)} is non-zero.
     */
    @Test
    public void testRestart() {
        TestConnectionPool pool = new TestConnectionPool(getService());
        int uses = 2;
        pool.setReuseCount(uses);
        for (int i = 0; i < pool.getCapacity() * uses; ++i) {
            OOConnection connection = pool.getConnection();
            checkConnection(connection);
            connection.close();
        }
        assertEquals(pool.getCapacity(), pool.getRestarted());

        OOConnection connection = pool.getConnection();
        checkConnection(connection);
        connection.close();
    }

    /**
     * Creates a new connection pool.
     *
     * @return a new connection pool
     */
    protected OOConnectionPool createPool() {
        return new OOBootstrapConnectionPool(getService());
    }

    private class TestConnectionPool extends OOBootstrapConnectionPool {

        /**
         * The no. of times the service has been restarted.
         */
        private int restarted;

        /**
         * Creates a new <code>TestConnectionPool</code>.
         *
         * @param service the bootstrap service
         */
        public TestConnectionPool(OOBootstrapService service) {
            super(service);
        }

        /**
         * Returns the no. of times the service has been restarted
         *
         * @return the no. of times the service has been restarted
         */
        public synchronized int getRestarted() {
            return restarted;
        }

        /**
         * Destroys a connection. Verifies that the service is restarted.
         *
         * @param state the connection state.
         */
        @Override
        protected synchronized void destroy(State state) {
            assertTrue(getService().isActive());
            super.destroy(state);
            assertFalse(getService().isActive());
            ++restarted;
        }
    }
}
