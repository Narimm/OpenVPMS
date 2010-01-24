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

import junit.framework.Assert;
import org.junit.Test;


/**
 * bstract test case for {@link DefaultOOConnectionPool}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractDefaultOOConnectionPoolTestCase
        extends AbstractOOConnectionPoolTestCase {

    /**
     * Verifies that a connection is destroyed after N uses if
     * {@link DefaultOOConnectionPool#setReuseCount(int)} is non-zero.
     */
    @Test
    public void testDestroy() {
        OOConnectionFactory factory = createFactory();
        TestConnectionPool pool = new TestConnectionPool(factory);
        int uses = 2;
        pool.setReuseCount(uses);
        for (int i = 0; i < factory.getMaxConnections() * uses; ++i) {
            OOConnection connection = pool.getConnection();
            checkConnection(connection);
            connection.close();
        }
        Assert.assertEquals(factory.getMaxConnections(), pool.getDestroyed());

        OOConnection connection = pool.getConnection();
        checkConnection(connection);
        connection.close();
    }

    /**
     * Creates a new connection factory.
     *
     * @return a new connection factory
     * @throws OpenOfficeException for any error
     */
    protected abstract OOConnectionFactory createFactory();

    /**
     * Creates a new connection pool.
     */
    protected OOConnectionPool createPool() {
        return new DefaultOOConnectionPool(createFactory());
    }


    private class TestConnectionPool extends DefaultOOConnectionPool {

        private int destroyed;

        /**
         * Creates a new <code>TestConnectionPool</code>.
         *
         * @param factory the connection factory
         */
        public TestConnectionPool(OOConnectionFactory factory) {
            super(factory);
        }

        /**
         * Returns the no. of times connections have been destroyed.
         *
         * @return the no. of times connections have been destroyed
         */
        public synchronized int getDestroyed() {
            return destroyed;
        }

        /**
         * Destroys a connection.
         *
         * @param state the connection state.
         */
        @Override
        protected synchronized void destroy(State state) {
            super.destroy(state);
            ++destroyed;
        }
    }
}
