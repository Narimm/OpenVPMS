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


/**
 * Manages the connection to a remote OpenOffice service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface OOConnection {

    /**
     * Returns the component loader.
     *
     * @return the component loader
     * @throws OpenOfficeException for any error
     */
    XComponentLoader getComponentLoader();

    /**
     * Returns a service of the specified type.
     *
     * @param name the service name
     * @param type the service type
     * @throws OpenOfficeException if the service can't be created
     */
    Object getService(String name, Class type);

    /**
     * Sets the listener for this connection.
     *
     * @param listener the listener. May be <code>null</code>
     */
    void setListener(OOConnectionListener listener);

    /**
     * Closes the connection, releasing any resources.
     *
     * @throws OpenOfficeException for any error
     */
    void close();

}
