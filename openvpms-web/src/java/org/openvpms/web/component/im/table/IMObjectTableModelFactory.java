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
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ArchetypeHandlers;
import org.openvpms.web.component.im.util.DescriptorHelper;

import java.util.HashSet;
import java.util.Set;


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
    private static ArchetypeHandlers _models;

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(IMObjectTableModelFactory.class);

    /**
     * Prevent construction.
     */
    private IMObjectTableModelFactory() {
    }

    /**
     * Creates a new table model.
     *
     * @param collection the collection node descriptor
     * @param context    the layout context
     * @return a new tabke model
     */
    public static IMObjectTableModel create(NodeDescriptor collection,
                                            LayoutContext context) {
        IMObjectTableModel result = null;

        String[] shortNames = DescriptorHelper.getShortNames(collection);
        Set<Class> matches = new HashSet<Class>();
        for (String shortName : shortNames) {
            Class clazz = getTableModels().getHandler(shortName);
            if (clazz != null) {
                matches.add(clazz);
            }
        }
        if (matches.size() == 1) {
            Class clazz = matches.toArray(new Class[0])[0];
            result = construct(clazz, shortNames, context);
        }

        if (result == null) {
            result = new DefaultIMObjectTableModel();
        }

        return result;
    }

    /**
     * Helper to create a new table model.
     *
     * @param clazz      the table model implementation
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new table model, or <code>null</code> if there is no valid
     *         constructor
     */
    private static IMObjectTableModel construct(Class clazz,
                                                String[] shortNames,
                                                LayoutContext context) {
        Object[][] methodParams = {{shortNames, context}, {context}, {}};

        for (Object[] params : methodParams) {
            try {
                return (IMObjectTableModel) ConstructorUtils.invokeConstructor(
                        clazz, params);
            } catch (NoSuchMethodException ignore) {
                // no-op
            } catch (Throwable exception) {
                _log.error(exception, exception);
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
        if (_models == null) {
            _models = new ArchetypeHandlers(
                    "IMObjectTableModelFactory.properties",
                    IMObjectTableModel.class);
        }
        return _models;
    }

}
