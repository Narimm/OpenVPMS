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
 * A factory for {@link OOPipeConnection} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OOPipeConnectionFactory implements OOConnectionFactory {

    /**
     * The pipe name.
     */
    private final String name;

    /**
     * The bootstrap service.
     */
    private final OOPipeBootstrapService service;


    /**
     * Constructs a new <code>OOPipeConnectionFactory</code.
     *
     * @param name the pipe name
     */
    public OOPipeConnectionFactory(String name) {
        this.name = name;
        this.service = null;
    }

    /**
     * Constructs a new <code>OOPipeConnectionFactory</code> using
     * connections provided by a bootstrap service.
     *
     * @param service the bootstrap service
     */
    public OOPipeConnectionFactory(OOPipeBootstrapService service) {
        this.name = null;
        this.service = service;
    }

    /**
     * Creates a new {@link OOConnection}.
     *
     * @return a new connection
     */
    public OOConnection create() {
        return (name != null) ? new OOPipeConnection(name) :
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
