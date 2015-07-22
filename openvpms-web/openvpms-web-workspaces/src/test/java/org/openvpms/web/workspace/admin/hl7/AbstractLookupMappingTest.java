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

package org.openvpms.web.workspace.admin.hl7;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.SPECIES;

/**
 * .
 *
 * @author Tim Anderson
 */
public abstract class AbstractLookupMappingTest extends ArchetypeServiceTest {

    /**
     * The separator to use.
     */
    protected static final char SEPARATOR = ',';

    /**
     * IDEXX species archetype.
     */
    protected static final String IDEXX_SPECIES = "lookup.speciesIDEXX";

    /**
     * Verifies a mapping matches that expected.
     *
     * @param mapping  the mapping to check
     * @param fromType the expected 'from lookup' archetype
     * @param fromCode the expected 'from lookup' code
     * @param fromName the expected 'from lookup' name
     * @param toType   the expected 'to lookup' archetype
     * @param toCode   the expected 'to lookup' code
     * @param toName   the expected 'to lookup' name
     * @param line     the expected line
     * @param error    the expected error
     */
    protected void checkMapping(LookupMapping mapping, String fromType, String fromCode, String fromName,
                                String toType, String toCode, String toName, int line, String error) {
        assertEquals(fromType, mapping.getFromType());
        assertEquals(fromCode, mapping.getFromCode());
        assertEquals(fromName, mapping.getFromName());
        assertEquals(toType, mapping.getToType());
        assertEquals(toCode, mapping.getToCode());
        assertEquals(toName, mapping.getToName());
        assertEquals(line, mapping.getLine());
        assertEquals(error, mapping.getError());
    }

    /**
     * Returns a species lookup.
     *
     * @param code the species code
     * @param name the species name
     * @return the lookup
     */
    protected Lookup getSpecies(String code, String name) {
        Lookup species = TestHelper.getLookup(SPECIES, code, name, true);
        IMObjectBean bean = new IMObjectBean(species);

        // remove any existing mappings
        for (LookupRelationship relationship : bean.getValues("mapping", LookupRelationship.class)) {
            species.removeLookupRelationship(relationship);
            Lookup target = (Lookup) get(relationship.getTarget());
            target.removeLookupRelationship(relationship);
            save(species, target);
        }
        return species;
    }

    /**
     * Returns an IDEXX species lookup.
     *
     * @param code the species code
     * @param name the species name
     * @return the lookup
     */
    protected Lookup getIDEXXSpecies(String code, String name) {
        return TestHelper.getLookup(IDEXX_SPECIES, code, name, true);
    }
}
