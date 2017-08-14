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

import org.springframework.beans.factory.BeanFactory;

import java.util.Set;

/**
 * Collects plugin from a Spring context as its being parsed.
 *
 * @author Tim Anderson
 */
public class PluginServiceProviderConfig {

    /**
     * The plugin service provider configuration bean name.
     */
    public static final String BEAN_NAME = "_pluginServiceProviderConfig";


    private Set<String> beanNames;

    public void setServices(Set<String> beanNames) {
        this.beanNames = beanNames;
    }

    public Set<String> getServices() {
        return beanNames;
    }

    /**
     * Helper to return an instance of {@link PluginServiceProviderConfig} registered in the supplied factory,
     * if it exists.
     *
     * @param factory the factory to use
     * @return the corresponding configuration, or {@code null} if none exists
     */
    public static PluginServiceProviderConfig getConfig(BeanFactory factory) {
        if (factory.containsBean(BEAN_NAME)) {
            return factory.getBean(BEAN_NAME, PluginServiceProviderConfig.class);
        }
        return null;
    }

}
