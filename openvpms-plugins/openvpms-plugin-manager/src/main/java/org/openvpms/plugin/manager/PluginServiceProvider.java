package org.openvpms.plugin.manager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.List;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public interface PluginServiceProvider {

    /**
     * Provide services to the specified bundle context.
     *
     * @param context the context
     * @return the service registrations
     */
    List<ServiceRegistration<?>> provide(BundleContext context);
}