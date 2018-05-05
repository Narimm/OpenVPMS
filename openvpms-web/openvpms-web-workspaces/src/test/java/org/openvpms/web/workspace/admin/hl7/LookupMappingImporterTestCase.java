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

package org.openvpms.web.workspace.admin.hl7;

import au.com.bytecode.opencsv.CSVWriter;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.csv.AbstractCSVReader;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.lookup.LookupRelationship;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.SPECIES;

/**
 * Tests the {@link LookupMappingImporter}.
 *
 * @author Tim Anderson
 */
public class LookupMappingImporterTestCase extends AbstractLookupMappingTest {

    /**
     * Sets up the test case
     */
    @Before
    public void setUp() {
        removeLookup(SPECIES, "SPECIES_1");
        removeLookup(SPECIES, "SPECIES_2");
        removeLookup(SPECIES, "SPECIES_3");
        removeLookup(IDEXX_SPECIES, "SPECIES_A");
        removeLookup(IDEXX_SPECIES, "SPECIES_B");
        removeLookup(IDEXX_SPECIES, "SPECIES_C");
    }

    /**
     * Tests importing lookup mappings.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testImport() throws IOException {
        String[][] data = {{SPECIES, "SPECIES_1", "species1", IDEXX_SPECIES, "SPECIES_A", "speciesA"},
                           {SPECIES, "SPECIES_2", "species2", IDEXX_SPECIES, "SPECIES_B", "speciesB"},
                           {SPECIES, "SPECIES_3", "species3", IDEXX_SPECIES, "SPECIES_C", "speciesC"}};
        LookupMappings mappings = load(data);
        assertEquals(3, mappings.getMappings().size());
        assertEquals(0, mappings.getErrors().size());

        checkMappings(data);
    }

    /**
     * Verifies that a mapping can be specified twice, and that the second occurrence is ignored.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testLoadDuplicate() throws IOException {
        String[][] data = {{SPECIES, "SPECIES_1", "species1", IDEXX_SPECIES, "SPECIES_A", "speciesA"},
                           {SPECIES, "SPECIES_1", "species1", IDEXX_SPECIES, "SPECIES_A", "speciesA"}};
        LookupMappings mappings = load(data);
        assertEquals(1, mappings.getMappings().size());
        assertEquals(0, mappings.getErrors().size());
        checkMappings(data);
    }

    /**
     * Verifies that the a mapping is not created if the name does not match that of an existing lookup.
     */
    @Test
    public void testMismatchName() throws IOException {
        // pre-create lookups
        getSpecies("SPECIES_1", "species1");
        getSpecies("SPECIES_2", "species2");
        getIDEXXSpecies("SPECIES_A", "speciesA");
        getIDEXXSpecies("SPECIES_B", "speciesB");

        String[][] invalid1 = {{SPECIES, "SPECIES_1", "species 1", IDEXX_SPECIES, "SPECIES_A", "speciesA"}};

        String[][] invalid2 = {{SPECIES, "SPECIES_2", "species2", IDEXX_SPECIES, "SPECIES_B", "species B"}};

        LookupMappings mappings = load(invalid1);
        assertEquals(0, mappings.getMappings().size());
        List<LookupMapping> errors = mappings.getErrors();
        assertEquals(1, errors.size());
        checkMapping(errors.get(0), SPECIES, "SPECIES_1", "species 1", IDEXX_SPECIES, "SPECIES_A", "speciesA", 2,
                     "Expected species1 for SPECIES_1 but got species 1");

        mappings = load(invalid2);
        assertEquals(0, mappings.getMappings().size());
        errors = mappings.getErrors();
        assertEquals(1, errors.size());

        checkMapping(errors.get(0), SPECIES, "SPECIES_2", "species2", IDEXX_SPECIES, "SPECIES_B", "species B", 2,
                     "Expected speciesB for SPECIES_B but got species B");
    }

    /**
     * Verifies that an error is raised if a lookup is mapped to an invalid archetype.
     */
    @Test
    public void testMapToInvalidArchetype() throws IOException {
        String[][] data = {{SPECIES, "SPECIES_1", "species1", SPECIES, "SPECIES_2", "species2"}};
        LookupMappings mappings = load(data);
        assertEquals(0, mappings.getMappings().size());
        List<LookupMapping> errors = mappings.getErrors();
        assertEquals(1, errors.size());
        checkMapping(errors.get(0), SPECIES, "SPECIES_1", "species1", SPECIES, "SPECIES_2", "species2", 2,
                     "Cannot add target with archetype lookup.species to node named mapping");
    }

    /**
     * Verifies that two mappings of the same type for the one source cannot be loaded.
     *
     * @throws IOException for any error
     */
    @Test
    public void testDoubleMapping() throws IOException {
        String[][] data = {{SPECIES, "SPECIES_1", "species1", IDEXX_SPECIES, "SPECIES_A", "speciesA"},
                           {SPECIES, "SPECIES_1", "species1", IDEXX_SPECIES, "SPECIES_B", "speciesB"}};
        LookupMappings mappings = load(data);
        assertEquals(1, mappings.getMappings().size());
        List<LookupMapping> errors = mappings.getErrors();
        assertEquals(1, errors.size());
        checkMapping(errors.get(0), SPECIES, "SPECIES_1", "species1", IDEXX_SPECIES, "SPECIES_B", "speciesB", 3,
                     "Failed to validate Mapping of Species: There are multiple mappings of the same type");
    }

    /**
     * Verifies a lookup mapping has been established for CSV data.
     *
     * @param data the CSV data
     */
    private void checkMappings(String[][] data) {
        ILookupService service = getLookupService();
        for (String[] mapping : data) {
            // make sure the 'from lookup' exists, with the correct name
            Lookup from = service.getLookup(mapping[0], mapping[1]);
            assertNotNull(from);
            assertEquals(mapping[2], from.getName());

            // make sure the 'to lookup' exists, with the correct name
            Lookup to = service.getLookup(mapping[3], mapping[4]);
            assertNotNull(to);
            assertEquals(mapping[5], to.getName());

            // make sure there is a relationship between them
            IMObjectBean bean = new IMObjectBean(from);
            assertTrue(bean.getNodeTargetObjectRefs("mapping").contains(to.getObjectReference()));
        }
    }

    /**
     * Serialises data to CSV and imports it.
     *
     * @param data the data to import
     * @return the loaded mappings
     * @throws IOException for any I/O error
     */
    private LookupMappings load(String[][] data) throws IOException {
        Document document = createCSV(data);
        LookupMappingImporter importer = new LookupMappingImporter(getArchetypeService(), getLookupService(),
                                                                   new DocumentHandlers(getArchetypeService()),
                                                                   SEPARATOR);
        return importer.load(document);
    }

    /**
     * Helper to remove a lookup and its relationships.
     *
     * @param shortName the lookup archetype short name
     * @param code      the lookup code
     */
    private void removeLookup(String shortName, String code) {
        Lookup lookup = getLookupService().getLookup(shortName, code);
        List<IMObject> toSave = new ArrayList<>();
        if (lookup != null) {
            toSave.add(lookup);
            IArchetypeService service = getArchetypeService();
            for (LookupRelationship relationship : new ArrayList<>(lookup.getSourceLookupRelationships())) {
                lookup.removeLookupRelationship(relationship);
                Lookup target = (Lookup) service.get(relationship.getTarget());
                if (target != null) {
                    target.removeLookupRelationship(relationship);
                    toSave.add(target);
                }
            }
            for (LookupRelationship relationship : new ArrayList<>(lookup.getTargetLookupRelationships())) {
                Lookup source = (Lookup) service.get(relationship.getSource());
                lookup.removeLookupRelationship(relationship);
                source.removeLookupRelationship(relationship);
                toSave.add(source);
            }
            service.save(toSave);
            remove(lookup);
        }
        assertNull(getLookupService().getLookup(shortName, code));
    }

    /**
     * Creates a CSV document.
     *
     * @param data the data to write
     * @return the CSV document
     * @throws IOException for any I/O error
     */
    private Document createCSV(String[][] data) throws IOException {
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, ',');
        csv.writeNext(LookupMappingCSVWriter.HEADER);
        for (String[] line : data) {
            csv.writeNext(line);
        }
        csv.close();

        DocumentHandlers handlers = new DocumentHandlers(getArchetypeService());
        DocumentHandler handler = handlers.get("Dummy.csv", AbstractCSVReader.MIME_TYPE);
        return handler.create("Dummy.csv", new ByteArrayInputStream(writer.toString().getBytes("UTF-8")),
                              AbstractCSVReader.MIME_TYPE, -1);
    }
}
