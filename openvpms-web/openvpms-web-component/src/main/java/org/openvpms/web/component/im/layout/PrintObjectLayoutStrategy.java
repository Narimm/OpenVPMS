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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonColumn;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Layout strategy that provides a button to print the object.
 *
 * @author Tim Anderson
 */
public abstract class PrintObjectLayoutStrategy extends AbstractLayoutStrategy {

    private String buttonId;
    private Button button;

    private boolean enableButton = true;


    /**
     * Constructs a {@link PrintObjectLayoutStrategy}.
     *
     * @param buttonId the button identifier
     */
    public PrintObjectLayoutStrategy(String buttonId) {
        this.buttonId = buttonId;
    }

    /**
     * Determines if the button should be enabled.
     *
     * @param enable if {@code true}, enable the button
     */
    public void setEnableButton(boolean enable) {
        this.enableButton = enable;
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(final IMObject object, PropertySet properties, IMObject parent, Component container,
                            final LayoutContext context) {
        if (enableButton) {
            button = ButtonFactory.create(buttonId);
            button.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onPrint(object, context.getContext(), context.getHelpContext());
                }
            });
        }
        super.doLayout(object, properties, parent, container, context);
        if (enableButton) {
            getFocusGroup().add(button);
        }
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
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                  Component container, LayoutContext context) {
        if (button != null) {
            ComponentGrid grid = createGrid(object, properties, context);
            ButtonColumn column = new ButtonColumn(getFocusGroup());
            RowLayoutData layoutData = new RowLayoutData();
            layoutData.setAlignment(Alignment.ALIGN_TOP);
            column.setLayoutData(layoutData);
            addButton(column.getButtons());
            Row row = RowFactory.create(Styles.WIDE_CELL_SPACING, createGrid(grid), column);
            container.add(ColumnFactory.create(Styles.SMALL_INSET, row));
        } else {
            super.doSimpleLayout(object, parent, properties, container, context);
        }
    }

    /**
     * Adds the print button.
     *
     * @param set the button set
     */
    protected void addButton(ButtonSet set) {
        set.add(button);
    }

    /**
     * Invoked when the print button is pressed.
     *
     * @param object  the object to print
     * @param context the context
     * @param help    the help context
     */
    protected void onPrint(IMObject object, Context context, HelpContext help) {
        try {
            ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, context);
            IMPrinterFactory factory = ServiceHelper.getBean(IMPrinterFactory.class);
            IMPrinter<IMObject> printer = factory.create(object, locator, context);
            InteractiveIMPrinter<IMObject> iPrinter = new InteractiveIMPrinter<>(printer, context, help);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
