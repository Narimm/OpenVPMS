/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.util;

import nextapp.echo2.app.Extent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extent helper.
 *
 * @author Tim Anderson
 */
public class ExtentHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ExtentHelper.class);

    /**
     * Converts a value to an extent.
     * <p/>
     * At present this only supports numeric values.
     *
     * @param value the value. May be {@code null}
     * @return the extent, or {@code null} if {@code value} is null
     */
    public static Extent toExtent(String value) {
        int result = -1;
        if (value != null) {
            try {
                if (value.indexOf('.') != -1) {
                    // to support Google Chrome which submits floating point for scrollLeft, scrollTop
                    result = (int) Math.round(Double.parseDouble(value));
                } else {
                    result = Integer.valueOf(value);
                }
            } catch (NumberFormatException exception) {
                log.debug("Invalid extent: " + value);
            }
        }
        return result != 1 ? new Extent(result) : null;
    }
}
