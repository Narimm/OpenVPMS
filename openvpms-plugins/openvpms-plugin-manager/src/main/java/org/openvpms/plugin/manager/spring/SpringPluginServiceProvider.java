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
package org.openvpms.plugin.manager.spring;

import org.openvpms.plugin.manager.PluginServiceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.BeanFactory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Provides host services to plugins.
 *
 * @author Tim Anderson
 */
public class SpringPluginServiceProvider implements PluginServiceProvider {

    /**
     * The bean factory to retrieve beans from.
     */
    private final BeanFactory factory;

    /**
     * The names of beans to provide to plugins.
     */
    private final Set<String> beanNames;

    /**
     * Constructs a {@link SpringPluginServiceProvider}.
     *
     * @param factory   the bean factory to retrieve beans from
     * @param beanNames the names of the services to provide to plugins
     */
    public SpringPluginServiceProvider(BeanFactory factory, Set<String> beanNames) {
        this.factory = factory;
        this.beanNames = beanNames;

    }

    /**
     * Provide services to the specified bundle context.
     *
     * @param context the context
     * @return the service registrations
     */
    public List<ServiceRegistration<?>> provide(BundleContext context) {
        List<ServiceRegistration<?>> result = new ArrayList<ServiceRegistration<?>>();
        for (String name : beanNames) {
            if (factory.isSingleton(name)) {
                Object bean = factory.getBean(name);
                Class<?>[] interfaces = bean.getClass().getInterfaces();
                if (interfaces.length != 0) {
                    String[] names = new String[interfaces.length];
                    for (int i = 0; i < interfaces.length; ++i) {
                        names[i] = interfaces[i].getName();
                    }
                    Hashtable<String, Object> properties = new Hashtable<>();
                    ServiceRegistration<?> registration = context.registerService(names, bean, properties);
                    result.add(registration);
                }
            }
        }
        return result;
    }

}