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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.view.layout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ShortNamePairArchetypeHandlers;
import org.openvpms.web.component.im.layout.DefaultLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;


/**
 * Abstact implementation of the {@link IMObjectLayoutStrategyFactory}
 * interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractLayoutStrategyFactory
    implements IMObjectLayoutStrategyFactory {

    /**
     * The logger.
     */
    private static final Log _log
        = LogFactory.getLog(AbstractLayoutStrategyFactory.class);

    /**
     * Creates a new layout strategy for an object.
     *
     * @param object the object to create the layout strategy for
     * @return a new layout strategy
     */
    public IMObjectLayoutStrategy create(IMObject object) {
        return create(object, null);
    }

    /**
     * Creates a new layout strategy for an object.
     *
     * @param object the object to create the layout strategy for
     * @param parent the parent object. May be <code>null</code>
     */
    public IMObjectLayoutStrategy create(IMObject object, IMObject parent) {
        IMObjectLayoutStrategy result = null;
        ArchetypeHandler handler;
        if (parent != null) {
            String primary = object.getArchetypeId().getShortName();
            String secondary = parent.getArchetypeId().getShortName();
            handler = getStrategies().getHandler(primary, secondary);
        } else {
            String shortName = object.getArchetypeId().getShortName();
            handler = getStrategies().getHandler(shortName);
        }
        if (handler != null) {
            try {
                result = (IMObjectLayoutStrategy) handler.create();
            } catch (Throwable throwable) {
                _log.error(throwable, throwable);
            }
        }
        if (result == null) {
            result = new DefaultLayoutStrategy();
        }

        return result;
    }

    /**
     * Returns the strategy imlementations.
     *
     * @return the strategy implementations
     */
    protected abstract ShortNamePairArchetypeHandlers getStrategies();

    /**
     * Helper to load the strategy implementations.
     *
     * @param name the resource name
     * @return the strategy implementations
     */
    protected ShortNamePairArchetypeHandlers load(String name) {
        return new ShortNamePairArchetypeHandlers(name,
                                                  IMObjectLayoutStrategy.class);
    }

}
