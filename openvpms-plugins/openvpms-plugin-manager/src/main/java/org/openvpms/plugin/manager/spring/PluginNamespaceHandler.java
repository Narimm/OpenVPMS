package org.openvpms.plugin.manager.spring;

import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class PluginNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * Invoked by the {@link DefaultBeanDefinitionDocumentReader} after
     * construction but before any custom elements are parsed.
     */
    public void init() {
        registerBeanDefinitionDecorator("service", new PluginServiceBeanDefinitionDecorator());
    }
}
