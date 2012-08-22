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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.plugin.runas;


/**
 * Service to run an operation using the permissions of a particular user.
 *
 * @author Tim Anderson
 */
public interface RunAsService {

    /**
     * Returns the default user for running plugins.
     *
     * @return the default user, or {@code null} if there is no default user
     */
    String getDefaultUser();

    /**
     * Runs an operation as the specified user.
     *
     * @param runnable the operation to run
     * @param user     the user to run the operation as
     */
    void runAs(Runnable runnable, String user);

}
