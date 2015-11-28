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

package org.openvpms.web.component.im.customer;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

/**
 * Editor for <em>party.customerperson</em> references.
 *
 * @author Tim Anderson
 */
public class CustomerReferenceEditor extends AbstractIMObjectReferenceEditor<Party> {

    /**
     * Constructs a {@link CustomerReferenceEditor}.
     *
     * @param property the reference property
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context
     */
    public CustomerReferenceEditor(Property property, IMObject parent, LayoutContext context) {
        this(property, parent, context, false);
    }

    /**
     * Constructs a {@link CustomerReferenceEditor}.
     *
     * @param property    the reference property
     * @param parent      the parent object. May be {@code null}
     * @param context     the layout context
     * @param allowCreate determines if objects may be created
     */
    public CustomerReferenceEditor(Property property, IMObject parent, LayoutContext context, boolean allowCreate) {
        super(property, parent, context, allowCreate);
    }

    /**
     * Sets the value property to the supplied object.
     *
     * @param object the object. May  be {@code null}
     * @return {@code true} if the value was set, {@code false} if it cannot be set due to error, or is the same as
     * the existing value
     */
    @Override
    public boolean setObject(Party object) {
        ContextHelper.setCustomer(getLayoutContext().getContext(), object);
        return super.setObject(object);
    }
}

