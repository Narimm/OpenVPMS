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
 * Service to bootstrap an OpenOffice instance listening on a socket,
 * on the local host.
 * The OpenOffice binaries must be in the path.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OOSocketBootstrapService extends OOBootstrapService {

    /**
     * The OpenOffice service host.
     */
    private final String host;

    /**
     * The OpenOffice service port.
     */
    private final int port;


    /**
     * Constructs a new <code>OOSocketBootstrapService</code>, listening
     * on "localhost" and the specified port. The service is started.
     *
     * @param port the OpenOffice service port
     * @throws OpenOfficeException if the service cannot be started
     */
    public OOSocketBootstrapService(int port) {
        this(port, true);
    }

    /**
     * Constructs a new <code>OOSocketBootstrapService</code>, listening
     * on "localhost" and the specified port.
     *
     * @param port  the OpenOffice service port
     * @param start if <code>true</code>, start the service
     * @throws OpenOfficeException if the service cannot be started
     */
    public OOSocketBootstrapService(int port, boolean start) {
        this("localhost", port, start);
    }

    /**
     * Constructs a new <code>OOSocketBootstrapService</code>, listening
     * on the specified host and port.
     *
     * @param host  the OpenOffice service host
     * @param port  the OpenOffice service port
     * @param start if <code>true</code>, start the service
     * @throws OpenOfficeException if the service cannot be started
     */
    public OOSocketBootstrapService(String host, int port, boolean start) {
        super("socket,host=" + host + ",port=" + port);
        this.host = host;
        this.port = port;
        if (start) {
            start();
        }
    }

    /**
     * Returns a new {@link OOConnection} to the service.
     *
     * @return a new connection to the service
     * @throws OpenOfficeException if a connection cannot be established
     */
    public OOConnection getConnection() {
        return new OOSocketConnection(host, port);
    }

}
