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


/**
 * A factory for {@link OOSocketConnection} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OOSocketConnectionFactory implements OOConnectionFactory {

    /**
     * The bootstrap service.
     */
    private final OOSocketBootstrapService service;

    /**
     * The OpenOffice service host.
     */
    private final String host;

    /**
     * The OpenOffice service port.
     */
    private final int port;


    /**
     * Constructs a new <code>OOSocketConnectionFactory</code> using
     * connections provided by a bootstrap service.
     *
     * @param service the bootstrap service
     */
    public OOSocketConnectionFactory(OOSocketBootstrapService service) {
        this.service = service;
        host = null;
        port = -1;
    }

    /**
     * Constructs a new <code>OOSocketConnectionFactory</code.
     *
     * @param host the OpenOffice service host
     * @param port the OpenOffice service port
     */
    public OOSocketConnectionFactory(String host, int port) {
        this.host = host;
        this.port = port;
        service = null;
    }

    /**
     * Creates a new {@link OOConnection}.
     *
     * @return a new connection
     */
    public OOConnection create() {
        return (host != null) ? new OOSocketConnection(host, port) :
                service.getConnection();
    }

    /**
     * Returns the maximum no. of concurrent connections.
     *
     * @return the maximum no. of current connections
     */
    public int getMaxConnections() {
        return 1;
    }
}
