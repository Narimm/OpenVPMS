package org.openvpms.plugin.manager;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.osgi.framework.BundleException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.List;

/**
 * A {@link PluginManager} that is configured by an <em>entity.pluginConfiguration</em>.
 *
 * @author Tim Anderson
 */
public class ConfigurablePluginManager implements PluginManager, InitializingBean, DisposableBean {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The plugin service provider;
     */
    private final PluginServiceProvider provider;

    /**
     * The plugin manager.
     */
    private PluginManager manager;

    /**
     * Constructs a {@link ConfigurablePluginManager}.
     *
     * @param service  the archetype service
     * @param provider the plugin service provider
     */
    public ConfigurablePluginManager(IArchetypeService service, PluginServiceProvider provider) {
        this.service = service;
        this.provider = provider;
    }

    /**
     * Returns the first service implementing the specified interface.
     *
     * @param type the interface
     * @return the first service implementing the interface, or {@code null} if none was found
     */
    @Override
    public <T> T getService(Class<T> type) {
        PluginManager manager = this.manager;
        return (manager != null) ? manager.getService(type) : null;
    }

    /**
     * Returns all services implementing the specified interface.
     *
     * @param type the interface
     * @return the services implementing the interface
     */
    @Override
    public <T> List<T> getServices(Class<T> type) {
        PluginManager manager = this.manager;
        return (manager != null) ? manager.getServices(type) : Collections.<T>emptyList();
    }

    /**
     * Determines if the plugin manager is started.
     *
     * @return {@code true} if the plugin manager is started
     */
    @Override
    public synchronized boolean isStarted() {
        return manager != null && manager.isStarted();
    }

    /**
     * Starts the plugin manager.
     *
     * @throws BundleException for any error
     */
    @Override
    public synchronized void start() throws BundleException {
        if (manager == null) {
            ArchetypeQuery query = new ArchetypeQuery("entity.pluginConfiguration", false, false);
            query.add(Constraints.sort("id"));
            query.setMaxResults(1);
            IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(service, query);
            Entity configuration = (iterator.hasNext()) ? iterator.next() : null;
            String path = null;
            if (configuration != null && configuration.isActive()) {
                IMObjectBean bean = new IMObjectBean(configuration, service);
                path = bean.getString("path");
            }
            if (path != null) {
                manager = new PluginManagerImpl(path, provider);
                manager.start();
            }
        }
    }

    /**
     * Stops the plugin manager.
     * <p>
     * This method will wait until the manager shuts down.
     *
     * @throws BundleException      for any error
     * @throws InterruptedException if the thread was interrupted
     */
    @Override
    public synchronized void stop() throws BundleException, InterruptedException {
        if (manager != null) {
            manager.stop();
            manager = null;
        }
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception if initialization fails
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        stop();
    }

}
