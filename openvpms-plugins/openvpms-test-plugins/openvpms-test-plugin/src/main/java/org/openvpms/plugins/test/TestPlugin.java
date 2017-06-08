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

package org.openvpms.plugins.test;

import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.plugin.test.service.TestService;

import java.util.Iterator;


/**
 * Test plugin.
 *
 * @author Tim Anderson
 */
public class TestPlugin {

    /**
     * Constructs a {@code TestPlugin}.
     *
     * @param archetypeService the archetype service
     * @param service          the test service
     */
    public TestPlugin(IArchetypeService archetypeService, TestService service) {
        ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.PRACTICE, true);
        query.setMaxResults(1);
        Iterator<Party> iterator = new IMObjectQueryIterator<Party>(archetypeService, query);
        while (iterator.hasNext()) {
            Party practice = iterator.next();
            service.setValue(practice.getName());
        }
    }
}
