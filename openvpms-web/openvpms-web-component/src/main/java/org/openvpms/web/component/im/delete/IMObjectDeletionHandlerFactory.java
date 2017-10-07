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

package org.openvpms.web.component.im.delete;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory for {@link IMObjectDeletionHandler} instances.
 *
 * @author Tim Anderson
 */
public class IMObjectDeletionHandlerFactory implements ApplicationContextAware {

    /**
     * IMObjectDeletionHandler implementations.
     */
    private ArchetypeHandlers<IMObjectDeletionHandler> deleters;

    /**
     * The application context.
     */
    private ApplicationContext context;

    /**
     * Constructs an {@link IMObjectDeletionHandlerFactory}.
     *
     * @param service the archetype service
     */
    public IMObjectDeletionHandlerFactory(IArchetypeService service) {
        deleters = new ArchetypeHandlers<>("IMObjectDeletionHandlerFactory", null, IMObjectDeletionHandler.class,
                                           service);
    }

    /**
     * Set the ApplicationContext that this object runs in.
     * Normally this call will be used to initialize the object.
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * Creates a new {@link IMObjectDeletionHandler} for the specified object.
     *
     * @param object the object to delete
     * @return a new deletion handler
     */
    @SuppressWarnings("unchecked")
    public <T extends IMObject> IMObjectDeletionHandler<T> create(T object) {
        String archetype = object.getArchetypeId().getShortName();
        ArchetypeHandler<IMObjectDeletionHandler> handler = deleters.getHandler(archetype);
        Class type = (handler != null) ? handler.getType() : DefaultDeletionHandler.class;
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory(context);
        factory.registerSingleton("object", object);
        return (IMObjectDeletionHandler<T>) factory.createBean(type, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR,
                                                               false);
    }

}
