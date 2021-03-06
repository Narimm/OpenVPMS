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

package org.openvpms.web.workspace.workflow.scheduling;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.prefs.PreferenceMonitor;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workspace.AbstractViewWorkspace;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.component.workspace.CRUDWindowListener;
import org.openvpms.web.echo.dialog.DialogManager;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.util.PeriodicTask;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;
import org.openvpms.web.workspace.patient.summary.CustomerPatientSummaryFactory;

import java.util.Date;


/**
 * Scheduling workspace.
 *
 * @author Tim Anderson
 */
public abstract class SchedulingWorkspace extends AbstractViewWorkspace<Entity> {

    /**
     * The user preferences.
     */
    private final Preferences preferences;

    /**
     * The scheduling preference group.
     */
    private final String preferenceGroup;

    /**
     * Monitors changes to scheduling preferences.
     */
    private final PreferenceMonitor monitor;

    /**
     * Used to periodically refresh the display, or {@code null} if refresh has been disabled.
     */
    private final PeriodicTask refresher;

    /**
     * The workspace.
     */
    private Component workspace;

    /**
     * The schedule event browser.
     */
    private ScheduleBrowser browser;

    /**
     * The CRUD window.
     */
    private ScheduleCRUDWindow window;

    /**
     * The current practice location.
     */
    private Party location;

    /**
     * Listener for practice location changes whilst the workspace is visible.
     */
    private ContextListener locationListener;

    /**
     * The minimum no. of seconds between refreshes.
     */
    private static final int MIN_REFRESH_INTERVAL = 5;

    /**
     * Constructs a {@code SchedulingWorkspace}.
     * <p>
     * If no archetypes are supplied, the {@link #setArchetypes} method must before performing any operations.
     *
     * @param id              the workspace identifier
     * @param archetypes      the archetype that this operates on. May be {@code null}
     * @param context         the context
     * @param preferences     user preferences
     * @param preferenceGroup the preference group to monitor for changes
     */
    public SchedulingWorkspace(String id, Archetypes<Entity> archetypes, Context context, Preferences preferences,
                               String preferenceGroup) {
        super(id, archetypes, context, false);
        locationListener = (key, value) -> {
            if (Context.LOCATION_SHORTNAME.equals(key)) {
                locationChanged((Party) value);
            }
        };
        this.preferences = preferences;
        this.preferenceGroup = preferenceGroup;
        monitor = new PreferenceMonitor(preferences);
        monitor.add(preferenceGroup);
        IMObjectBean bean = ServiceHelper.getArchetypeService().getBean(context.getPractice());
        int refreshInterval = bean.getInt("schedulingRefresh");
        if (refreshInterval > 0) {
            if (refreshInterval < MIN_REFRESH_INTERVAL) {
                // limit the no. of calls
                refreshInterval = MIN_REFRESH_INTERVAL;
            }
            refresher = new PeriodicTask(ApplicationInstance.getActive(), refreshInterval, () -> {
                if (browser != null && !DialogManager.isWindowDisplayed()) {
                    browser.refresh();
                }
            });
        } else {
            refresher = null;
        }
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Entity object) {
        setScheduleView(object, new Date());
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or {@code null} if there is no summary
     */
    @Override
    public Component getSummary() {
        if (window != null) {
            Act act = window.getObject();
            CustomerPatientSummaryFactory factory = ServiceHelper.getBean(CustomerPatientSummaryFactory.class);
            CustomerPatientSummary summary = factory.createCustomerPatientSummary(getContext(), getHelpContext(),
                                                                                  preferences);
            return summary.getSummary(act);
        }
        return null;
    }

    /**
     * Invoked when the workspace is displayed.
     */
    @Override
    public void show() {
        // listen for context change events
        ((GlobalContext) getContext()).addListener(locationListener);
        checkPreferences();
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
     * Invoked when user preferences have changed.
     * <p>
     * This updates the workspace if scheduling preferences have changed.
     */
    @Override
    public void preferencesChanged() {
        checkPreferences();
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
        browser.query();
    }

    /**
     * Returns the preferences.
     *
     * @return the preferences
     */
    protected Preferences getPreferences() {
        return preferences;
    }

    /**
     * Checks preferences, refreshing the view if they have updated.
     */
    protected void checkPreferences() {
        if (monitor.changed()) {
            Entity object = getObject();
            IMObjectReference defaultValue = object != null ? object.getObjectReference() : null;
            IMObjectReference viewRef = preferences.getReference(preferenceGroup, "view", defaultValue);
            Party location = getContext().getLocation();
            if (location != null && viewRef != null) {
                LocationRules rules = ServiceHelper.getBean(LocationRules.class);
                Entity view = IMObjectHelper.getObject(viewRef, rules.getScheduleViews(location));
                if (view != null) {
                    object = view;
                }
            }
            setObject(object);
        }
    }

    /**
     * Sets the schedule view and date.
     *
     * @param view the schedule view
     * @param date the date to view
     */
    protected void setScheduleView(Entity view, Date date) {
        location = getContext().getLocation();
        super.setObject(view);
        layoutWorkspace();
        initQuery(view, date);
    }

    /**
     * Creates a new browser.
     *
     * @return a new browser
     */
    protected abstract ScheduleBrowser createBrowser();

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected abstract ScheduleCRUDWindow createCRUDWindow();

    /**
     * Returns the default schedule view for the specified practice location.
     *
     * @param location    the practice location
     * @param preferences the user preferences
     * @return the default schedule view, or {@code null} if there is no default
     */
    protected abstract Entity getDefaultView(Party location, Preferences preferences);

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
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
        query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(IMObject object) {
        query();
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked when an event is selected.
     *
     * @param event the event. May be {@code null}
     */
    protected void eventSelected(PropertySet event) {
        Act act = browser.getAct(event);
        window.setObject(act);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Invoked to edit an event.
     *
     * @param event the event
     */
    protected void onEdit(PropertySet event) {
        Act act = browser.getAct(event);
        window.setObject(act);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
        if (act != null) {
            window.edit();
        }
    }

    /**
     * Lays out the workspace.
     */
    protected void layoutWorkspace() {
        setBrowser(createBrowser());
        setCRUDWindow(createCRUDWindow());
        setWorkspace(createWorkspace());
    }

    /**
     * Creates the workspace split pane.
     *
     * @return a new workspace split pane
     */
    protected Component createWorkspace() {
        Component acts = browser.getComponent();
        Component window = getCRUDWindow().getComponent();
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "SchedulingWorkspace.Layout", window, acts);
    }

    /**
     * Registers a new browser.
     *
     * @param browser the new browser
     */
    protected void setBrowser(ScheduleBrowser browser) {
        this.browser = browser;
        browser.addScheduleBrowserListener(new ScheduleBrowserListener() {
            public void query() {
                onQuery();
            }

            public void selected(PropertySet object) {
                eventSelected(object);
            }

            public void browsed(PropertySet object) {
                eventSelected(object);
            }

            public void edit(PropertySet set) {
                onEdit(set);
            }

            public void create() {
                window.create();
            }
        });
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    protected ScheduleBrowser getBrowser() {
        return browser;
    }

    /**
     * Registers a new workspace.
     *
     * @param workspace the workspace
     */
    protected void setWorkspace(Component workspace) {
        SplitPane root = getRootComponent();
        if (this.workspace != null) {
            root.remove(this.workspace);
        }
        this.workspace = workspace;
        root.add(this.workspace);
    }

    /**
     * Registers a new CRUD window.
     *
     * @param window the window
     */
    protected void setCRUDWindow(ScheduleCRUDWindow window) {
        this.window = window;
        this.window.setListener(new CRUDWindowListener<Act>() {
            public void saved(Act object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(Act object) {
                onDeleted(object);
            }

            public void refresh(Act object) {
                onRefresh(object);
            }
        });
    }

    /**
     * Returns the CRUD window.
     *
     * @return the CRUD window
     */
    protected CRUDWindow<Act> getCRUDWindow() {
        return window;
    }

    /**
     * Perform an initial query, selecting the first available act.
     *
     * @param view the party
     * @param date the date to query
     */
    protected void initQuery(Entity view, Date date) {
        browser.setScheduleView(view);
        browser.setDate(date);
        browser.query();
        onQuery();
    }

    /**
     * Returns the workspace.
     *
     * @return the workspace. May be {@code null}
     */
    protected Component getWorkspace() {
        return workspace;
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
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Entity latest = getLatest();
        if (browser == null || !ObjectUtils.equals(location, getContext().getLocation())) {
            layoutWorkspace();
            latest = (Entity) browser.getScheduleView();
            setObject(latest);
        } else if (latest != getObject()) {
            setObject(latest);
        } else {
            browser.query();

            // need to add the existing workspace to the container
            Component workspace = getWorkspace();
            if (workspace != null) {
                container.add(workspace);
            }
        }
    }

    /**
     * Invoked when the practice location changes. Updates the schedule view.
     *
     * @param newLocation the new location. May be {@code null}
     */
    private void locationChanged(Party newLocation) {
        if (newLocation == null) {
            setObject(null);
        } else if (!ObjectUtils.equals(location, newLocation)) {
            setObject(getDefaultView(newLocation, preferences));
        }
    }

}
