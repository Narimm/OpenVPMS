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

package org.openvpms.web.workspace;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Command;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.app.Window;
import nextapp.echo2.webcontainer.ContainerContext;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import nextapp.echo2.webcontainer.command.BrowserRedirectCommand;
import nextapp.echo2.webrender.ClientConfiguration;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.subscription.core.Subscription;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.app.PreferenceSelectionHistory;
import org.openvpms.web.component.prefs.UserPreferences;
import org.openvpms.web.component.subscription.SubscriptionHelper;
import org.openvpms.web.component.workspace.WorkspacesFactory;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.lightbox.LightBox;
import org.openvpms.web.echo.servlet.ServletHelper;
import org.openvpms.web.echo.servlet.SessionMonitor;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;


/**
 * The entry point to the OpenVPMS application.
 *
 * @author Tim Anderson
 */
public class OpenVPMSApp extends ContextApplicationInstance {

    /**
     * The workspaces factory.
     */
    private final WorkspacesFactory factory;

    /**
     * The window.
     */
    private Window window;

    /**
     * Context change listener.
     */
    private ContextChangeListener listener;

    /**
     * The current location.
     */
    private String location;

    /**
     * The current customer.
     */
    private String customer;

    /**
     * Light box used to darken the screen when displaying the lock screen dialog.
     */
    private LightBox lightBox;

    /**
     * The session monitor.
     */
    private final SessionMonitor monitor;

    /**
     * The lock dialog.
     */
    private PopupDialog lockDialog;

    /**
     * The lock task queue handle.
     */
    private TaskQueueHandle lockHandle;

    /**
     * The default interval, in seconds that the client will poll the server to detect screen auto-lock updates.
     * This should not be set too small as that increases server load.
     */
    private static final int DEFAULT_LOCK_POLL_INTERVAL = 30;


    /**
     * Constructs an {@link OpenVPMSApp}.
     *
     * @param context       the context
     * @param factory       the workspaces factory
     * @param practiceRules the practice rules
     * @param locationRules the location rules
     * @param userRules     the user rules
     * @param monitor       the session monitor
     * @param preferences   the user preferences
     */
    public OpenVPMSApp(GlobalContext context, WorkspacesFactory factory, PracticeRules practiceRules,
                       LocationRules locationRules, UserRules userRules, SessionMonitor monitor,
                       UserPreferences preferences) {
        super(context, practiceRules, locationRules, userRules, preferences);
        this.factory = factory;
        this.monitor = monitor;
        location = getLocation(context.getLocation());
        customer = getCustomer(context.getCustomer());
        if (monitor.getAutoLock() > 0) {
            getLockTaskQueue(DEFAULT_LOCK_POLL_INTERVAL);  // configure a queue to trigger polls of the server
        }
        loadHistory();
    }

    /**
     * Invoked to initialize the application, returning the default window.
     *
     * @return the default window of the application
     */
    public Window init() {
        configureSessionExpirationURL();
        setStyleSheet();
        window = new Window();
        updateTitle();
        ApplicationContentPane pane = new ApplicationContentPane(getContext(), factory, getPreferences());
        lightBox = new LightBox();
        window.setContent(pane);
        pane.add(lightBox);

        if (getActiveWindowCount() <= 1) {
            Subscription subscription = SubscriptionHelper.getSubscription(ServiceHelper.getArchetypeService());
            final Date now = new Date();
            Date expiryDate = subscription != null ? subscription.getExpiryDate() : null;
            if (expiryDate == null || expiryDate.before(now)) {
                Party practice = getContext().getPractice();
                if (practice != null) {
                    Date installDate = (Date) practice.getDetails().get("installDate");
                    if (installDate == null || installDate.after(now)) {
                        installDate = now;
                        practice.getDetails().put("installDate", installDate);
                        try {
                            ServiceHelper.getArchetypeService(false).save(practice);
                        } catch (Throwable exception) {
                            //  do nothing
                        }
                    }
                    SubscriptionDialog dialog = new SubscriptionDialog(installDate, expiryDate, lightBox);
                    dialog.show(window, this);
                }
            }
        }

        getContext().addListener(new ContextListener() {
            public void changed(String key, IMObject value) {
                if (Context.CUSTOMER_SHORTNAME.equals(key)) {
                    customer = getCustomer(value);
                    updateTitle();
                } else if (Context.LOCATION_SHORTNAME.equals(key)) {
                    location = getLocation(value);
                    updateTitle();
                }
            }
        });
        return window;
    }

    /**
     * Invoked when the application is disposed and will not be used again.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (window != null) {
            window.dispose();
        }
    }

    /**
     * Returns the instance associated with the current thread.
     *
     * @return the current instance, or {@code null}
     */
    public static OpenVPMSApp getInstance() {
        return (OpenVPMSApp) ApplicationInstance.getActive();
    }

    /**
     * Creates a new browser window.
     */
    public void createWindow() {
        createWindow(-1, -1);
    }

    /**
     * Creates a new browser window.
     *
     * @param width  the window width. If {@code -1} the default width will be used
     * @param height the window height. If {@code -1} the default height will be used
     */
    public void createWindow(int width, int height) {
        StringBuilder uri = new StringBuilder(ServletHelper.getRedirectURI("app"));
        StringBuilder features = new StringBuilder("menubar=yes,toolbar=yes,location=yes");
        if (width != -1 && height != -1) {
            uri.append("?width=");
            uri.append(width);
            uri.append("&height=");
            uri.append(height);
            features.append(",width=");
            features.append(width);
            features.append(",height=");
            features.append(height);
        }
        Command open = new BrowserOpenWindowCommand(uri.toString(), "_blank", features.toString());
        enqueueCommand(open);
    }

    /**
     * Determines the no. of browser windows/tabs currently active.
     *
     * @return the no. of browser windows/tabs currently active
     */
    public int getActiveWindowCount() {
        return ServletHelper.getApplicationInstanceCount("app");
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        getDefaultWindow().removeAll();
        clearContext();
        setContextChangeListener(null);
        Command redirect = new BrowserRedirectCommand(ServletHelper.getRedirectURI("logout"));
        enqueueCommand(redirect);
    }

    /**
     * Switches the current workspace to display an object.
     *
     * @param object the object to view
     */
    public void switchTo(IMObject object) {
        if (listener != null) {
            listener.changeContext(object);
        }
    }

    /**
     * Switches the current workspace to one that supports a particular archetype.
     *
     * @param shortName the archetype short name
     */
    public void switchTo(String shortName) {
        if (listener != null) {
            listener.changeContext(shortName);
        }
    }

    /**
     * Locks the application, until the user re-enters their password.
     * <p/>
     * This method may be invoked outside a servlet request, so a task is queued to lock the screen.
     * This task is invoked when the client synchronizes with the server, at most DEFAULT_LOCK_QUEUE seconds after
     * lock() is invoked.
     */
    @Override
    public synchronized void lock() {
        if (lockDialog == null) {
            setLockDialog(createLockDialog());
            // enqueue the task, ideally within 1 second, although in practice this may be up to DEFAULT_LOCK_QUEUE
            // seconds.
            enqueueTask(getLockTaskQueue(1), new Runnable() {
                @Override
                public void run() {
                    lockScreen();
                }
            });
        }
    }

    /**
     * Unlocks the application.
     * <p/>
     * This method may be invoked outside a servlet request, so a task is queued to unlock the screen.
     */
    @Override
    public synchronized void unlock() {
        if (lockDialog != null) {
            if (lockDialog.getParent() != null) {
                enqueueTask(getLockTaskQueue(1), new Runnable() {
                    @Override
                    public void run() {
                        unlockScreen();
                    }
                });
            } else {
                // the dialog hasn't been shown yet, so just destroy it.
                lockDialog.dispose();
                lockDialog = null;
            }
        }
    }

    /**
     * Sets the context change listener.
     *
     * @param listener the context change listener
     */
    protected void setContextChangeListener(ContextChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Loads customer and patient selection history.
     */
    private void loadHistory() {
        GlobalContext context = getContext();
        UserPreferences prefs = getPreferences();
        PreferenceSelectionHistory customer = new PreferenceSelectionHistory(context, GlobalContext.CUSTOMER_SHORTNAME, prefs,
                                                                             PreferenceArchetypes.GENERAL,
                                                                             "customerHistory");
        PreferenceSelectionHistory patient = new PreferenceSelectionHistory(context, GlobalContext.PATIENT_SHORTNAME, prefs,
                                                                            PreferenceArchetypes.GENERAL,
                                                                            "patientHistory");
        context.setHistory(customer.getShortName(), customer);
        context.setHistory(patient.getShortName(), patient);
    }

    /**
     * Creates a lock screen dialog.
     *
     * @return a new dialog
     */
    private PopupDialog createLockDialog() {
        return new LockScreenDialog(this) {
            private boolean showLightBox = !lightBox.isVisible();

            @Override
            public void show() {
                super.show();
                if (showLightBox) {
                    lightBox.setZIndex(getZIndex());
                    lightBox.show();
                }
            }

            @Override
            public void userClose() {
                if (showLightBox) {
                    lightBox.hide();
                }
                super.userClose();
                resetLockTaskQueue(); // ensure the queue is set back to the default poll interval, to reduce load
            }
        };
    }

    /**
     * Locks the screen.
     */
    private void lockScreen() {
        PopupDialog dialog = getLockDialog();
        if (dialog != null) {
            monitor.locked();
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    setLockDialog(null);
                    monitor.unlock();
                }
            });
            dialog.show();

            // If there are no other active windows, set the poll interval back to the default.
            // If there are multiple windows active, leave the task queue active so that if unlock happens in another
            // window it is detected in a timely fashion
            if (getActiveWindowCount() <= 1) {
                resetLockTaskQueue();
            }
        } else {
            resetLockTaskQueue();
        }
    }

    /**
     * Unlocks the screen.
     */
    private void unlockScreen() {
        try {
            PopupDialog dialog = getLockDialog();
            if (dialog != null) {
                setLockDialog(null);
                dialog.close();
            }
        } finally {
            resetLockTaskQueue();
        }
    }

    /**
     * Returns the lock dialog.
     *
     * @return the lock dialog, or {@code null} if it hasn't been created
     */
    private synchronized PopupDialog getLockDialog() {
        return lockDialog;
    }

    /**
     * Register the lock dialog.
     *
     * @param dialog the dialog
     */
    private synchronized void setLockDialog(PopupDialog dialog) {
        this.lockDialog = dialog;
    }

    /**
     * Returns the lock task queue.
     *
     * @param interval the client poll interval, in seconds
     * @return the task queue
     */
    private synchronized TaskQueueHandle getLockTaskQueue(int interval) {
        if (lockHandle == null) {
            lockHandle = createTaskQueue();
        }
        setTaskQueueInterval(lockHandle, interval);
        return lockHandle;
    }

    /**
     * Helper to set the interval for a task queue.
     *
     * @param handle   the task queue handle
     * @param interval the interval, in seconds
     */
    private void setTaskQueueInterval(TaskQueueHandle handle, int interval) {
        ContainerContext context = (ContainerContext) getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
        if (context != null) {
            context.setTaskQueueCallbackInterval(handle, interval * 1000);
        }
    }

    /**
     * Sets the lock task queue poll interval back to the default.
     * This reduces server load.
     */
    private synchronized void resetLockTaskQueue() {
        if (lockHandle != null) {
            setTaskQueueInterval(lockHandle, DEFAULT_LOCK_POLL_INTERVAL); //
        }
    }

    /**
     * Configures the client to redirect to the login page when the session
     * expires.
     */
    private void configureSessionExpirationURL() {
        ContainerContext context = (ContainerContext) getContextProperty(
                ContainerContext.CONTEXT_PROPERTY_NAME);
        if (context != null) {
            String url = ServletHelper.getServerURL() + ServletHelper.getRedirectURI("login");
            ClientConfiguration config = new ClientConfiguration();
            config.setProperty(ClientConfiguration.PROPERTY_SESSION_EXPIRATION_URI, url);
            config.setProperty(ClientConfiguration.PROPERTY_SESSION_EXPIRATION_MESSAGE,
                               Messages.get("session.expired"));
            context.setClientConfiguration(config);
        }
    }

    /**
     * Updates the window title with the customer name.
     */
    private void updateTitle() {
        window.setTitle(Messages.format("app.title", location, customer));
    }

    /**
     * Returns the location name.
     *
     * @param location the location or {@code null}
     * @return the location name
     */
    private String getLocation(IMObject location) {
        return getName(location, "app.title.noLocation");
    }

    /**
     * Returns the location name.
     *
     * @param customer the customer or {@code null}
     * @return the customer name
     */
    private String getCustomer(IMObject customer) {
        return getName(customer, "app.title.noCustomer");
    }

    /**
     * Returns the name of an object, or a fallback string if the object is
     * {@code null}.
     *
     * @param object  the object. May be {@code null}
     * @param nullKey the message key if the object is null
     * @return the name of the object
     */
    private String getName(IMObject object, String nullKey) {
        if (object == null) {
            return Messages.get(nullKey);
        }
        return object.getName();
    }


    private static class SubscriptionDialog extends PopupDialog {

        private final Date installDate;
        private final Date expiryDate;
        private final LightBox lightBox;

        private static final String SUBSCRIBE_ID = "button.subscribe";
        private static final String NOT_NOW_ID = "button.notnow";
        private static final String[] BUTTONS = {SUBSCRIBE_ID, NOT_NOW_ID};

        /**
         * Constructs a {@link SubscriptionDialog}.
         */
        public SubscriptionDialog(Date installDate, Date expiryDate, LightBox lightBox) {
            super(Messages.get("subscription.title"), "MessageDialog", BUTTONS);
            setModal(true);
            this.installDate = installDate;
            this.expiryDate = expiryDate;
            this.lightBox = lightBox;
        }

        /**
         * Invoked when a button is pressed. This delegates to the appropriate
         * on*() method for the button if it is known, else sets the action to
         * the button identifier and closes the window.
         *
         * @param button the button identifier
         */
        @Override
        protected void onButton(String button) {
            if (SUBSCRIBE_ID.equals(button)) {
                String url = Messages.get("subscription.url");
                ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(url, null, null));
            }
            super.onButton(button);
        }

        public void show(Window window, final OpenVPMSApp app) {
            doLayout();
            setZIndex(1);
            window.getContent().add(this);
            Date date = DateRules.getDate(new Date(), -30, DateUnits.DAYS);
            if ((expiryDate != null && expiryDate.before(date)) || (expiryDate == null && installDate.before(date))) {
                getButtons().setEnabled(NOT_NOW_ID, false);
                setClosable(false);
                final TaskQueueHandle handle = app.createTaskQueue();
                app.setTaskQueueInterval(handle, 30);
                app.enqueueTask(handle, new Runnable() {
                    @Override
                    public void run() {
                        app.removeTaskQueue(handle);
                        getButtons().setEnabled(NOT_NOW_ID, true);
                        setClosable(true);
                    }
                });
            }
            lightBox.setZIndex(getZIndex());
            lightBox.show();
        }

        @Override
        public void userClose() {
            lightBox.hide();
            super.userClose();
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label label = LabelFactory.create(true, true);
            label.setStyleName(Styles.H4);
            Label message = LabelFactory.create(true, true);

            if (expiryDate == null) {
                label.setText(Messages.get("subscription.nosubscription"));
                int more = 30 - Days.daysBetween(new DateTime(installDate), new DateTime()).getDays();
                if (more > 0) {
                    message.setText(Messages.format("subscription.evaluate", more));
                } else {
                    message.setText(Messages.get("subscription.purchase"));
                }
            } else {
                label.setText(Messages.format("subscription.expiredOn", DateFormatter.formatDate(expiryDate, false)));
                message.setText(Messages.get("subscription.continue"));
            }
            Row row = RowFactory.create(Styles.LARGE_INSET,
                                        ColumnFactory.create(Styles.WIDE_CELL_SPACING, label, message));
            getLayout().add(row);
        }
    }
}