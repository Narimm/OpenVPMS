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
 * Pool of {@link OOConnection} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultOOConnectionPool extends AbstractOOConnectionPool {

    /**
     * The connection factory.
     */
    private final OOConnectionFactory factory;


    /**
     * Creates a new <code>OOConnectionPool</code>.
     *
     * @param factory the connection factory
     */
    public DefaultOOConnectionPool(OOConnectionFactory factory) {
        super(factory.getMaxConnections());
        this.factory = factory;
    }

    /**
     * Creates a new connection.
     *
     * @return a new connection
     * @throws OpenOfficeException if the connection cannot be created
     */
    protected OOConnection create() {
        return factory.create();
    }

}
