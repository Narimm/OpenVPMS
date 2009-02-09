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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.ServiceNotInit;


/**
 * OpenOffice helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeHelper {

    /**
     * A reference to the OpenOffice connection pool.
     */
    private static OOConnectionPool pool;

    /**
     * A reference to the print service.
     */
    private static PrintService printService;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OpenOfficeHelper.class);


    /**
     * Initialises the helper.
     *
     * @param pool         a reference to the OpenOffice connection pool
     * @param printService the print service
     */
    public OpenOfficeHelper(OOConnectionPool pool,
                            PrintService printService) {
        OpenOfficeHelper.pool = pool;
        OpenOfficeHelper.printService = printService;
    }

    /**
     * Returns a reference to the {@link DefaultOOConnectionPool}. If one is not
     * available then raises an exception.
     *
     * @return the pool
     * @throws OpenOfficeException if the pool is not set
     */
    public static OOConnectionPool getConnectionPool() {
        if (pool == null) {
            throw new OpenOfficeException(ServiceNotInit);
        }

        return pool;
    }

    /**
     * Returns a reference to the {@link PrintService}. If one is not available,
     * raises an exception.
     *
     * @return the print service
     */
    public static PrintService getPrintService() {
        if (printService == null) {
            throw new OpenOfficeException(ServiceNotInit);
        }
        return printService;
    }

    /**
     * Closes a connection, catching any exceptions.
     *
     * @param connection the connection. May be <code>null</code>
     */
    public static void close(OOConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (OpenOfficeException exception) {
                log.warn(exception, exception);
            }
        }
    }

}
