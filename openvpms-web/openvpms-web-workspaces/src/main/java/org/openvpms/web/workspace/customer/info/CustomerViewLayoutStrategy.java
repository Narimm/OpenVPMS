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
 */
package org.openvpms.web.workspace.customer.info;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.tabpane.TabPaneModel;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;


/**
 * Layout strategy for customers that includes an appointment browser.
 *
 * @author Tim Anderson
 */
public class CustomerViewLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Lays out child components in a tab model.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param model       the tab model
     * @param context     the layout context
     * @param shortcuts   if <tt>true</tt> include short cuts
     */
    @Override
    protected void doTabLayout(IMObject object, List<NodeDescriptor> descriptors, PropertySet properties,
                               TabPaneModel model, LayoutContext context, boolean shortcuts) {
        super.doTabLayout(object, descriptors, properties, model, context, shortcuts);
        Browser<Act> appointments = getAppointments((Party) object, context);
        Component inset = ColumnFactory.create("Inset", appointments.getComponent());

        String label = Messages.get("customer.info.appointments");
        if (shortcuts && model.size() < 10) {
            label = getShortcut(label, model.size() + 1);
        }
        model.addTab(label, inset);
    }

    /**
     * Creates a new appointment browser.
     *
     * @param customer the customer
     * @param context  the layout context
     * @return a new appointment browser
     */
    protected Browser<Act> getAppointments(Party customer, LayoutContext context) {
        Query<Act> query = new CustomerAppointmentQuery(customer);
        IMTableModel<Act> model = new CustomerAppointmentTableModel(context);
        Browser<Act> browser = new DefaultIMObjectTableBrowser<Act>(query, model, context);
        final ContextSwitchListener listener = context.getContextSwitchListener();
        browser.addBrowserListener(new BrowserListener<Act>() {
            public void selected(Act object) {
                // switch to the appointment workspace, and select the appointment.
                listener.switchTo(object);
            }

            public void query() {
                // no-op
            }

            public void browsed(Act object) {
                // no-op
            }
        });
        return browser;
    }

}
