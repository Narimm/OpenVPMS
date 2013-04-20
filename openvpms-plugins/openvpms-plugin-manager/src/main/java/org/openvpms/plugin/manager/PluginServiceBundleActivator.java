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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.manager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * Enter description.
 *
 * @author Tim Anderson
 */
class PluginServiceBundleActivator implements BundleActivator {

    private List<ServiceRegistration<?>> registrations = null;

    private final PluginServiceProvider provider;

    public PluginServiceBundleActivator(PluginServiceProvider provider) {
        this.provider = provider;
    }

    public void start(BundleContext context) {
        registrations = provider.provide(context);
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration<?> registration : registrations) {
            registration.unregister();
        }
        registrations = null;
    }
}
