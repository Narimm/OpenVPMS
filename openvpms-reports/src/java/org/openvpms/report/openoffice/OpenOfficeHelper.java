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

import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.ServiceNotInit;


/**
 * Helper to access the {@link OpenOfficeService} and {@link PrintService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeHelper {

    /**
     * A reference to the service.
     */
    private static OpenOfficeService _service;

    /**
     * A reference to the print service.
     */
    private static PrintService _printService;


    /**
     * Initialises the helper..
     *
     * @param service a reference to the archetype service
     */
    public OpenOfficeHelper(OpenOfficeService service,
                            PrintService printService) {
        _service = service;
        _printService = printService;
    }

    /**
     * Returns a reference to the {@link OpenOfficeService}. If one is not
     * available then raises an exception.
     *
     * @return the service
     * @throws OpenOfficeException if the value is not set
     */
    public static OpenOfficeService getService() {
        if (_service == null) {
            throw new OpenOfficeException(ServiceNotInit);
        }

        return _service;
    }

    /**
     * Returns a reference to the {@link PrintService}. If one is not available,
     * raises an exception.
     *
     * @return the print service
     */
    public static PrintService getPrintService() {
        if (_printService == null) {
            throw new OpenOfficeException(ServiceNotInit);
        }
        return _printService;
    }

}
