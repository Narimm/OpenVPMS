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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.list;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Font;


/**
 * A list cell that can be styled.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class StyledListCell extends AbstractStyledListCell {

    /**
     * Constructs a <tt>StyledListCell</tt>.
     *
     * @param value the cell value
     */
    public StyledListCell(String value) {
        super(value);
    }

    /**
     * Constructs a <tt>AStlyedListCell</tt> that gets its style from a label style.
     *
     * @param value     the cell value
     * @param styleName the style name
     */
    public StyledListCell(String value, String styleName) {
        super(value, styleName);
    }

    /**
     * Constructs a <tt>StyledListCell</tt> with the specified style.
     *
     * @param value      the cell value
     * @param background the background colour. May be <tt>null</tt>
     * @param foreground the foreground colour. May be <tt>null</tt>
     */
    public StyledListCell(String value, Color background, Color foreground) {
        this(value, background, foreground, null);
    }

    /**
     * Constructs a <tt>StyledListCell</tt> with the specified style.
     *
     * @param value      the cell value
     * @param background the background colour. May be <tt>null</tt>
     * @param foreground the foreground colour. May be <tt>null</tt>
     * @param font       the font. May be <tt>null</tt>
     */
    public StyledListCell(String value, Color background, Color foreground, Font font) {
        super(value, background, foreground, font);
    }
}
