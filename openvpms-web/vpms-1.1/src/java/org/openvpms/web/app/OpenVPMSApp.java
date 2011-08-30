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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Command;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Window;
import nextapp.echo2.webcontainer.ContainerContext;
import nextapp.echo2.webcontainer.command.BrowserRedirectCommand;
import nextapp.echo2.webrender.ClientConfiguration;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.WebRenderServlet;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.resource.util.Styles;

import javax.servlet.http.HttpServletRequest;


/**
 * The entry point to the OpenVPMS application.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OpenVPMSApp extends ContextApplicationInstance {

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
     * Constructs a new <tt>OpenVPMSApp</tt>.
     */
    public OpenVPMSApp() {
        Context context = getContext();
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
        setStyleSheet(Styles.DEFAULT_STYLE_SHEET);
        window = new Window();
        updateTitle();
        window.setContent(new ApplicationContentPane());
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
     * Sets the content pane.
     *
     * @param content the content pane
     */
    public void setContent(ContentPane content) {
        window.setContent(content);
    }

    /**
     * Returns the content pane.
     *
     * @return the content pane
     */
    public ContentPane getContent() {
        return window.getContent();
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        getDefaultWindow().removeAll();
        clearContext();
        setContextChangeListener(null);
        Command redirect = new BrowserRedirectCommand("logout");
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
            StringBuffer uri = request.getRequestURL();
            String baseUri = uri.substring(0, uri.lastIndexOf("/"));
            String loginUri = baseUri + "/login";
            ClientConfiguration config = new ClientConfiguration();
            config.setProperty(
                    ClientConfiguration.PROPERTY_SESSION_EXPIRATION_URI,
                    loginUri);
            context.setClientConfiguration(config);
        }
    }

    /**
     * Updates the window title with the customer name.
     */
    private void updateTitle() {
        window.setTitle(Messages.get("app.title", location, customer));
    }

    /**
     * Returns the location name.
     *
     * @param location the location or <tt>null</tt>
     * @return the location name
     */
    private String getLocation(IMObject location) {
        return getName(location, "app.title.noLocation");
    }

    /**
     * Returns the location name.
     *
     * @param customer the customer or <tt>null</tt>
     * @return the customer name
     */
    private String getCustomer(IMObject customer) {
        return getName(customer, "app.title.noCustomer");
    }

    /**
     * Returns the name of an object, or a fallback string if the object is
     * <tt>null</tt>.
     *
     * @param object  the object. May be <tt>null</tt>
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
