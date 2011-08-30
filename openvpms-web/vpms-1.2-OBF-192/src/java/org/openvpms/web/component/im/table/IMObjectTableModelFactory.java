/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.table;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;

import java.util.ArrayList;
import java.util.List;


/**
 * Factory for {@link IMObjectTableModel} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectTableModelFactory {

    /**
     * Table model implementations.
     */
    private static ArchetypeHandlers<IMObjectTableModel> models;

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(IMObjectTableModelFactory.class);

    /**
     * Prevent construction.
     */
    private IMObjectTableModelFactory() {
    }

    /**
     * Creates a new table model.
     *
     * @param shortNames the short names of the archetype the table must display
     * @param context    the layout context. May be <tt>null</tt>
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
     * @param context the layout context. May be <tt>null</tt>
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
     * @param shortNames the archetype short names. May be <tt>null;
     * @param context    the layout context
     * @return a new table model, or <code>null</code> if there is no valid
     *         constructor
     */
    @SuppressWarnings("unchecked")
    private static <T extends IMObject> IMObjectTableModel<T>
            construct(ArchetypeHandler<IMObjectTableModel<T>> handler,
                      String[] shortNames,
                      LayoutContext context) {
        List<Object[]> methodParams = new ArrayList<Object[]>();
        if (shortNames != null && context != null) {
            methodParams.add(new Object[]{shortNames, context});
        }
        if (context != null) {
            methodParams.add(new Object[]{context});
        }
        methodParams.add(new Object[0]);

        for (Object[] params : methodParams) {
            try {
                Class clazz = handler.getType();
                IMObjectTableModel<T> result = (IMObjectTableModel<T>)
                        ConstructorUtils.invokeConstructor(clazz, params);
                handler.initialise(result);
                return result;
            } catch (NoSuchMethodException ignore) {
                // no-op
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
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
