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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.hl7.patient.PatientContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link PIDPopulator}.
 *
 * @author Tim Anderson
 */
public class PIDPopulatorTestCase extends AbstractMessageTest {

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();

        PatientContext context = getContext();
        Mockito.when(context.getPatientId()).thenReturn(1001L);
        Mockito.when(context.getClinicianId()).thenReturn(2001L);
        Mockito.when(context.getCustomerId()).thenReturn(3001L);
    }

    /**
     * Verifies that species can be mapped, and that species with no mapping get set to the unmappedSpecies.
     */
    @Test
    public void testSpeciesMapping() throws HL7Exception, IOException {
        String noMap = "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||OTHER^OTHER";
        String withMap = "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432||||3001|||||||||||||||||CANINE^Dog";

        // map species to IDEXX species
        HL7Mapping mapping = new HL7Mapping();
        mapping.setSpeciesLookup("lookup.speciesIDEXX");
        mapping.setUnmappedSpecies("OTHER");

        // set up a message to populate
        HapiContext hapiContext = HapiContextFactory.create(new IDGenerator() {
            @Override
            public String getID() throws IOException {
                return "1200022";
            }
        });
        ADT_A01 adt = new ADT_A01(hapiContext.getModelClassFactory());
        adt.setParser(hapiContext.getGenericParser());
        adt.initQuickstart("ADT", "A01", "P");

        // set up the lookups. Ensure there is no mapping
        Lookup canine = TestHelper.getLookup(PatientArchetypes.SPECIES, "CANINE");
        HL7TestHelper.removeRelationships(canine); // remove any existing relationship
        Lookup idexxCanine = TestHelper.getLookup("lookup.speciesIDEXX", "CANINE", "Dog", true);

        // populate the PID segment
        PIDPopulator populator = new PIDPopulator(getArchetypeService(), getLookupService());
        populator.populate(adt.getPID(), getContext(), mapping);
        String encode = adt.getPID().encode();

        // verify the species as been set to the unmappedSpecies
        assertEquals(noMap, encode);

        // set up mapping relationship
        HL7TestHelper.addMapping(canine, idexxCanine);

        // verify the species is populated correctly
        populator.populate(adt.getPID(), getContext(), mapping);
        encode = adt.getPID().encode();
        assertEquals(withMap, encode);
    }

}
