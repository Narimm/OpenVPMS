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

package org.openvpms.component.business.domain.im.datatypes.basic;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.springframework.test.context.ContextConfiguration;


/**
 * Tests the {@link TypedValueMap} class when made persistent via the archetype
 * service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-05-02 04:28:50Z $
 */
@ContextConfiguration("/org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml")
public class PersistentTypedValueMapTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the workaround for OBF-161.
     */
    @Test
    public void testOBF161() {
        Party person = (Party) create("party.person");
        person.getDetails().put("lastName", "foo");
        person.getDetails().put("firstName", null);
        save(person);

        person = get(person);
        assertNotNull(person);
        person.getDetails().put("firstName", "bar");
        save(person);
    }


}
