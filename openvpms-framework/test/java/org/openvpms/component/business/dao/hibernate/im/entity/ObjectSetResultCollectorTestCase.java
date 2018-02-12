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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.junit.Test;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link ObjectSetResultCollector}.
 *
 * @author Tim Anderson
 */
public class ObjectSetResultCollectorTestCase {

    /**
     * Verifies that references can be collected.
     */
    @Test
    public void testSelectReference() {
        UUID linkId = UUID.randomUUID();
        List<String> names = Arrays.asList("patient.archetypeId", "patient.id", "patient.linkId");
        List<String> refNames = Collections.singletonList("patient");
        Map<String, Set<String>> types = new HashMap<>();
        types.put("patient", new HashSet<>(Collections.singletonList("party.patientpet")));
        ObjectSetResultCollector collector = new ObjectSetResultCollector(names, refNames, types);
        Object[] row1 = {new ArchetypeId("party.patientpet"), 10L, linkId.toString()};
        Object[] row2 = {null, null, null};  // null reference. This could happen in a left join
        collector.collect(row1);
        collector.collect(row2);

        List<ObjectSet> results = collector.getResults();
        assertEquals(2, results.size());

        ObjectSet set1 = results.get(0);
        IMObjectReference patient1 = set1.getReference("patient.reference");
        assertNotNull(patient1);
        assertEquals("party.patientpet", patient1.getArchetype());
        assertEquals(10, patient1.getId());
        assertEquals(linkId.toString(), patient1.getLinkId());

        ObjectSet set2 = results.get(1);
        IMObjectReference patient2 = set2.getReference("patient.reference");
        assertNull(patient2);
    }
}
