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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.doc.DocumentActLayoutStrategy;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.SingleParticipationCollectionEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;

import java.util.List;


/**
 * Layout strategy that includes a 'Print Form' button to print the act.
 *
 * @author Tim Anderson
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
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly;


    /**
     * Constructs a {@code PatientInvestigationActLayoutStrategy}.
     */
    public PatientInvestigationActLayoutStrategy() {
        this(null, null, null);
    }

    /**
     * Constructs a {@code PatientInvestigationActLayoutStrategy}.
     *
     * @param editor         the document reference editor. May be {@code null}
     * @param versionsEditor the document version editor. May be {@code null}
     * @param productEditor  editor the product editor. May be {@code null}
     */
    public PatientInvestigationActLayoutStrategy(DocumentEditor editor,
                                                 ActRelationshipCollectionEditor versionsEditor,
                                                 SingleParticipationCollectionEditor productEditor) {
        super(editor, versionsEditor);
        if (productEditor != null) {
            addComponent(new ComponentState(productEditor));
        }
    }

    /**
     * Determines if the data should be displayed read-only.
     *
     * @param readOnly if {@code true} display the date read-only
     */
    public void setDateReadOnly(boolean readOnly) {
        showDateReadOnly = readOnly;
    }

    /**
     * Determines if the button should be enabled.
     *
     * @param enable if {@code true}, enable the button
     */
    public void setEnableButton(boolean enable) {
        enablePrint = enable;
    }

    /**
     * Determines if the product should be read-only.
     *
     * @param readOnly if {@code true} display the product read-only
     */
    public void setShowProductReadOnly(boolean readOnly) {
        showProductReadOnly = readOnly;
    }

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
        if (showDateReadOnly || showProductReadOnly) {
            ReadOnlyComponentFactory factory = new ReadOnlyComponentFactory(context);
            if (showDateReadOnly) {
                addComponent(factory.create(properties.get("startTime"), object));
            }
            if (showProductReadOnly) {
                addComponent(factory.create(properties.get("product"), object));
            }
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(final IMObject object, IMObject parent, List<Property> properties,
                                  Component container, final LayoutContext context) {
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
            ComponentGrid grid = createGrid(object, properties, context);
            Row row = RowFactory.create("WideCellSpacing", createGrid(grid));
            ButtonSet set = new ButtonSet(row);
            set.add(print);
            container.add(ColumnFactory.create("Inset.Small", row));
        } else {
            super.doSimpleLayout(object, parent, properties, container, context);
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
            IMPrinter<IMObject> printer = IMPrinterFactory.create(object, locator, context);
            InteractiveIMPrinter<IMObject> iPrinter = new InteractiveIMPrinter<IMObject>(printer, context, help);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }


}