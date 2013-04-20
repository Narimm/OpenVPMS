package org.openvpms.plugin.manager.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.Collections;
import java.util.Set;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class PluginServiceFactoryBean extends AbstractFactoryBean {

    private Set<String> beanNames = Collections.emptySet();

    public void setBeanNames(Set<String> beanNames) {
        this.beanNames = beanNames;
    }

    /**
     * Return the type of object that this FactoryBean creates.
     *
     * @return {@code HostActivator.class}
     */
    @Override
    public Class<?> getObjectType() {
        return SpringPluginServiceProvider.class;
    }

    /**
     * @return the object returned by this factory
     * @throws Exception if an exception occured during object creation
     * @see #getObject()
     */
    @Override
    protected Object createInstance() throws Exception {
        return new SpringPluginServiceProvider(getBeanFactory(), beanNames);
    }


}
