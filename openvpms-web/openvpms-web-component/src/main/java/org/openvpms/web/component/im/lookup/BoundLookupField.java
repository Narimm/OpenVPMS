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

package org.openvpms.web.component.im.lookup;

import nextapp.echo2.app.list.ListModel;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.Binder;
import org.openvpms.web.component.bound.SelectFieldBinder;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.property.Property;


/**
 * Binds a lookup {@link Property} to a {@link LookupField}.
 *
 * @author Tim Anderson
 */
public class BoundLookupField extends LookupField {

    /**
     * The binder.
     */
    private Binder binder;


    /**
     * Constructs a {@link BoundLookupField}.
     *
     * @param property the property to bind
     * @param parent   the parent object
     */
    public BoundLookupField(Property property, IMObject parent) {
        this(property, new NodeLookupQuery(parent, property));
    }

    /**
     * Constructs a {@link BoundLookupField}.
     *
     * @param property the property to bind
     * @param source   the source of the lookups to display
     */
    public BoundLookupField(Property property, LookupQuery source) {
        this(property, source, false);
    }

    /**
     * Constructs a {@link BoundLookupField}.
     *
     * @param property the property to bind
     * @param source   the source of the lookups to display
     * @param all      if {@code true}, add a localised "All"
     */
    public BoundLookupField(Property property, LookupQuery source, boolean all) {
        this(property, new LookupListModel(source, all, !property.isRequired()));
    }

    /**
     * Constructs a {@link BoundLookupField}.
     *
     * @param property the property to bind
     * @param model    the model
     */
    public BoundLookupField(Property property, LookupListModel model) {
        super(model);
        binder = new SelectFieldBinder(this, property) {
            @Override
            protected boolean equals(Object value, ListModel model, int index) {
                // Local lookups have string keys, but the property could be an integer, so convert to a string
                // to perform comparisons
                Object other = model.get(index);
                if (other instanceof String) {
                    if (value != null) {
                        value = value.toString();
                    }
                }
                return ObjectUtils.equals(value, other);
            }
        };
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
        }
        if (getSelected() == null) {
            setDefaultSelection();
        }
    }

    /**
     * Refreshes the model if required.
     * <p/>
     * If the model refreshes, the selection will be cleared.
     *
     * @return {@code true} if the model refreshed
     */
    @Override
    public boolean refresh() {
        boolean result = super.refresh();
        if (result) {
            binder.setProperty();
        }
        return result;
    }

    /**
     * Life-cycle method invoked when the {@code Component} is added to a registered hierarchy.
     */
    @Override
    public void init() {
        super.init();
        binder.bind();
    }

    /**
     * Life-cycle method invoked when the {@code Component} is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }
}
