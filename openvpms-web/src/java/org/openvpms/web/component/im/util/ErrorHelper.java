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

package org.openvpms.web.component.im.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Helper for displaying and logging errors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ErrorHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(ErrorHelper.class);


    /**
     * Display and log an error.
     *
     * @param error the error
     */
    public static void show(String error) {
        _log.error(error);
        ErrorDialog.show(error);
    }

    /**
     * Display and log an error.
     *
     * @param title the title
     * @param error the error
     */
    public static void show(String title, String error) {
        _log.error(error);
        ErrorDialog.show(title, error);
    }

    /**
     * Display and log an error.
     *
     * @param title the title
     * @param error the error
     */
    public static void show(String title, OpenVPMSException error) {
        _log.error(error.getLocalizedMessage(), error);
        ErrorDialog.show(title, error.getLocalizedMessage());
    }

    /**
     * Display and log an error.
     *
     * @param error the error
     */
    public static void show(OpenVPMSException error) {
        _log.error(error.getLocalizedMessage(), error);
        ErrorDialog.show(error.getLocalizedMessage());
    }
}
