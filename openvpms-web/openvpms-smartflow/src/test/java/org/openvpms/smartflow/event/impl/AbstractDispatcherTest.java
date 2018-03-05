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

package org.openvpms.smartflow.event.impl;

import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * .
 *
 * @author Tim Anderson
 */
public abstract class AbstractDispatcherTest extends ArchetypeServiceTest {

    /**
     * Creates a practice location with the specified API key.
     *
     * @param name the practice location name
     * @param key  the API key. May be {@code null}
     * @return a new practice location
     */
    protected Party createLocation(String name, String key) {
        Party location = (Party) create(PracticeArchetypes.LOCATION);
        location.setName(name);
        setKey(location, key);
        return location;
    }

    /**
     * Sets the API key for a practice location.
     *
     * @param location the practice location
     * @param key      the API key. May be {@code null}
     */
    protected void setKey(Party location, String key) {
        IMObjectBean bean = new IMObjectBean(location);
        bean.setValue("smartFlowSheetKey", key);
    }
}
