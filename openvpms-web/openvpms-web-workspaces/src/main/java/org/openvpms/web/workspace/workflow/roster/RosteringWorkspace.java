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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import echopointng.TabbedPane;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserAdapter;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.workspace.BrowserCRUDWindowTab;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.TabComponent;
import org.openvpms.web.component.workspace.TabbedWorkspace;
import org.openvpms.web.echo.dialog.DialogManager;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.echo.util.PeriodicTask;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * Rostering workspace.
 *
 * @author Tim Anderson
 */
public class RosteringWorkspace extends TabbedWorkspace<IMObject> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Used to periodically refresh the display, or {@code null} if refresh has been disabled.
     */
    private final PeriodicTask refresher;

    /**
     * The current practice location.
     */
    private Party location;

    /**
     * Determines if rostering is enabled for the location.
     */
    private boolean enableRostering;

    /**
     * Listener for practice location changes whilst the workspace is visible.
     */
    private ContextListener locationListener;

    /**
     * The minimum no. of seconds between refreshes.
     */
    private static final int MIN_REFRESH_INTERVAL = 5;

    /**
     * Constructs a {@link RosteringWorkspace}.
     *
     * @param context the context
     */
    public RosteringWorkspace(Context context) {
        super("workflow.rostering", context);
        service = ServiceHelper.getArchetypeService();
        location = context.getLocation();
        enableRostering = isRosteringEnabled(location);

        locationListener = (key, value) -> {
            if (Context.LOCATION_SHORTNAME.equals(key)) {
                locationChanged((Party) value);
            }
        };
        IMObjectBean bean = service.getBean(context.getPractice());
        int refreshInterval = bean.getInt("schedulingRefresh");
        if (refreshInterval > 0) {
            if (refreshInterval < MIN_REFRESH_INTERVAL) {
                // limit the no. of calls
                refreshInterval = MIN_REFRESH_INTERVAL;
            }
            refresher = new PeriodicTask(ApplicationInstance.getActive(), refreshInterval, this::refreshBrowser);
        } else {
            refresher = null;
        }
    }

    /**
     * Invoked when the workspace is displayed.
     */
    @Override
    public void show() {
        // listen for context change events
        ((GlobalContext) getContext()).addListener(locationListener);
        if (refresher != null) {
            refresher.start();
        }
    }

    /**
     * Invoked when the workspace is hidden.
     */
    @Override
    public void hide() {
        if (refresher != null) {
            refresher.stop();
        }
        ((GlobalContext) getContext()).removeListener(locationListener);
    }

    /**
     * Adds tabs to the tabbed pane.
     *
     * @param model the tabbed pane model
     */
    @Override
    protected void addTabs(ObjectTabPaneModel<TabComponent> model) {
        addAreaBrowser(model);
        addEmployeeBrowser(model);
    }

    /**
     * Returns the class type that this operates on.
     *
     * @return the class type that this operates on
     */
    @Override
    protected Class<IMObject> getType() {
        return IMObject.class;
    }

    /**
     * Queries the browser.
     * <p>
     * If automatic refreshes are being done, this restarts it.
     */
    protected void query() {
        if (refresher != null) {
            refresher.restart();
        }
    }

    /**
     * Determines if the workspace should be refreshed.
     * This implementation always returns {@code true}.
     *
     * @return {@code true}
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

    /**
     * Invoked when events are queried.
     * <p>
     * Should be overridden to update the global context.
     * <p>
     * This implementation refreshes the summary.
     */
    protected void onQuery() {
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Periodically invoked to refresh the visible browser.
     */
    private void refreshBrowser() {
        if (!DialogManager.isWindowDisplayed()) {
            // only refresh if no dialog is being displayed
            TabComponent component = getSelected();
            if (component instanceof BrowserTab) {
                ((BrowserTab) component).refresh();
            }
        }
    }

    /**
     * Adds a tab to display roster events by area.
     *
     * @param model the tab model
     */
    private void addAreaBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("area");
        if (enableRostering) {
            LayoutContext context = new DefaultLayoutContext(getContext(), help);
            AreaRosterBrowser browser = new AreaRosterBrowser(context);
            RosterCRUDWindow window = new AreaRosterCRUDWindow(browser, getContext(), help);
            addTab("workflow.rostering.areas", model, new BrowserTab(browser, window));
        } else {
            addRosteringDisabled("workflow.rostering.areas", model, help);
        }
    }

    /**
     * Adds a tab to display roster events by user.
     *
     * @param model the tab model
     */
    private void addEmployeeBrowser(ObjectTabPaneModel<TabComponent> model) {
        HelpContext help = subtopic("employee");
        if (enableRostering) {
            LayoutContext context = new DefaultLayoutContext(getContext(), help);
            UserRosterBrowser browser = new UserRosterBrowser(context);
            RosterCRUDWindow window = new UserRosterCRUDWindow(browser, getContext(), help);
            addTab("workflow.rostering.users", model, new BrowserTab(browser, window));
        } else {
            addRosteringDisabled("workflow.rostering.users", model, help);
        }
    }

    /**
     * Adds a tab indicating that rostering has been disabled.
     *
     * @param name  the tab name resource bundle key
     * @param model the tab model
     * @param help  the help context
     */
    private void addRosteringDisabled(String name, ObjectTabPaneModel<TabComponent> model, HelpContext help) {
        addTab(name, model, new TabComponent() {

            @Override
            public void show() {
            }

            @Override
            public Component getComponent() {
                String message;
                if (location != null) {
                    message = Messages.format("workflow.rostering.disabled", location.getName());
                } else {
                    message = Messages.get("workflow.rostering.selectlocation");
                }
                Label label = LabelFactory.text(message, Styles.BOLD);
                ColumnLayoutData layout = new ColumnLayoutData();
                layout.setAlignment(Alignment.ALIGN_CENTER);
                label.setLayoutData(layout);
                return ColumnFactory.create(Styles.LARGE_INSET, label);
            }

            @Override
            public HelpContext getHelpContext() {
                return help;
            }
        });
    }

    /**
     * Invoked when the practice location changes.
     *
     * @param newLocation the new location. May be {@code null}
     */
    private void locationChanged(Party newLocation) {
        location = newLocation;
        enableRostering = isRosteringEnabled(location);
        TabbedPane pane = getTabbedPane();
        int index = pane.getSelectedIndex();
        ObjectTabPaneModel<TabComponent> model = getModel();
        while (model.size() > 0) {
            model.removeTabAt(0);
        }
        addTabs(model);
        if (index < 0) {
            // force the pane to refresh
            pane.setSelectedIndex(-1);
            pane.setSelectedIndex(index);
        }
    }

    /**
     * Determines if rostering is enabled at the specified location.
     *
     * @param location the location. May be {@code null}
     * @return {@code true} if rostering is enabled
     */
    private boolean isRosteringEnabled(Party location) {
        boolean result = false;
        if (location != null) {
            IMObjectBean bean = service.getBean(location);
            result = bean.getBoolean("rostering");
        }
        return result;
    }

    private static class BrowserTab extends BrowserCRUDWindowTab<Act> {

        BrowserTab(RosterBrowser browser, RosterCRUDWindow window) {
            super(new RosterBrowserAdapter(browser), window, true, false);
            // suppress double click, as it is cell dependent
        }

        /**
         * Refresh the browser.
         */
        public void refresh() {
            RosterBrowserAdapter adapter = (RosterBrowserAdapter) getBrowser();
            ((RosterBrowser) adapter.getBrowser()).refresh();
        }

        /**
         * Returns the tab component.
         *
         * @return the tab component
         */
        @Override
        public Component getComponent() {
            return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                                           "SchedulingWorkspace.Layout", getWindow().getComponent(),
                                           getBrowser().getComponent());
        }

        /**
         * Creates a new listener to register on the browser.
         *
         * @return a new listener
         */
        @Override
        protected BrowserListener<Act> createListener() {
            return new RosterBrowserListener<Act>() {
                @Override
                public void query() {
                    onQuery();
                }

                @Override
                public void selected(Act object) {
                    onSelected(object);
                }

                @Override
                public void browsed(Act object) {
                    onBrowsed(object);
                }

                @Override
                public void create() {
                    getWindow().create();
                }

                @Override
                public void edit(Act event) {
                    CRUDWindow<Act> window = getWindow();
                    window.setObject(event);
                    window.edit();
                }
            };
        }
    }

    private static class RosterBrowserAdapter extends BrowserAdapter<PropertySet, Act> {

        /**
         * Constructs a {@link RosterBrowserAdapter}.
         *
         * @param browser the browser to adapt from
         */
        RosterBrowserAdapter(RosterBrowser browser) {
            super(browser);
        }

        /**
         * Adapts a listener.
         *
         * @param listener the listener to adapt
         * @return the adapted listener
         */
        @Override
        protected BrowserListener<PropertySet> adapt(BrowserListener<Act> listener) {
            if (listener instanceof RosterBrowserListener) {
                return new RosterBrowserListener<PropertySet>() {
                    public void query() {
                        listener.query();
                    }

                    public void selected(PropertySet object) {
                        listener.selected(convert(object));
                    }

                    public void browsed(PropertySet object) {
                        listener.browsed(convert(object));
                    }

                    /**
                     * Invoked to create and edit a new event.
                     */
                    @Override
                    public void create() {
                        ((RosterBrowserListener<Act>) listener).create();
                    }

                    @Override
                    public void edit(PropertySet event) {
                        Act act = convert(event);
                        if (act != null) {
                            ((RosterBrowserListener<Act>) listener).edit(act);
                        }
                    }
                };
            }
            return super.adapt(listener);
        }

        /**
         * Converts an object.
         *
         * @param object the object to convert
         * @return the converted object
         */
        @Override
        protected Act convert(PropertySet object) {
            return object != null ? ((RosterBrowser) getBrowser()).getAct(object) : null;
        }
    }
}
