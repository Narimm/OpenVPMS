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

package org.openvpms.archetype.rules.export;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExportRulesTestCase extends ArchetypeServiceTest {

    private ExportRules rules;

    @Test
    public void testExporterGetFullName() {
        Act export = (Act) create(ExportArchetypes.EXPORT);
        ActBean bean = new ActBean(export);
        Party customer = TestHelper.createCustomer("Foo", "Bar", true);
        Party importer = TestHelper.createImporter(true);
        bean.addParticipation(ExportArchetypes.EXPORTER_PARTICIPATION, customer);
        bean.addParticipation(ExportArchetypes.IMPORTER_PARTICIPATION, importer);
        IMObjectBean customerbean = new IMObjectBean(customer);
        Lookup mr = TestHelper.getLookup("lookup.personTitle", "MR");
        customerbean.setValue("title", mr.getCode());
        customerbean.save();
        assertEquals("Mr Foo Bar", rules.getFullName(export));
        bean.setValue("exporterAgent", "Jenny Smith");
        assertEquals("Jenny Smith", rules.getFullName(export));
    }

    @Test
    public void testGetPatients() {
        Act export = (Act) create(ExportArchetypes.EXPORT);
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient(customer, true);
        EntityBean pbean = new EntityBean(patient);
        pbean.setValue("name", "Foo");
        pbean.save();
        Party patient2 = TestHelper.createPatient(customer, true);
        IMObjectBean pbean2 = new IMObjectBean(patient2);
        pbean2.setValue("name", "Bar");
        pbean2.save();
        ActBean bean = new ActBean(export);
        bean.addParticipation(ExportArchetypes.EXPORTER_PARTICIPATION, customer);
        bean.addParticipation(ExportArchetypes.PATIENT_PARTICIPATION, patient2);
        List<Party> list = rules.getPatients(export);
        assertEquals(1, list.size());
        assertEquals("Bar", list.get(0).getName());
        bean.addParticipation(ExportArchetypes.PATIENT_PARTICIPATION, patient);
        List<Party> list2 = rules.getPatients(export);
        assertEquals("Foo", list2.get(0).getName());
        assertEquals("Bar", list2.get(1).getName());
    }

    @Before
    public void setUp() throws Exception {
        rules = new ExportRules(getArchetypeService(), getLookupService());
        Lookup country1 = TestHelper.getLookup("lookup.country", "AU", "Australia", true);
        country1.setDefaultLookup(true);
        save(country1);
        Lookup state = TestHelper.getLookup("lookup.state", "VIC", "Victoria", country1, "lookupRelationship.countryState");
        state.setDefaultLookup(true);
        save(state);
    }
}