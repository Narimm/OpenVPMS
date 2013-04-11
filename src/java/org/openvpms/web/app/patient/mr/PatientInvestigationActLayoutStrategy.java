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

package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.doc.DocumentActLayoutStrategy;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.RowFactory;

import java.util.List;


/**
 * Layout strategy that includes a 'Print Form' button to print the act.
 */
public class PatientInvestigationActLayoutStrategy extends DocumentActLayoutStrategy {

    /**
     * Determines if the date node should be displayed read-only.
     */
    private boolean showDateReadOnly;

    /**
     * Determines if printing should be enabled.
     */
    private boolean enablePrint = true;


    /**
     * Constructs a <tt>PatientInvestigationActLayoutStrategy</tt>.
     */
    public PatientInvestigationActLayoutStrategy() {
        this(null, null);
    }

    /**
     * Constructs a <tt>PatientInvestigationActLayoutStrategy</tt>.
     *
     * @param editor         the document reference editor. May be <tt>null</tt>
     * @param versionsEditor the document version editor. May be <tt>null</tt>
     */
    public PatientInvestigationActLayoutStrategy(DocumentEditor editor,
                                                 ActRelationshipCollectionEditor versionsEditor) {
        super(editor, versionsEditor);
    }

    /**
     * Determines if the data should be displayed read-only.
     *
     * @param readOnly if <tt>true</tt> display the date read-only
     */
    public void setDateReadOnly(boolean readOnly) {
        showDateReadOnly = readOnly;
    }

    /**
     * Determines if the button should be enabled.
     *
     * @param enable if <tt>true</tt>, enable the button
     */
    public void setEnableButton(boolean enable) {
        enablePrint = enable;
    }

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <tt>property</tt>
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent,
                                             LayoutContext context) {
        ComponentState result = null;
        String name = property.getName();
        if (showDateReadOnly && name.equals("startTime")) {
            result = getReadOnlyComponent(property, parent, context);
        }

        if (result == null) {
            result = super.createComponent(property, parent, context);
        }
        return result;
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object      the object to lay out
     * @param parent      the parent object. May be <tt>null</tt>
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    @Override
    protected void doSimpleLayout(final IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                  PropertySet properties, Component container, final LayoutContext context) {
        if (enablePrint) {
            Button print = ButtonFactory.create("button.printform");
            print.addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    onPrint(object, context.getContext(), context.getHelpContext());
                }
            });
            RowLayoutData rowLayout = new RowLayoutData();
            Alignment topRight = new Alignment(Alignment.RIGHT, Alignment.TOP);
            rowLayout.setAlignment(topRight);
            print.setLayoutData(rowLayout);
            Grid grid = createGrid(object, descriptors, properties, context);
            Row row = RowFactory.create("WideCellSpacing", grid);
            ButtonSet set = new ButtonSet(row);
            set.add(print);
            container.add(ColumnFactory.create("Inset.Small", row));
        } else {
            super.doSimpleLayout(object, parent, descriptors, properties, container, context);
        }
    }

    /**
     * Invoked when the print button is pressed.
     *
     * @param object  the object to print
     * @param context the context
     * @param help    the help context
     */
    private void onPrint(IMObject object, Context context, HelpContext help) {
        try {
            ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, context);
            IMPrinter<IMObject> printer = IMPrinterFactory.create(object, locator);
            InteractiveIMPrinter<IMObject> iPrinter = new InteractiveIMPrinter<IMObject>(printer, help);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }


    /**
     * Helper to return a read-only component.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a read-only component to display the property
     */
    private ComponentState getReadOnlyComponent(Property property,
                                                IMObject parent,
                                                LayoutContext context) {
        ReadOnlyComponentFactory factory = new ReadOnlyComponentFactory(context);
        return factory.create(property, parent);
    }

}