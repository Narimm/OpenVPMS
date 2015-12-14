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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.hl7.util.HL7Archetypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link HL7Mapping} class.
 *
 * @author Tim Anderson
 */
public class HL7MappingTestCase extends ArchetypeServiceTest {

    /**
     * Verifies the default values for <em>entity.HL7Mapping</em> match those expected.
     */
    @Test
    public void testDefaults() {
        Entity object = (Entity) getArchetypeService().create(HL7Archetypes.MAPPING);
        HL7Mapping mapping = HL7Mapping.create(object, getArchetypeService());
        assertTrue(mapping.sendADT());
        assertTrue(mapping.sendUpdatePatient());
        assertTrue(mapping.sendCancelAdmit());
        assertTrue(mapping.getPopulatePID3());
        assertFalse(mapping.getPopulatePID2());
        assertEquals("M", mapping.getMale());
        assertEquals("C", mapping.getMaleDesexed());
        assertEquals("F", mapping.getFemale());
        assertEquals("S", mapping.getFemaleDesexed());
        assertEquals("U", mapping.getUnknownSex());
        assertNull(mapping.getSpeciesLookup());
        assertNull(mapping.getUnmappedSpecies());
        assertFalse(mapping.includeMillis());
        assertFalse(mapping.includeTimeZone());
    }

    /**
     * Verifies the values for <em>entity.HL7MappingCubex</em> match those expected.
     */
    @Test
    public void testCubexMapping() {
        Entity object = (Entity) getArchetypeService().create(HL7Archetypes.CUBEX_MAPPING);
        HL7Mapping mapping = HL7Mapping.create(object, getArchetypeService());
        assertTrue(mapping.sendADT());
        assertTrue(mapping.sendUpdatePatient());
        assertTrue(mapping.sendCancelAdmit());
        assertFalse(mapping.getPopulatePID3());
        assertTrue(mapping.getPopulatePID2());
        assertEquals("M", mapping.getMale());
        assertEquals("NM", mapping.getMaleDesexed());
        assertEquals("F", mapping.getFemale());
        assertEquals("SF", mapping.getFemaleDesexed());
        assertEquals("U", mapping.getUnknownSex());
        assertNull(mapping.getSpeciesLookup());
        assertNull(mapping.getUnmappedSpecies());
        assertFalse(mapping.includeMillis());
        assertFalse(mapping.includeTimeZone());
    }

    /**
     * Verifies the values for <em>entity.HL7MappingIDEXX</em> match those expected.
     */
    @Test
    public void testIDEXXMapping() {
        Entity object = (Entity) getArchetypeService().create(HL7Archetypes.IDEXX_MAPPING);
        HL7Mapping mapping = HL7Mapping.create(object, getArchetypeService());
        assertTrue(mapping.sendADT());
        assertFalse(mapping.sendUpdatePatient());
        assertFalse(mapping.sendCancelAdmit());
        assertTrue(mapping.getPopulatePID3());
        assertFalse(mapping.getPopulatePID2());
        assertEquals("M", mapping.getMale());
        assertEquals("C", mapping.getMaleDesexed());
        assertEquals("F", mapping.getFemale());
        assertEquals("S", mapping.getFemaleDesexed());
        assertEquals("U", mapping.getUnknownSex());
        assertEquals("lookup.speciesIDEXX", mapping.getSpeciesLookup());
        assertEquals("OTHER", mapping.getUnmappedSpecies());
        assertFalse(mapping.includeMillis());
        assertFalse(mapping.includeTimeZone());
    }

}
