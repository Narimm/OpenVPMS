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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.macro.Macros;
import org.openvpms.macro.Variables;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.property.AbstractPropertyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyComponentFactory;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Renders a component that enables report parameters to be edited.
 *
 * @author Tim Anderson
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
     * The focus group.
     */
    private final FocusGroup focus = new FocusGroup(getClass().getSimpleName());

    /**
     * Constructs a {@code ReportParameters}.
     *
     * @param parameters the parameters
     * @param variables  the variables for macro expansion
     * @param columns    the number of columns to display the parameters in
     */
    public ReportParameters(Set<ParameterType> parameters, Variables variables, int columns) {
        this(parameters, null, variables, columns);
    }

    /**
     * Constructs  a {@code ReportParameters}.
     *
     * @param parameters the parameters
     * @param context    the parameter context, used for macro support. May be {@code null}
     * @param variables  the variables for macro expansion
     * @param columns    the number of columns to display the parameters in
     */
    public ReportParameters(Set<ParameterType> parameters, IMObject context, Variables variables, int columns) {
        properties = createProperties(parameters, context, variables);
        if (properties.size() > 0) {
            Grid grid;
            if (columns == 1) {
                grid = GridFactory.create(2);
                grid.setColumnWidth(0, new Extent(20, Extent.PERCENT));
                grid.setColumnWidth(1, new Extent(80, Extent.PERCENT));
            } else {
                grid = GridFactory.create(columns * 2);
            }
            grid.setWidth(Styles.FULL_WIDTH);
            PropertyComponentFactory factory = ComponentFactory.INSTANCE;
            for (Property property : properties) {
                if (property.isBoolean() || property.isString()
                    || property.isNumeric() || property.isDate()) {
                    Component component = factory.create(property);
                    Label label = LabelFactory.create();
                    label.setText(property.getDisplayName());
                    grid.add(label);
                    grid.add(component);
                    focus.add(component);
                }
            }
            component = grid;
        } else {
            component = LabelFactory.create("reporting.run.noparameters");
        }
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group.
     */
    public FocusGroup getFocusGroup() {
        return focus;
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
     * @return {@code true} if the parameters are valid
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
     * @param context    the parameter context, used for macro support. May be {@code null}
     * @param variables  the variables for macro expansion. May be {@code null}
     * @return the properties
     */
    private List<Property> createProperties(Set<ParameterType> parameters, Object context, Variables variables) {
        List<Property> result = new ArrayList<Property>();
        for (ParameterType type : parameters) {
            if (!type.isSystem()) {
                SimpleProperty property = new SimpleProperty(type.getName(), type.getType());
                if (type.getDescription() != null) {
                    property.setDisplayName(type.getDescription());
                }
                if (property.isBoolean() || property.isString() || property.isNumeric() || property.isDate()) {
                    Object defaultValue = type.getDefaultValue();
                    if (defaultValue != null) {
                        property.setValue(defaultValue);
                    }
                }
                if (property.isString()) {
                    // register a transformer that supports macro expansion
                    Macros macros = ServiceHelper.getMacros();
                    property.setTransformer(new StringPropertyTransformer(property, true, macros, context, variables));
                    property.setMaxLength(-1); // unlimited length
                }
                result.add(property);
            }
        }
        return result;
    }

    private static class ComponentFactory extends AbstractPropertyComponentFactory {

        /**
         * The singleton instance.
         */
        public static ComponentFactory INSTANCE = new ComponentFactory();


        /**
         * Constructs a {@code ComponentFactory}.
         */
        private ComponentFactory() {
            super(Styles.DEFAULT);
        }

        /**
         * This implementation creates TextAreas for long strings, that fill the available width.
         *
         * @param property the property to bind
         * @param columns  the maximum no, of columns to display
         * @return a new string component
         */
        @Override
        protected Component createString(Property property, int columns) {
            Component result;
            if (property.getMaxLength() == -1 || property.getMaxLength() > NodeDescriptor.DEFAULT_MAX_LENGTH) {
                TextArea text = BoundTextComponentFactory.createTextArea(property, columns, 5);
                text.setWidth(Styles.FULL_WIDTH);
                result = text;
            } else {
                result = super.createString(property, columns);
            }
            return result;
        }
    }
}
