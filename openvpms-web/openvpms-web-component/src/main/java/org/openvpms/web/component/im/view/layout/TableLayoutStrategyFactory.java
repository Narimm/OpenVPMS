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

import org.openvpms.web.component.im.archetype.ShortNamePairArchetypeHandlers;


/**
 * Implementation of the {@link org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory}
 * for objects displayed in tables.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-11 04:09:07Z $
 */
public class TableLayoutStrategyFactory extends AbstractLayoutStrategyFactory {

    /**
     * Layout strategy implementations.
     */
    private static ShortNamePairArchetypeHandlers _strategies;


    /**
     * Returns the strategy imlementations.
     *
     * @return the strategy implementations
     */
    protected ShortNamePairArchetypeHandlers getStrategies() {
        synchronized (getClass()) {
            if (_strategies == null) {
                _strategies = load("TableLayoutStrategyFactory.properties");
            }
        }
        return _strategies;
    }

}
