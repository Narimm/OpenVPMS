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

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Editor for <em>entity.organisationScheduleView</em> and <em>entity.organisationWorkListView</em> objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ScheduleViewEditor extends AbstractIMObjectEditor {

    /**
     * The expression editor.
     */
    private final PropertyEditor expressionEditor;

    /**
     * Construct a new <tt>ScheduleViewEditor</tt>.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public ScheduleViewEditor(Entity object, IMObject parent,
                              LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        expressionEditor = new ExpressionEditor(
            getProperty("displayExpression"), object, getLayoutContext());
        getEditors().add(expressionEditor);
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
                if ("displayExpression".equals(property.getName())) {
                    return new ComponentState(expressionEditor.getComponent(),
                                              expressionEditor.getProperty(),
                                              expressionEditor.getFocusGroup());
                }
                return super.createComponent(property, parent, context);
            }

            /**
             * Determines the no. of columns to display.
             *
             * @param descriptors the node descriptors
             * @return the number of columns
             */
            @Override
            protected int getColumns(List<NodeDescriptor> descriptors) {
                return 1;
            }
        };


    }

    private static class ExpressionEditor
        extends AbstractPropertyEditor {

        /**
         * The wrapper component.
         */
        private Component container;

        /**
         * The component focus group.
         */
        private FocusGroup focus;

        /**
         * The expression dialog.
         */
        private final ScheduleViewExpressionEditor editor;

        /**
         * The help context.
         */
        private final HelpContext help;


        /**
         * Creates a new <tt>ExpressionEditor</tt>.
         *
         * @param property the property being edited
         * @param parent   the parent object
         * @param context  the layout context
         */
        public ExpressionEditor(Property property, IMObject parent,
                                LayoutContext context) {
            super(property);
            ComponentState state = context.getComponentFactory().create(
                property, parent);
            Component field = state.getComponent();
            Button test = ButtonFactory.create("test", new ActionListener() {
                public void onAction(ActionEvent onEvent) {
                    onTest();
                }
            });
            focus = state.getFocusGroup();
            focus.add(test);
            container = RowFactory.create("CellSpacing", field, test);
            boolean arrivalTime = TypeHelper.isA(
                parent, "entity.organisationScheduleView");
            editor = new ScheduleViewExpressionEditor(property, arrivalTime);
            help = context.getHelpContext();
        }

        /**
         * Returns the edit component.
         *
         * @return the edit component
         */
        public Component getComponent() {
            return container;
        }

        /**
         * Returns the focus group.
         *
         * @return the focus group, or <tt>null</tt> if the editor hasn't been
         *         rendered
         */
        public FocusGroup getFocusGroup() {
            return focus;
        }

        /**
         * Validates the object.
         *
         * @param validator the validator
         * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
         */
        @Override
        protected boolean doValidation(Validator validator) {
            boolean valid = super.doValidation(validator);
            if (valid) {
                try {
                    editor.evaluate();
                } catch (Throwable exception) {
                    valid = false;
                    ValidatorError error = new ValidatorError(getProperty(), exception.getMessage());
                    validator.add(getProperty(), error);
                }
            }
            return valid;
        }

        /**
         * Pops up a dialog to test the expression.
         */
        private void onTest() {
            String title = Messages.get("editor.edit.title", editor.getDisplayName());
            ScheduleViewExpressionDialog dialog = new ScheduleViewExpressionDialog(title, editor,
                                                                                   help.subtopic("test"));
            dialog.show();
        }

    }
}

