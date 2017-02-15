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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowser;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.workspace.BrowserCRUDWorkspace;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.resource.i18n.Messages;

import java.util.HashMap;
import java.util.Map;


/**
 * Reminder generation workspace.
 *
 * @author Tim Anderson
 */
public class ReminderWorkspace extends BrowserCRUDWorkspace<Act, Act> {

    /**
     * Constructs a {@link ReminderWorkspace}.
     *
     * @param context     the context
     * @param mailContext the mail context
     */
    public ReminderWorkspace(Context context, MailContext mailContext) {
        super("reporting.reminder", context, false);
        setArchetypes(Archetypes.create(ReminderArchetypes.REMINDER_ITEMS, Act.class));
        setChildArchetypes(getArchetypes());
        setMailContext(mailContext);
    }

    /**
     * Determines if the parent object is optional (i.e may be {@code null},
     * when laying out the workspace.
     *
     * @return {@code true}
     */
    @Override
    protected boolean isParentOptional() {
        return true;
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    @Override
    protected CRUDWindow<Act> createCRUDWindow() {
        return ((ReminderTabbedBrowser) getBrowser()).getSelectedWindow();
    }

    /**
     * Creates a new query to populate the browser.
     *
     * @return a new query
     */
    @Override
    protected Query<Act> createQuery() {
        return new ReminderItemQuery();
    }

    /**
     * Creates a new browser.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        // create a layout context, with hyperlinks enabled
        LayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        TableComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);
        ReminderItemQuery errorQuery = new ReminderItemQuery(new ReminderItemObjectSetQuery(true));
        ReminderTabbedBrowser browser = new ReminderTabbedBrowser((ReminderItemQuery) query, errorQuery, context);
        browser.setListener(new TabbedBrowserListener() {
            @Override
            public void onBrowserChanged() {
                changeCRUDWindow();
            }
        });
        return browser;
    }

    /**
     * Changes the CRUD window depending on the current browser view.
     */
    private void changeCRUDWindow() {
        ReminderTabbedBrowser browser = (ReminderTabbedBrowser) getBrowser();
        setCRUDWindow(browser.getSelectedWindow());
        setWorkspace(createWorkspace());
    }


    private static class ReminderTabbedBrowser extends TabbedBrowser<Act> {

        private Map<Integer, CRUDWindow<Act>> windows = new HashMap<>();

        public ReminderTabbedBrowser(ReminderItemQuery query, ReminderItemQuery errorQuery, LayoutContext context) {
            addBrowser("reporting.reminder.send", new ReminderItemBrowser(query, context), context);
            addBrowser("reporting.reminder.errors", new ReminderItemBrowser(errorQuery, context), context);
        }

        public CRUDWindow<Act> getSelectedWindow() {
            return windows.get(getSelectedBrowserIndex());
        }

        private void addBrowser(String key, ReminderItemBrowser browser, LayoutContext context) {
            int index = addBrowser(Messages.get(key), browser);
            ReminderItemCRUDWindow window = new ReminderItemCRUDWindow(browser, context.getContext(),
                                                                       context.getHelpContext());
            windows.put(index, window);
        }
    }

}

