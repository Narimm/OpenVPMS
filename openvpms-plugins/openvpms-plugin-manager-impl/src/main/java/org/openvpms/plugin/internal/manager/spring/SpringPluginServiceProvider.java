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

package org.openvpms.plugin.internal.manager.spring;


import org.apache.commons.lang.ClassUtils;
import org.openvpms.plugin.internal.manager.PluginServiceProvider;
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
        List<ServiceRegistration<?>> result = new ArrayList<>();
        for (String name : beanNames) {
            if (factory.isSingleton(name)) {
                Object bean = factory.getBean(name);
                // TODO - provide a way of specifying which interfaces to use
                List interfaces = ClassUtils.getAllInterfaces(bean.getClass());
                if (!interfaces.isEmpty()) {
                    String[] names = new String[interfaces.size()];
                    for (int i = 0; i < interfaces.size(); ++i) {
                        names[i] = ((Class) interfaces.get(i)).getName();
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