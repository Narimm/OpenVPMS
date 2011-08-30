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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import nextapp.echo2.app.Color;


/**
 * Colour helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ColourHelper {

    /**
     * Converts a <tt>Color</tt> to a string.
     *
     * @param value the colour to convert. May be <tt>null</tt>
     * @return the hexadecimal representation of the colour, or <tt>null</tt>
     *         if <tt>value</tt> is <tt>null</tt>
     */
    public static String getString(Color value) {
        if (value != null) {
            return "0x" + Integer.toHexString(value.getRgb());
        }
        return null;
    }

    /**
     * Converts a <tt>String</tt> to an integer and returns the
     * specified opaque <tt>Color</tt>. This method handles string
     * formats that are used to represent octal and hexidecimal numbers.
     *
     * @param value a <tt>String</tt> that represents
     *              an opaque color as a 24-bit integer. May be <tt>null</tt>
     * @return the new <tt>Color</tt> object, or <tt>null</tt> if
     *         <tt>value</tt> is <tt>null</tt> or cannot be convered
     */
    public static Color getColor(String value) throws NumberFormatException {
        if (value != null) {
            try {
                int rgb = Integer.decode(value);
                return new Color(rgb);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return null;
    }
}
