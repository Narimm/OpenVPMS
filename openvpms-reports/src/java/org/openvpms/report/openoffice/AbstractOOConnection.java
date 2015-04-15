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

package org.openvpms.report.openoffice;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToConnect;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToGetService;


/**
 * Manages the connection to a remote OpenOffice service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractOOConnection
        implements OOConnection, com.sun.star.lang.XEventListener {

    /**
     * The connection parameters.
     * E.g, <code>socket,host=localhost,port=2002</code>
     */
    private final String connectParams;

    /**
     * The protocol parameters, parsed from the UNO URL. E.g, <code>urp</code>
     */
    private final String protocolParams;

    /**
     * The object name, parsed from the UNO URL.
     * E.g, <code>StarOffice.ServiceManager</code>
     */
    private final String objectName;

    /**
     * The bridge.
     */
    private XComponent bridge;

    /**
     * The component factory.
     */
    private XMultiComponentFactory componentFactory;

    /**
     * The component context.
     */
    private XComponentContext componentContext;

    /**
     * The connection listener.
     */
    private OOConnectionListener listener;


    /**
     * Constructs a new <code>AnstractOOConnection</code>, specifying the
     * connection paramaters, e.g <code>socket,host=localhost,port=8100</code>
     * for a socket connection, or <code>pipe,name=oopipe</code> for a named
     * pipe connection.
     *
     * @param parameters the connection parameters
     */
    public AbstractOOConnection(String parameters) {
        connectParams = parameters;
        protocolParams = "urp";
        objectName = "StarOffice.ServiceManager";
    }

    /**
     * Invoked when the remote bridge has gone down, because the office crashed
     * or was terminated.
     *
     * @param event the event
     */
    public synchronized void disposing(EventObject event) {
        componentFactory = null;
        componentContext = null;
    }

    /**
     * Returns the component loader.
     *
     * @return the component loader
     * @throws OpenOfficeException for any error
     */
    public synchronized XComponentLoader getComponentLoader() {
        return (XComponentLoader) getService("com.sun.star.frame.Desktop",
                                             XComponentLoader.class);
    }

    /**
     * Returns a service of the specified type.
     *
     * @param name the service name
     * @param type the service type
     * @throws OpenOfficeException if the service can't be created
     */
    public synchronized Object getService(String name, Class type) {
        if (componentFactory == null) {
            try {
                connect();
            } catch (Exception exception) {
                throw new OpenOfficeException(exception, FailedToConnect,
                                              connectParams);
            }
        }
        Object service;
        try {
            service = getService(name, type, componentFactory,
                                 componentContext);
        } catch (Exception exception) {
            throw new OpenOfficeException(exception, FailedToGetService, name);
        }
        if (service == null) {
            throw new OpenOfficeException(FailedToGetService, name);
        }
        return service;
    }

    /**
     * Sets the listener for this connection.
     *
     * @param listener the listener. May be <code>null</code>
     */
    public synchronized void setListener(OOConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * Closes the connection, releasing any resources.
     *
     * @throws OpenOfficeException for any error
     */
    public synchronized void close() {
        if (bridge != null) {
            doClose();
            if (listener != null) {
                listener.closed(this);
            }
        }
    }

    /**
     * Closes the connection, releasing any resources.
     */
    protected void doClose() {
        bridge.dispose();
        bridge = null;
    }

    /**
     * Returns a service of the specified type, using the specified context.
     *
     * @param name    the service name
     * @param type    the service type
     * @param factory the component factory
     * @param context the component context
     * @return the service
     * @throws com.sun.star.uno.Exception if the service can't be created
     */
    private Object getService(String name, Class type,
                              XMultiComponentFactory factory,
                              XComponentContext context)
            throws com.sun.star.uno.Exception {
        Object object = factory.createInstanceWithContext(name, context);
        return getInterface(type, object);
    }

    /**
     * Helper to return an interface for an object.
     *
     * @param type   the interface type
     * @param object the object
     * @return an interface for the object
     */
    private Object getInterface(Class<?> type, Object object) {
        return UnoRuntime.queryInterface(type, object);
    }

    /**
     * Connects to OpenOffice.
     *
     * @throws Exception for any error
     */
    private void connect() throws Exception {

        XComponentContext localContext
                = Bootstrap.createInitialComponentContext(null);
        XMultiComponentFactory localServiceManager
                = localContext.getServiceManager();

        // create the connector service
        XConnector connector = (XConnector) getService(
                "com.sun.star.connection.Connector", XConnector.class,
                localServiceManager, localContext);

        XConnection connection = connector.connect(connectParams);

        XBridgeFactory bridgeFactory = (XBridgeFactory) getService(
                "com.sun.star.bridge.BridgeFactory", XBridgeFactory.class,
                localServiceManager, localContext);

        // create a nameless bridge with no instance provider
        XBridge xbridge = bridgeFactory.createBridge("", protocolParams,
                                                     connection, null);

        // get the bridge's XComponent interface and add this as an event
        // listener
        bridge = (XComponent) getInterface(XComponent.class, xbridge);
        bridge.addEventListener(this);

        // get the remote instance
        Object object = xbridge.getInstance(objectName);
        if (object == null) {
            throw new com.sun.star.uno.Exception(
                    "Server didn't provide an instance for " + objectName,
                    null);
        }

        // query the initial object for its main factory interface
        componentFactory = (XMultiComponentFactory) getInterface(
                XMultiComponentFactory.class, object);

        // retrieve the component context (it's not yet exported from office)
        // Query for the XPropertySet interface.
        XPropertySet propertySet = (XPropertySet) getInterface(
                XPropertySet.class, componentFactory);

        // qet the default context from the office server
        Object defaultContext = propertySet.getPropertyValue("DefaultContext");

        // query for the XComponentContext interface
        componentContext = (XComponentContext) getInterface(
                XComponentContext.class, defaultContext);
    }

}
