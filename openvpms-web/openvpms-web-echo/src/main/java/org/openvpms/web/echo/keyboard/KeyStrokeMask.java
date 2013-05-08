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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.echo.keyboard;

import echopointng.KeyStrokes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.StringTokenizer;


/**
 * Key stroke mask.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class KeyStrokeMask {

    /**
     * The key stroke mask.
     */
    private int code = KeyStrokes.ALT_MASK;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(KeyStrokeMask.class);


    /**
     * Sets the key stroke mask.
     *
     * @param mask the mask name
     */
    public void setMask(String mask) {
        int code = 0;
        StringTokenizer tokens = new StringTokenizer(mask, "-");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if ("Ctrl".equalsIgnoreCase(token)) {
                code |= KeyStrokes.CONTROL_MASK;
            } else if ("Alt".equalsIgnoreCase(token)) {
                code |= KeyStrokes.ALT_MASK;
            } else if ("Shift".equalsIgnoreCase(token)) {
                code |= KeyStrokes.SHIFT_MASK;
            } else {
                log.warn("Unsupported key=" + token + " in mask=" + mask);
            }
        }
        if (code != 0) {
            this.code = code;
        }
    }

    /**
     * Returns the key code for a key, including the mask.
     *
     * @param key the key
     * @return the key code for the key
     */
    public int getKeyCode(char key) {
        return code | key;
    }
}

