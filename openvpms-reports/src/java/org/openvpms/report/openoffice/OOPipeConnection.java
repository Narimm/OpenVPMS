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
 * Manages a pipe connection to a remote OpenOffice service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OOPipeConnection extends AbstractOOConnection {

    /**
     * Constructs a new <code>OOPipeConnection</code>.
     *
     * @param name the pipe name
     */
    public OOPipeConnection(String name) {
        super("pipe,name=" + name);
    }

    /**
     * Closes the connection, releasing any resources.
     * This implementation introduces a delay after the bridge has been closed
     * to ensure that the pipe is cleaned up correctly. TODO - find a more
     * robust solution
     */
    @Override
    protected void doClose() {
        super.doClose();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignore) {
            // no-op
        }
    }

}
