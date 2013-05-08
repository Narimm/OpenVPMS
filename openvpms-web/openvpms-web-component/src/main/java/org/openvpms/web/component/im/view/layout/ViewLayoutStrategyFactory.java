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

package org.openvpms.web.component.im.view.layout;

import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.util.ShortNamePairArchetypeHandlers;


/**
 * Implementation of the {@link IMObjectLayoutStrategyFactory}
 * interface for viewing objects.
 * Loads configuration from <em>ViewLayoutStrategyFactory.properties</em>
 * and <em>DefaultLayoutStrategyFactory.properties</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ViewLayoutStrategyFactory
    extends AbstractLayoutStrategyFactory {

    /**
     * Layout strategy implementations.
     */
    private static ShortNamePairArchetypeHandlers strategies;

    /**
     * Returns the strategy imlementations.
     *
     * @return the strategy implementations
     */
    protected ShortNamePairArchetypeHandlers getStrategies() {
        synchronized (getClass()) {
            if (strategies == null) {
                strategies = load("ViewLayoutStrategyFactory.properties");
                strategies.load("DefaultLayoutStrategyFactory.properties");
            }
        }
        return strategies;
    }

}
