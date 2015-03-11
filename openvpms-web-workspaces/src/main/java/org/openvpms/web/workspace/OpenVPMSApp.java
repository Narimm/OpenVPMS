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

package org.openvpms.web.workspace;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Command;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.app.Window;
import nextapp.echo2.webcontainer.ContainerContext;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import nextapp.echo2.webcontainer.command.BrowserRedirectCommand;
import nextapp.echo2.webrender.ClientConfiguration;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.WebRenderServlet;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.workspace.WorkspacesFactory;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.lightbox.LightBox;
import org.openvpms.web.echo.servlet.ServletHelper;
import org.openvpms.web.echo.servlet.SessionMonitor;
import org.openvpms.web.resource.i18n.Messages;

import javax.servlet.http.HttpServletRequest;


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
     */
    public OpenVPMSApp(GlobalContext context, WorkspacesFactory factory, PracticeRules practiceRules,
                       LocationRules locationRules, UserRules userRules, SessionMonitor monitor) {
        super(context, practiceRules, locationRules, userRules);
        this.factory = factory;
        this.monitor = monitor;
        location = getLocation(context.getLocation());
        customer = getCustomer(context.getCustomer());
        if (monitor.getAutoLock() > 0) {
            getLockTaskQueue(DEFAULT_LOCK_POLL_INTERVAL);  // configure a queue to trigger polls of the server
        }
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
        ApplicationContentPane pane = new ApplicationContentPane(getContext(), factory);
        lightBox = new LightBox();
        window.setContent(pane);
        pane.add(lightBox);
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
     * Returns the instance associated with the current thread.
     *
     * @return the current instance, or <code>null</code>
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
     * <p/>
     * This method may be invoked outside a servlet request, so a task is queued to unlock the screen.
     */
    @Override
    public synchronized void unlock() {
        if (lockDialog != null) {
            enqueueTask(getLockTaskQueue(1), new Runnable() {
                @Override
                public void run() {
                    unlockScreen();
                }
            });
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
     * Creates a lock screen dialog.
     *
     * @return a new dialog
     */
    private PopupDialog createLockDialog() {
        return new LockScreenDialog(this) {
            @Override
            public void show() {
                super.show();
                lightBox.setZIndex(getZIndex());
                lightBox.show();
            }

            @Override
            public void userClose() {
                lightBox.hide();
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
        Connection connection = WebRenderServlet.getActiveConnection();
        if (context != null && connection != null) {
            HttpServletRequest request = connection.getRequest();
            String url = request.getRequestURL().toString();
            url = url.substring(0, url.length() - request.getRequestURI().length());
            url = url + ServletHelper.getRedirectURI("login");
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

}
