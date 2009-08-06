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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.RowFactory;


/**
 * Layout strategy that provides a button to print the object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PrintObjectLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The button label.
     */
    private final String label;

    /**
     * Determines if the button should be enabled.
     */
    private boolean enableButton = true;


    /**
     * Constructs a new <tt>PrintObjectLayoutStrategy</tt>.
     *
     * @param label the button label
     */
    public PrintObjectLayoutStrategy(String label) {
        this.label = label;
    }

    /**
     * Determines if the button should be enabled.
     *
     * @param enable if <tt>true</tt>, enable the button
     */
    public void setEnableButton(boolean enable) {
        this.enableButton = enable;
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(final IMObject object, PropertySet properties,
                            Component container, LayoutContext context) {
        Button button = ButtonFactory.create(
                label, new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onPrint(object);
                    }
                });
        button.setEnabled(enableButton);

        RowLayoutData rowLayout = new RowLayoutData();
        Alignment topRight = new Alignment(Alignment.RIGHT, Alignment.TOP);
        rowLayout.setAlignment(topRight);
        rowLayout.setWidth(new Extent(100, Extent.PERCENT));
        button.setLayoutData(rowLayout);
        Row row = RowFactory.create("InsetX", button);
        ColumnLayoutData columnLayout = new ColumnLayoutData();
        columnLayout.setAlignment(topRight);
        row.setLayoutData(columnLayout);
        container.add(row);
        super.doLayout(object, properties, container, context);
        getFocusGroup().add(button);
    }

    /**
     * Invoked when the print button is pressed.
     *
     * @param object the object to print
     */
    protected void onPrint(IMObject object) {
        try {
            IMPrinter<IMObject> printer = new IMObjectReportPrinter<IMObject>(object);
            InteractiveIMPrinter<IMObject> iPrinter = new InteractiveIMPrinter<IMObject>(printer);
            iPrinter.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
