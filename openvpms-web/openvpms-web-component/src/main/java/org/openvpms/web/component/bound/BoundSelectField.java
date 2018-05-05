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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.bound;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ListDataEvent;
import nextapp.echo2.app.event.ListDataListener;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.property.Property;


/**
 * Binds a {@link Property} to a {@code SelectField}.
 *
 * @author Tim Anderson
 */
public class BoundSelectField extends SelectField implements BoundProperty {

    /**
     * The binder.
     */
    public Binder binder;


    /**
     * Constructs a {@code BoundSelectField}.
     *
     * @param property the property to bind
     * @param model    the list model
     */
    public BoundSelectField(final Property property, final ListModel model) {
        super(model);
        binder = new SelectFieldBinder(this, property);
        if (!StringUtils.isEmpty(property.getDescription())) {
            setToolTipText(property.getDescription());
        }
        model.addListDataListener(new ListDataListener() {
            @Override
            public void contentsChanged(ListDataEvent e) {
                binder.setField();
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                binder.setField();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                binder.setField();
            }
        });
    }

    /**
     * Life-cycle method invoked when the <code>Component</code> is added to a registered hierarchy.
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

    /**
     * Returns the property.
     *
     * @return the property
     */
    @Override
    public Property getProperty() {
        return binder.getProperty();
    }
}
