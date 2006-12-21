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
public interface OOConnectionPool {

    /**
     * Gets a connection, blocking for up to <code>timeout</code> milliseconds
     * until one becomes available.
     *
     * @param timeout the maximum no. of milliseconds to wait for a connection
     * @return a connection or <code>null</code> if none was available before
     *         the timeout expired
     * @throws OpenOfficeException if a connection error occurs
     */
    OOConnection getConnection(long timeout);

    /**
     * Gets a connection, blocking until one becomes available.
     *
     * @throws OpenOfficeException if a connection error occurs
     */
    OOConnection getConnection();

    /**
     * Returns the pool capacity.
     *
     * @return the pool capacity
     */
    int getCapacity();
}
