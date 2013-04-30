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

package org.openvpms.web.component.bound;

import org.openvpms.web.component.palette.Palette;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;

import java.util.ArrayList;
import java.util.List;


/**
 * Binds a {@link Property} to a <tt>Palette</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BoundPalette<T> extends Palette<T> {

    /**
     * The bound property.
     */
    private final CollectionProperty property;


    /**
     * Constructs a new <code>BoundPalette</coce>.
     *
     * @param items    all items that may be selected
     * @param property the property to bind
     */
    @SuppressWarnings("unchecked")
    public BoundPalette(List<T> items, CollectionProperty property) {
        super(items, new ArrayList<T>(property.getValues()));
        this.property = property;
    }

    /**
     * Add items to the 'selected' list.
     *
     * @param values the values to add.
     */
    @Override
    protected void add(Object[] values) {
        for (Object value : values) {
            property.add(value);
        }
    }

    /**
     * Remove items from the 'selected' list.
     *
     * @param values the values to remove
     */
    @Override
    protected void remove(Object[] values) {
        for (Object value : values) {
            property.remove(value);
        }
    }
}
