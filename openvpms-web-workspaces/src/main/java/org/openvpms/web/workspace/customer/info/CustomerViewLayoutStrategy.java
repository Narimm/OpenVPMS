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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.info;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectTabPaneModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

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
     * @param object     the parent object
     * @param properties the properties
     * @param model      the tab model
     * @param context    the layout context
     * @param shortcuts  if <tt>true</tt> include short cuts
     */
    @Override
    protected void doTabLayout(IMObject object, List<Property> properties, IMObjectTabPaneModel model, LayoutContext context,
                               boolean shortcuts) {
        super.doTabLayout(object, properties, model, context, shortcuts);
        Browser<Act> appointments = getAppointments((Party) object, context);
        Component inset = ColumnFactory.create(Styles.INSET, appointments.getComponent());

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
        final LayoutContext subContext = new DefaultLayoutContext(context);
        subContext.setComponentFactory(new TableComponentFactory(subContext));
        IMTableModel<Act> model = new CustomerAppointmentTableModel(subContext);
        Browser<Act> browser = new DefaultIMObjectTableBrowser<Act>(query, model, context);
        browser.addBrowserListener(new BrowserListener<Act>() {
            public void selected(Act object) {
                // switch to the appointment workspace, and select the appointment.
                onAppointmentSelected(object, subContext);
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

    /**
     * Invoked when an appointment is selected.
     * <p/>
     * If the appointment can be viewed at the current practice location, the appointment workspace will be switched
     * to.
     *
     * @param appointment   the appointment
     * @param layoutContext the layout context
     */
    private void onAppointmentSelected(Act appointment, LayoutContext layoutContext) {
        AppointmentRules rules = ServiceHelper.getBean(AppointmentRules.class);
        Context context = layoutContext.getContext();
        ContextSwitchListener listener = layoutContext.getContextSwitchListener();
        ActBean bean = new ActBean(appointment);
        Entity schedule = bean.getNodeParticipant("schedule");
        Party location = context.getLocation();
        if (schedule != null && location != null) {
            Entity view = rules.getScheduleView(location, schedule);
            if (view != null) {
                listener.switchTo(appointment);
            } else {
                Party newLocation = rules.getLocation(schedule);
                String name = (newLocation != null) ? newLocation.getName() : Messages.get("imobject.none");
                InformationDialog.show(Messages.format("customer.info.appointment.wrongLocation", name));
            }
        }
    }

}
