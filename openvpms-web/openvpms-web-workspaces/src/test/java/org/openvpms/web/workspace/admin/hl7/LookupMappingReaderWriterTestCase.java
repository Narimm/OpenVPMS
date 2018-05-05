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

package org.openvpms.web.workspace.admin.hl7;

import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.SPECIES;

/**
 * Tests the {@link LookupMappingCSVWriter} and {@link LookupMappingCSVReader}.
 *
 * @author Tim Anderson
 */
public class LookupMappingReaderWriterTestCase extends AbstractLookupMappingTest {

    /**
     * Tests writing a CSV and reading it back.
     */
    @Test
    public void testReadWrite() {
        DocumentHandlers handlers = new DocumentHandlers(getArchetypeService());

        Lookup species1 = getSpecies("SPECIES_1", "species1");
        Lookup species2 = getSpecies("SPECIES_2", "species2");
        Lookup species3 = getSpecies("SPECIES_3", "species3");

        Lookup speciesA = getIDEXXSpecies("SPECIES_A", "speciesA");
        Lookup speciesB = getIDEXXSpecies("SPECIES_B", "speciesB");
        Lookup speciesC = getIDEXXSpecies("SPECIES_C", "speciesC");
        IMObjectBean bean = new IMObjectBean(species2);
        IMObjectRelationship relationship = bean.addNodeTarget("mapping", speciesB);
        speciesB.addLookupRelationship((LookupRelationship) relationship);
        save(species2, speciesB);

        final List<Lookup> from = Arrays.asList(species1, species2, species3);
        final List<Lookup> to = Arrays.asList(speciesA, speciesB, speciesC);

        LookupMappingCSVWriter writer = new LookupMappingCSVWriter(getArchetypeService(), getLookupService(),
                                                                   handlers, SEPARATOR) {
            @Override
            protected List<Lookup> getLookups(String shortName) {
                return SPECIES.equals(shortName) ? from : to;
            }
        };
        Document document = writer.write("mapping.csv", SPECIES, IDEXX_SPECIES);
        LookupMappingCSVReader reader = new LookupMappingCSVReader(handlers, SEPARATOR);
        LookupMappings read = reader.read(document);
        List<LookupMapping> mappings = read.getMappings();
        List<LookupMapping> errors = read.getErrors();
        assertEquals(1, mappings.size());
        assertEquals(4, errors.size());
        checkMapping(errors.get(0), SPECIES, "SPECIES_1", "species1", IDEXX_SPECIES, null, null, 2,
                     "A value for Map To Code is required");
        checkMapping(mappings.get(0), SPECIES, "SPECIES_2", "species2", IDEXX_SPECIES, "SPECIES_B", "speciesB", 3,
                     null);
        checkMapping(errors.get(1), SPECIES, "SPECIES_3", "species3", IDEXX_SPECIES, null, null, 4,
                     "A value for Map To Code is required");
        checkMapping(errors.get(2), SPECIES, null, null, null, null, null, 5,
                     "A value for Map From Code is required");
        checkMapping(errors.get(3), SPECIES, null, null, null, null, null, 6,
                     "A value for Map From Code is required");
    }

}
