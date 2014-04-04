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
import nextapp.echo2.app.Window;
import nextapp.echo2.webcontainer.ContainerContext;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import nextapp.echo2.webcontainer.command.BrowserRedirectCommand;
import nextapp.echo2.webrender.ClientConfiguration;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.WebRenderServlet;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.workspace.WorkspacesFactory;
import org.openvpms.web.echo.servlet.ServletHelper;
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
     * Constructs an {@link OpenVPMSApp}.
     *
     * @param context       the context
     * @param factory       the workspaces factory
     * @param practiceRules the practice rules
     * @param locationRules the location rules
     */
    public OpenVPMSApp(GlobalContext context, WorkspacesFactory factory, PracticeRules practiceRules,
                       LocationRules locationRules) {
        super(context, practiceRules, locationRules);
        this.factory = factory;
        location = getLocation(context.getLocation());
        customer = getCustomer(context.getCustomer());
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
        window.setContent(new ApplicationContentPane(getContext(), factory));
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
     * Sets the context change listener.
     *
     * @param listener the context change listener
     */
    protected void setContextChangeListener(ContextChangeListener listener) {
        this.listener = listener;
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
