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

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.lang.reflect.Constructor;


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
    public static <T extends IMObject> IMObjectTableModel<T> create(
        String[] shortNames, LayoutContext context) {
        IMObjectTableModel<T> result = null;

        ArchetypeHandler handler = getTableModels().getHandler(shortNames);
        if (handler != null) {
            result = construct(handler, shortNames, context);
        }
        if (result == null) {
            result = new DefaultIMObjectTableModel<T>();
        }
        return result;
    }

    /**
     * Creates a new table model.
     *
     * @param type    the table model type
     * @param context the layout context. May be {@code null}
     * @return a new table model, or {@link DefaultIMObjectTableModel} if
     *         the type cannot be constructed
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> IMObjectTableModel<T> create(
        Class type, LayoutContext context) {
        IMObjectTableModel<T> result = null;

        ArchetypeHandler handler = getTableModels().getHandler(type);
        if (handler != null) {
            result = construct(handler, null, context);
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
     * @param shortNames the archetype short names. May be {@code null;
     * @param context    the layout context
     * @return a new table model, or {@code null} if there is no valid
     *         constructor
     */
    @SuppressWarnings("unchecked")
    private static <T extends IMObject> IMObjectTableModel<T> construct(ArchetypeHandler<IMObjectTableModel<T>> handler,
                                                                        String[] shortNames, LayoutContext context) {

        Object[] params;
        Class type = handler.getType();
        Constructor ctor = ConstructorUtils.getAccessibleConstructor(type, new Class[]{String[].class,
            LayoutContext.class});
        if (ctor != null) {
            params = new Object[]{shortNames, context};
        } else {
            ctor = ConstructorUtils.getAccessibleConstructor(type, new Class[]{String[].class});
            if (ctor != null) {
                params = new Object[]{shortNames};
            } else {
                ctor = ConstructorUtils.getAccessibleConstructor(type, new Class[]{LayoutContext.class});
                if (ctor != null) {
                    params = new Object[]{context};
                } else {
                    ctor = ConstructorUtils.getAccessibleConstructor(type, new Class[0]);
                    if (ctor != null) {
                        params = new Object[0];
                    } else {
                        log.error("No valid constructor found for class: " + type.getName());
                        return null;
                    }
                }
            }
        }
        try {
            IMObjectTableModel<T> result = (IMObjectTableModel<T>) ConstructorUtils.invokeConstructor(type, params);
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
            models = new ArchetypeHandlers<IMObjectTableModel>(
                "IMObjectTableModelFactory",
                IMObjectTableModel.class);
        }
        return models;
    }

}
