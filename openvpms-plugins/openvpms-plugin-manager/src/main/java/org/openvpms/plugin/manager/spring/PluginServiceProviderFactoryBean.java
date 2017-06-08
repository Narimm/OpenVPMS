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
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.Set;

/**
 * A {@code FactoryBean} for creating {@link SpringPluginServiceProvider} instances.
 *
 * @author Tim Anderson
 */
public class PluginServiceProviderFactoryBean extends AbstractFactoryBean {

    /**
     * The bean names.
     */
    private Set<String> beanNames = null;

    /**
     * Sets the service names to expose to plugins.
     *
     * @param beanNames the bean names of the services
     */
    public void setServices(Set<String> beanNames) {
        this.beanNames = beanNames;
    }

    /**
     * Return the type of object that this FactoryBean creates.
     *
     * @return {@code PluginServiceProvider.class}
     */
    @Override
    public Class<?> getObjectType() {
        return PluginServiceProvider.class;
    }

    /**
     * Creates an instance of {@link SpringPluginServiceProvider}.
     *
     * @return the object returned by this factory
     * @throws Exception if an exception occurred during object creation
     */
    @Override
    protected Object createInstance() throws Exception {
        PluginServiceProviderConfig config = PluginServiceProviderConfig.getConfig(getBeanFactory());
        if (config != null) {
            if (beanNames == null) {
                beanNames = config.getServices();
            } else {
                beanNames.addAll(config.getServices());
            }
        }
        return new SpringPluginServiceProvider(getBeanFactory(), beanNames);
    }


}
