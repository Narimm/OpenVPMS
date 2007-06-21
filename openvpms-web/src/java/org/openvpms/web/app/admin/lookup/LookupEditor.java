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

package org.openvpms.web.app.admin.lookup;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;


/**
 * Lookup editor. For lookups where there is are both code and name nodes, and
 * the code is hidden, this derives the initial value of code from the name.
 * The derived value is the name with letters converted to uppercase, and
 * anything it is not in the range [A-Z,0-9] replaced with underscores.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupEditor extends AbstractIMObjectEditor {

    /**
     * The code component.
     */
    private Component code;


    /**
     * Construct a new <code>LookupEditor</code>.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>.
     */
    public LookupEditor(Lookup object, IMObject parent, LayoutContext context) {
        super(object, parent, context);

        if (object.isNew()) {
            Property code = getProperty("code");
            Property name = getProperty("name");
            if (code != null && name != null) {
                if (code.isHidden()) {
                    // derive the code from the name
                    name.addModifiableListener(new ModifiableListener() {
                        public void modified(Modifiable modifiable) {
                            onNameModified();
                        }
                    });
                }
            }
        }
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    public boolean save() {
        boolean saved = super.save();
        if (saved && code != null) {
            code.setEnabled(false);
        }
        return saved;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AbstractLayoutStrategy() {

            @Override
            protected ComponentState createComponent(Property property,
                                                     IMObject parent,
                                                     LayoutContext context) {
                ComponentState state = super.createComponent(property,
                                                             parent,
                                                             context);
                if ("code".equals(property.getName())) {
                    code = state.getComponent();
                }
                return state;
            }
        };
    }

    /**
     * Invoked when layout has completed.
     * Disables the code property editor if the object has been saved.
     */
    @Override
    protected void onLayoutCompleted() {
        if (code != null && !getObject().isNew()) {
            code.setEnabled(false);
        }
    }

    /**
     * Invoked when the name is nodified. Derives the code, but only for
     * new objects.
     */
    private void onNameModified() {
        if (getObject().isNew()) {
            String code = null;
            String name = (String) getProperty("name").getValue();
            if (name != null) {
                code = name.toUpperCase();
                code = code.replaceAll("[^A-Z0-9]+", "_");
            }
            getProperty("code").setValue(code);
        }
    }
}
