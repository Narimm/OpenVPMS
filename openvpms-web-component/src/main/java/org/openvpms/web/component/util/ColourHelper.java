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
    public static Color getColor(String value) {
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

    /**
     * Returns a colour that can be used as the text foreground/background to the supplied colour, so that the text is
     * readable.
     * <p/>
     * This returns <tt>Color.BLACK</tt> if the colour is closest to <tt>Color.WHITE</tt>, or <tt>Color.WHITE</tt>
     * if the colour is closer to <tt>Color.BLACK</tt>.
     *
     * @param colour the colour
     * @return a colour that may be used to ensure text is readable
     */
    public static Color getTextColour(Color colour) {
        int distToBlack = distance(colour, Color.BLACK);
        int distToWhite = distance(colour, Color.WHITE);
        return distToBlack < distToWhite ? Color.WHITE : Color.BLACK;
    }

    /**
     * Provides a rough calculation of the distance between two colours.
     * <p/>
     * This is based on:
     * <ul>
     * <li>the common RGB to grayscale approximation: 0.3*R + 0.59*G + 0.11*B
     * <li>treating the RGB values as XYZ coordinates and calculating the difference between them
     * </ul>
     * A more precise approach would be to calculate:
     * <pre> sqrt((0.3 x (c1.R - c2.R))**2 + (0.59 x (c1.G - c2.G))**2 + (0.11 x (c1.B - c2.B)**2)</pre>
     *
     * @param colour1 the first colour
     * @param colour2 the second colour
     * @return the distance between the two colours
     */
    private static int distance(Color colour1, Color colour2) {
        // RGB to grayscale of 30% red + 59% green + 11% blue
        return square(30 * (colour1.getRed() - colour2.getRed()))
               + square(59 * (colour1.getGreen() - colour2.getGreen()))
               + square(11 * (colour1.getBlue() - colour2.getBlue()));
    }

    /**
     * Helper to square an integer.
     *
     * @param value the value to square
     * @return the squared value
     */
    private static int square(int value) {
        return value * value;
    }
}
