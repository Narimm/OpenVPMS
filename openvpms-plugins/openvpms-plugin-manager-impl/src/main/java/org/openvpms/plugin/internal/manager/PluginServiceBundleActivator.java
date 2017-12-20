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

package org.openvpms.plugin.internal.manager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.servlet.ServletContext;
import java.util.List;


/**
 * A {@code BundleActivator} that registers services with the {@code BundleContext} on startup, using a
 * {@link PluginServiceProvider}.
 *
 * @author Tim Anderson
 */
class PluginServiceBundleActivator implements BundleActivator {

    /**
     * The plugin service provider.
     */
    private final PluginServiceProvider provider;

    /**
     * The servlet context, used to by the http bridge.
     */
    private final ServletContext servletContext;

    /**
     * The service registrations.
     */
    private List<ServiceRegistration<?>> registrations = null;


    /**
     * Constructs a {@link PluginServiceBundleActivator}.
     *
     * @param provider       the plugin service provider
     * @param servletContext the servlet context, used by the http bridge. May be {@code null}
     */
    public PluginServiceBundleActivator(PluginServiceProvider provider, ServletContext servletContext) {
        this.provider = provider;
        this.servletContext = servletContext;
    }

    /**
     * Provides services to the context.
     *
     * @param context the context
     */
    public void start(BundleContext context) {
        if (servletContext != null) {
            // pass the BundleContext to the servlet context. This is used by Felix HTTP Proxy
            servletContext.setAttribute(BundleContext.class.getName(), context);
        }
        registrations = provider.provide(context);
    }

    /**
     * Unregisters any provided service from the context.
     *
     * @param context the context
     */
    public void stop(BundleContext context) {
        for (ServiceRegistration<?> registration : registrations) {
            registration.unregister();
        }
        registrations = null;
    }
}
