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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.workspace.alert.Alert;
import org.openvpms.web.workspace.alert.AlertLayoutStrategy;


/**
 * Layout strategy for <em>act.patientAlert</em>.
 * This includes a field to display the associated alert type's priority and colour.
 *
 * @author Tim Anderson
 */
public class PatientAlertLayoutStrategy extends AlertLayoutStrategy {

    /**
     * Constructs a {@link PatientAlertLayoutStrategy}.
     */
    public PatientAlertLayoutStrategy() {
        super();
    }

    /**
     * Creates a component for the alert type.
     *
     * @param alert      the alert
     * @param object     the object to apply
     * @param properties the object's properties
     * @param context    the layout context
     * @return the component
     */
    @Override
    protected ComponentState createAlert(Alert alert, IMObject object, PropertySet properties, LayoutContext context) {
        ComponentState result;
        if (context.isEdit()) {
            result = super.createAlert(alert, object, properties, context);
        } else {
            TextField field = TextComponentFactory.create(20);
            field.setText(alert.getAlertType().getName());
            setAlertColour(alert, field);
            Property property = properties.get("alertType");
            result = new ComponentState(field, property);
        }
        return result;
    }
}
