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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.admin.lookup;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Style;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.echo.colour.ColourHelper;


/**
 * An editor for <em>lookup.customerAlertType</em>.
 *
 * @author Tim Anderson
 */
public class CustomerAlertTypeEditor extends LookupEditor {

    /**
     * Constructs a {@link CustomerAlertTypeEditor}.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}.
     */
    public CustomerAlertTypeEditor(Lookup object, IMObject parent, LayoutContext context) {
        super(object, parent, context);
        getProperty("priority").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                setPriorityDefaultColour();
            }
        });
        if (object.isNew()) {
            setPriorityDefaultColour(); // sets the colour to that of the default priority
        }
    }

    /**
     * Defaults the colour to that from the style associated with the priority.
     */
    private void setPriorityDefaultColour() {
        Object code = getProperty("priority").getValue();
        if (code != null) {
            String styleName = "AlertType." + code;
            ApplicationInstance app = ApplicationInstance.getActive();
            Style style = app.getStyle(Label.class, styleName);
            if (style != null) {
                Color background = (Color) style.getProperty("background");
                if (background != null) {
                    getProperty("colour").setValue(ColourHelper.getString(background));
                }
            }
        }
    }
}
