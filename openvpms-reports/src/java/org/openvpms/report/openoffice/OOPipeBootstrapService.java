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

import java.util.Random;


/**
 * Service to bootstrap an OpenOffice instance listening on a pipe,
 * on the local host.
 * The OpenOffice binaries must be in the path.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OOPipeBootstrapService extends OOBootstrapService {

    /**
     * The pipe name.
     */
    private final String name;

    /**
     * The pipe, cached on first access for the lifetime of the service.
     */
    private OOConnection pipe;


    /**
     * Constructs a new <code>OOPipeBootstrapService</code>, using
     * a random pipe name. The service is started.
     *
     * @throws OpenOfficeException if the service cannot be started
     */
    public OOPipeBootstrapService() {
        this(getRandomPipeName(), true);
    }

    /**
     * Constructs a new <code>OOPipeBootstrapService</code>, using
     * a named pipe. The service is started.
     *
     * @throws OpenOfficeException if the service cannot be started
     */
    public OOPipeBootstrapService(String name) {
        this(name, true);
    }

    /**
     * Constructs a new <code>OOPipeBootstrapService</code>, using
     * a named pipe.
     *
     * @param start if <code>true</code>, start the service
     * @throws OpenOfficeException if the service cannot be started
     */
    public OOPipeBootstrapService(String name, boolean start) {
        super("pipe,name=" + name);
        this.name = name;
        if (start) {
            start();
        }
    }

    /**
     * Starts the OpenOffice service.
     *
     * @throws OpenOfficeException if the service cannot be started
     */
    @Override
    public synchronized void start() {
        super.start();
        pipe = null;
    }

    /**
     * Returns a new {@link OOConnection} to the service.
     *
     * @return a new connection to the service
     * @throws OpenOfficeException if a connection cannot be established
     */
    public synchronized OOConnection getConnection() {
        if (pipe == null) {
            pipe = new PipeHandle(new OOPipeConnection(name));
        }
        return pipe;
    }

    /**
     * Generates a random pipe name.
     *
     * @return a random pipe name
     */
    private static String getRandomPipeName() {
        long id = new Random().nextLong() & 0x7fffffffffffffffL;
        return "uno" + Long.toString(id);
    }

    class PipeHandle implements OOConnection {

        /**
         * The underlying connection.
         */
        private final OOConnection connection;

        /**
         * Constructs a new <code>PipeHandle</code>.
         *
         * @param connection the connection
         */
        public PipeHandle(OOConnection connection) {
            this.connection = connection;
        }

        /**
         * Returns the component loader.
         *
         * @return the component loader
         * @throws OpenOfficeException for any error
         */
        public XComponentLoader getComponentLoader() {
            return connection.getComponentLoader();
        }

        /**
         * Returns a service of the specified type.
         *
         * @param name the service name
         * @param type the service type
         * @throws OpenOfficeException if the service can't be created
         */
        public Object getService(String name, Class type) {
            return connection.getService(name, type);
        }

        /**
         * Sets the listener for this connection.
         *
         * @param listener the listener. May be <code>null</code>
         */
        public void setListener(OOConnectionListener listener) {
            connection.setListener(listener);
        }

        /**
         * Closes the connection, releasing any resources.
         *
         * @throws OpenOfficeException for any error
         */
        public void close() {
            try {
                connection.close();
            } finally {
                synchronized (OOPipeBootstrapService.this) {
                    pipe = null;
                }
            }
        }
    }

}
