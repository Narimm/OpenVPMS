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

package org.openvpms.web.component.im.till;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.DefaultListModel;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

/**
 * An editor for <em>party.organisationTill</em> instances.
 *
 * @author Tim Anderson
 */
public class TillEditor extends AbstractIMObjectEditor {

    /**
     * Constructs a {@link TillEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public TillEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validateDrawerCommand(validator);
    }

    /**
     * Validates the open drawer command string.
     *
     * @param validator the validator
     * @return {@code true} if it is valid
     */
    private boolean validateDrawerCommand(Validator validator) {
        boolean valid = true;
        Property property = getProperty("drawerCommand");
        String command = property.getString();
        if (!StringUtils.isEmpty(command)) {
            String[] values = command.split(",");
            for (String value : values) {
                if (!validateControlCode(validator, property, value)) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }


    /**
     * Validates a drawer command control code.
     *
     * @param validator the validator
     * @param property  the drawer command property
     * @param code      the code to validate
     * @return {@code true} if it is valid
     */
    private boolean validateControlCode(Validator validator, Property property, String code) {
        boolean valid = false;
        try {
            int value = Integer.valueOf(code.trim());
            if (value >= 0 && value < 256) {
                valid = true;
            }
        } catch (NumberFormatException exception) {
            // do nothing
        }
        if (!valid) {
            validator.add(property, new ValidatorError(property, Messages.get("till.drawerCommand.invalid")));
        }
        return valid;
    }

    /**
     * Invoked to test the open drawer command.
     */
    private void onTest() {
        Validator validator = new DefaultValidator();
        if (!validate(validator)) {
            showErrors(validator);
        } else {
            String printer = getProperty("printerName").getString();
            String command = getProperty("drawerCommand").getString();
            if (StringUtils.isEmpty(printer) || StringUtils.isEmpty(command)) {
                ErrorHelper.show(Messages.get("till.drawerCommand.test"));
            } else {
                CashDrawer drawer = new CashDrawer((Entity) getObject());
                try {
                    drawer.open();
                } catch (Throwable exception) {
                    ErrorHelper.show(exception);
                }
            }
        }
    }

    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Apply the layout strategy.
         * <p/>
         * This renders an object in a {@code Component}, using a factory to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            Property printerName = properties.get("printerName");
            DefaultListModel model = new DefaultListModel(PrintHelper.getPrinters());
            SelectField field = BoundSelectFieldFactory.create(printerName, model);
            addComponent(new ComponentState(field, printerName));

            Property drawerCommand = properties.get("drawerCommand");
            ComponentState drawer = createComponent(drawerCommand, parent, context);
            Button button = ButtonFactory.create("button.test", new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onTest();
                }
            });
            Row row = RowFactory.create(Styles.CELL_SPACING, drawer.getComponent(), button);
            ComponentState state = new ComponentState(row, drawerCommand);
            addComponent(state);
            return super.apply(object, properties, parent, context);
        }
    }
}
