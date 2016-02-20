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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.util;

import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link EntityRelationshipHelper} class.
 *
 * @author Tim Anderson
 */
public class EntityRelationshipHelperTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link EntityRelationshipHelper#getDefaultTarget(Entity, String, IArchetypeService)} and
     * {@link EntityRelationshipHelper#setDefault(Entity, String, EntityRelationship, IArchetypeService)} methods.
     */
    @Test
    public void testGetDefaultTarget() {
        IArchetypeService service = getArchetypeService();
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        EntityBean bean = new EntityBean(practice);

        EntityRelationship rel1 = bean.addNodeRelationship("locations", location1);
        EntityRelationship rel2 = bean.addNodeRelationship("locations", location2);

        // if no location is the default, either could be returned
        Entity entity1 = EntityRelationshipHelper.getDefaultTarget(practice, "locations", service);
        assertNotNull(entity1);
        assertTrue(entity1.equals(location1) || entity1.equals(location2));

        // make location1 the default and verify it is returned
        EntityRelationshipHelper.setDefault(practice, "locations", rel1, service);
        assertEquals(location1, EntityRelationshipHelper.getDefaultTarget(practice, "locations", service));

        // make location2 the default and verify it is returned
        EntityRelationshipHelper.setDefault(practice, "locations", rel2, service);
        assertEquals(location2, EntityRelationshipHelper.getDefaultTarget(practice, "locations", service));

        // make location2 inactive, and verify location1 is returned
        location2.setActive(false);
        location2.removeEntityRelationship(rel2); // so don't have to save the practice
        save(location2);
        assertEquals(location1, EntityRelationshipHelper.getDefaultTarget(practice, "locations", service));
    }
}
