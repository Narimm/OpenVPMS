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

    public static final String HOST_COMPONENT_PROVIDER = "hostComponentProvider";
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
        BeanDefinition factoryBean;
        if (!registry.containsBeanDefinition(HOST_COMPONENT_PROVIDER)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(PluginServiceFactoryBean.class);
            factoryBean = builder.getBeanDefinition();
            if (!factoryBean.getPropertyValues().contains(SERVICES)) {
                factoryBean.getPropertyValues().addPropertyValue(SERVICES, new ArrayList<String>());
            }
            registry.registerBeanDefinition(HOST_COMPONENT_PROVIDER, factoryBean);
        } else {
            factoryBean = registry.getBeanDefinition(HOST_COMPONENT_PROVIDER);
        }

        factoryBean.getPropertyValues().getPropertyValue(SERVICES).getValue();
        return definition;
    }

}
