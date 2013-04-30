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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.view.layout;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.util.ShortNamePairArchetypeHandlers;


/**
 * Implementation of the {@link IMObjectLayoutStrategyFactory}
 * interface for editing layout strategies.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-07-28 00:05:58Z $
 */
public class EditLayoutStrategyFactory
    extends AbstractLayoutStrategyFactory {

    /**
     * Layout strategy implementations.
     */
    private static ShortNamePairArchetypeHandlers strategies;


    /**
     * Creates a new layout strategy for an object.
     *
     * @param object the object to create the layout strategy for
     * @param parent the parent object. May be <code>null</code>
     */
    @Override
    public IMObjectLayoutStrategy create(IMObject object, IMObject parent) {
        IMObjectLayoutStrategy result = super.create(object, parent);
        if (result instanceof ExpandableLayoutStrategy) {
            ((ExpandableLayoutStrategy) result).setShowOptional(
                !object.isNew());
        }
        return result;
    }

    /**
     * Returns the strategy imlementations.
     *
     * @return the strategy implementations
     */
    protected ShortNamePairArchetypeHandlers getStrategies() {
        synchronized (getClass()) {
            if (strategies == null) {
                strategies = load("EditLayoutStrategyFactory.properties");
                strategies.load("DefaultLayoutStrategyFactory.properties");
            }
        }
        return strategies;
    }

}
