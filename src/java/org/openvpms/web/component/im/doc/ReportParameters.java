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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.property.DefaultPropertyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyComponentFactory;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Renders a component that enables report parameters to be edited.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportParameters {

    /**
     * The component.
     */
    private final Component component;

    /**
     * The report parameters.
     */
    private final List<Property> properties;


    /**
     * Creates a new <tt>ReportParameters</tt>.
     *
     * @param parameters the parameters
     */
    public ReportParameters(Set<ParameterType> parameters) {
        properties = createProperties(parameters);
        if (properties.size() > 0) {
            Grid grid;
            if (properties.size() <= 4) {
                grid = GridFactory.create(2);
            } else {
                grid = GridFactory.create(4);
            }
            PropertyComponentFactory factory
                    = DefaultPropertyComponentFactory.INSTANCE;
            for (Property property : properties) {
                if (property.isBoolean() || property.isString()
                        || property.isNumeric() || property.isDate()) {
                    Component component = factory.create(property);
                    Label label = LabelFactory.create();
                    label.setText(property.getDisplayName());
                    grid.add(label);
                    grid.add(component);
                }
            }
            component = grid;
        } else {
            component = LabelFactory.create("reporting.run.noparameters");
        }
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Validates the parameters, popping up an error dialog if the parameters
     * are invalid.
     *
     * @return <tt>true</tt> if the parameters are valid
     */
    public boolean validate() {
        boolean valid = true;
        Validator validator = new Validator();
        for (Property property : properties) {
            if (!validator.validate(property)) {
                valid = false;
                break;
            }
        }
        if (!valid) {
            ValidationHelper.showError(validator);
        }
        return valid;
    }

    /**
     * Returns the parameter values, keyed on name.
     *
     * @return the parameter values
     */
    public Map<String, Object> getValues() {
        Map<String, Object> result = new HashMap<String, Object>();
        for (Property property : properties) {
            result.put(property.getName(), property.getValue());

        }
        return result;
    }

    /**
     * Creates a list of properties for a set of report parameters.
     *
     * @param parameters the parameters
     * @return the properties
     */
    private List<Property> createProperties(Set<ParameterType> parameters) {
        List<Property> result = new ArrayList<Property>();
        for (ParameterType type : parameters) {
            if (!type.isSystem()) {
                SimpleProperty property = new SimpleProperty(type.getName(),
                                                             type.getType());
                if (type.getDescription() != null) {
                    property.setDisplayName(type.getDescription());
                }
                if (property.isBoolean() || property.isString()
                        || property.isNumeric() || property.isDate()) {
                    Object defaultValue = type.getDefaultValue();
                    if (defaultValue != null) {
                        property.setValue(defaultValue);
                    }
                }
                if (property.isString()) {
                    // a largish value which will force the component factory
                    // to create a TextArea, as opposed to a TextField
                    property.setMaxLength(300);
                }
                result.add(property);
            }
        }
        return result;
    }
}
