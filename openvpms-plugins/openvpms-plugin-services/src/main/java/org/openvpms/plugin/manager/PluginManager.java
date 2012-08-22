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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.plugin.manager;

import com.atlassian.plugin.DefaultModuleDescriptorFactory;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.main.AtlassianPlugins;
import com.atlassian.plugin.main.PluginsConfiguration;
import com.atlassian.plugin.main.PluginsConfigurationBuilder;
import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class PluginManager implements InitializingBean {

    private final HostComponentProvider provider;
    private final PackageScannerConfiguration scanner;
    private final String pluginsDir;

    private int frequency = 2;

    public PluginManager(String pluginsDir, HostComponentProvider provider, PackageScannerConfiguration scanner) {
        this.pluginsDir = pluginsDir;
        this.provider = provider;
        this.scanner = scanner;
    }

    public void setPollingFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void start() throws Exception {
        // Determine which module descriptors, or extension points, to expose.
        // This 'on-start' module is used throughout this guide as an example only
        DefaultModuleDescriptorFactory modules = new DefaultModuleDescriptorFactory(new DefaultHostContainer());

        // Construct the configuration
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassLoader parent = (loader != null) ? loader : PluginManager.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(new ClassLoader(parent) {
            @Override
            public URL getResource(String name) {
                if (name.equals("osgi-framework-bundles.zip")) {
                    return super.getResource("openvpms-osgi-framework-bundles.zip");
                }
                return super.getResource(name);
            }
        });
        PluginsConfiguration config = new PluginsConfigurationBuilder()
                .pluginDirectory(new File(pluginsDir))
                .packageScannerConfiguration(scanner)
                .hotDeployPollingFrequency(frequency, TimeUnit.SECONDS)
                .hostComponentProvider(provider)
                .moduleDescriptorFactory(modules)
                .build();

        // Start the plugin framework
        AtlassianPlugins plugins = new AtlassianPlugins(config);
        Thread.currentThread().setContextClassLoader(loader);
        plugins.start();
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an essential property) or if
     *                   initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        start();
    }
}