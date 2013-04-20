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
