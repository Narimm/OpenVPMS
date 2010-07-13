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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Layout strategy that provides a button to print the object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PrintObjectLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The layout helper.
     */
    private PrintObjectLayoutHelper layout;


    /**
     * Constructs a new <tt>PrintObjectLayoutStrategy</tt>.
     *
     * @param label the button label
     */
    public PrintObjectLayoutStrategy(String label) {
        layout = new PrintObjectLayoutHelper(label);
    }

    /**
     * Determines if the button should be enabled.
     *
     * @param enable if <tt>true</tt>, enable the button
     */
    public void setEnableButton(boolean enable) {
        layout.setEnableButton(enable);
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(final IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context) {
        Button button = layout.doLayout(container);
        button.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onPrint(object);
            }
        });
        super.doLayout(object, properties, parent, container, context);
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
