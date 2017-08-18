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

package org.openvpms.web.component.bound;

import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.palette.Palette;

import java.util.ArrayList;
import java.util.List;


/**
 * Binds a {@link Property} to a {@link Palette}
 *
 * @author Tim Anderson
 */
public class BoundPalette<T> extends Palette<T> implements BoundProperty {

    /**
     * The bound property.
     */
    private final CollectionProperty property;

    /**
     * The listener to be notified of property updates.
     */
    private final ModifiableListener listener;

    /**
     * Constructs a {@link BoundPalette}.
     *
     * @param items    all items that may be selected
     * @param property the property to bind
     */
    @SuppressWarnings("unchecked")
    public BoundPalette(List<T> items, CollectionProperty property) {
        super(items, new ArrayList<T>(property.getValues()));
        this.property = property;
        listener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onModified();
            }
        };
        property.addModifiableListener(listener);
    }

    /**
     * Returns the property.
     *
     * @return the property
     */
    @Override
    public Property getProperty() {
        return property;
    }

    /**
     * Add items to the 'selected' list.
     *
     * @param values the values to add.
     */
    @Override
    protected void add(Object[] values) {
        try {
            property.removeModifiableListener(listener);
            for (Object value : values) {
                property.add(value);
            }
        } finally {
            property.addModifiableListener(listener);
        }
    }

    /**
     * Remove items from the 'selected' list.
     *
     * @param values the values to remove
     */
    @Override
    protected void remove(Object[] values) {
        try {
            property.removeModifiableListener(listener);
            for (Object value : values) {
                property.remove(value);
            }
        } finally {
            property.addModifiableListener(listener);
        }
    }

    /**
     * Updates the palette when the property changes.
     */
    @SuppressWarnings("unchecked")
    private void onModified() {
        setSelected(property.getValues());
    }
}
