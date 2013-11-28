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

package org.openvpms.web.component.im.table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.SimpleAutowireCandidateResolver;


/**
 * Factory for {@link IMObjectTableModel} instances.
 *
 * @author Tim Anderson
 */
public class IMObjectTableModelFactory {

    /**
     * Table model implementations.
     */
    private static ArchetypeHandlers<IMObjectTableModel> models;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectTableModelFactory.class);

    /**
     * Prevent construction.
     */
    private IMObjectTableModelFactory() {
    }

    /**
     * Creates a new table model.
     *
     * @param shortNames the short names of the archetype the table must display
     * @param context    the layout context. May be {@code null}
     * @return a new table model
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> IMObjectTableModel<T> create(String[] shortNames, LayoutContext context) {
        return create(shortNames, null, context);
    }

    /**
     * Creates a new table model.
     *
     * @param shortNames the short names of the archetype the table must display
     * @param query      the query
     * @param context    the layout context. May be {@code null}
     * @return a new table model
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> IMObjectTableModel<T> create(String[] shortNames, Query<T> query,
                                                                    LayoutContext context) {
        IMObjectTableModel<T> result = null;

        ArchetypeHandler handler = getTableModels().getHandler(shortNames);
        if (handler != null) {
            result = construct(handler, shortNames, query, context);
        }
        if (result == null) {
            result = new DefaultDescriptorTableModel<T>(shortNames, query, context);
        }
        return result;
    }

    /**
     * Creates a new table model.
     *
     * @param type    the table model type
     * @param context the layout context. May be {@code null}
     * @return a new table model, or {@link DefaultIMObjectTableModel} if the type cannot be constructed
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> IMObjectTableModel<T> create(
            Class type, LayoutContext context) {
        IMObjectTableModel<T> result = null;

        ArchetypeHandler handler = getTableModels().getHandler(type);
        if (handler != null) {
            result = construct(handler, null, null, context);
        }
        if (result == null) {
            result = new DefaultIMObjectTableModel<T>();
        }
        return result;
    }

    /**
     * Helper to create a new table model.
     *
     * @param handler    the archetype handler
     * @param shortNames the archetype short names. May be {@code null}
     * @param query      the query. May be {@code null}
     * @param context    the layout context
     * @return a new table model, or {@code null} if there is no valid constructor
     */
    @SuppressWarnings("unchecked")
    private static <T extends IMObject> IMObjectTableModel<T> construct(ArchetypeHandler<IMObjectTableModel<T>> handler,
                                                                        final String[] shortNames, Query<T> query,
                                                                        LayoutContext context) {
        Class type = handler.getType();
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        // Spring doesn't automatically autowire the shortNames array when it is registered as a singleton, so need to
        // register the following to handle it explicitly
        factory.setAutowireCandidateResolver(new SimpleAutowireCandidateResolver() {
            @Override
            public Object getSuggestedValue(DependencyDescriptor descriptor) {
                if (String[].class.equals(descriptor.getDependencyType())) {
                    return shortNames;
                }
                return super.getSuggestedValue(descriptor);
            }
        });
        if (query != null) {
            factory.registerSingleton("query", query);
        }
        if (context != null) {
            factory.registerSingleton("context", context);
        }
        try {
            IMObjectTableModel<T> result = (IMObjectTableModel<T>) factory.createBean(
                    type, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
            handler.initialise(result);
            return result;
        } catch (Throwable exception) {
            log.error(exception, exception);
        }
        return null;
    }

    /**
     * Returns the table models.
     *
     * @return the table models
     */
    private static synchronized ArchetypeHandlers getTableModels() {
        if (models == null) {
            models = new ArchetypeHandlers<IMObjectTableModel>("IMObjectTableModelFactory", IMObjectTableModel.class);
        }
        return models;
    }

}
