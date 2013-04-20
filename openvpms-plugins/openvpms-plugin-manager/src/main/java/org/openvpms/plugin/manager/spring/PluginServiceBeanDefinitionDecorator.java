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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;

import java.util.ArrayList;


/**
 * Called when Spring encounters a "plugin:service" element in a bean declaration.
 *
 * @author Tim Anderson
 */
public class PluginServiceBeanDefinitionDecorator implements BeanDefinitionDecorator {

    /**
     * The plugin service provider bean name.
     */
    public static final String BEAN_NAME = "pluginServiceProvider";

    /**
     * The services property name.
     */
    private static final String SERVICES = "services";

    /**
     * Parses the specified {@link Node} for a plugin:service element.
     *
     * @param node       the export node
     * @param definition the bean definition holder
     * @param context    the parser context
     * @return the bean definition holder
     */
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext context) {
        BeanDefinitionRegistry registry = context.getRegistry();
        BeanDefinition factory;
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(PluginServiceFactoryBean.class);
            factory = builder.getBeanDefinition();
            if (!factory.getPropertyValues().contains(SERVICES)) {
                factory.getPropertyValues().addPropertyValue(SERVICES, new ArrayList<String>());
            }
            registry.registerBeanDefinition(BEAN_NAME, factory);
        } else {
            factory = registry.getBeanDefinition(BEAN_NAME);
        }

        factory.getPropertyValues().getPropertyValue(SERVICES).getValue();
        return definition;
    }

}
