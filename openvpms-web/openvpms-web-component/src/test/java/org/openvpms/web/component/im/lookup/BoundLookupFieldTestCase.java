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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.lookup;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.bound.AbstractBoundFieldTest;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;

import java.util.Arrays;


/**
 * Tests the {@link BoundLookupField} class.
 *
 * @author Tim Anderson
 */
public class BoundLookupFieldTestCase extends AbstractBoundFieldTest<BoundLookupField, String> {

    /**
     * The first lookup test value.
     */
    private static final Lookup lookup1 = new Lookup(new ArchetypeId("lookup.species"), "value1", "Species 1");

    /**
     * The second lookup test value.
     */
    private static final Lookup lookup2 = new Lookup(new ArchetypeId("lookup.species"), "value2", "Species 2");


    /**
     * Constructs a <tt>BoundLookupFieldTestCase</tt>.
     */
    public BoundLookupFieldTestCase() {
        super(lookup1.getCode(), lookup2.getCode());
    }

    /**
     * Returns the value of the field.
     *
     * @param field the field
     * @return the value of the field
     */
    protected String getValue(BoundLookupField field) {
        return field.getSelectedCode();
    }

    /**
     * Sets the value of the field.
     *
     * @param field the field
     * @param value the value to set
     */
    protected void setValue(BoundLookupField field, String value) {
        field.setSelected(value);
    }

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    protected BoundLookupField createField(Property property) {
        ListLookupQuery lookups = new ListLookupQuery(Arrays.asList(lookup1, lookup2));
        return new BoundLookupField(property, lookups, false);
    }

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    protected Property createProperty() {
        return new SimpleProperty("lookup", String.class);
    }
}
