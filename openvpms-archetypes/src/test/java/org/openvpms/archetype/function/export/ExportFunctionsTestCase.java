/*
 * Copyright (c) 2015.
 *
 * Copy Charlton IT
 *
 * All rights reserved.
 */

package org.openvpms.archetype.function.export;

import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.function.party.PartyFunctions;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.export.ExportArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExportFunctionsTestCase extends ArchetypeServiceTest {


    @Test
    public void testGetImportCountryFullName() throws Exception {
        Act export = (Act) create(ExportArchetypes.EXPORT);
        JXPathContext ctx = createContext(export);
        assertEquals("Australia", ctx.getValue("export:getImportCountryFullName(.)"));
        assertEquals("Australia", ctx.getValue("export:getImportCountryFullName()"));
    }

    @Test
    public void testGetImporter() throws Exception {
        Act doc = (Act) create("act.exportDocumentAttachment");
        Act export = (Act) create(ExportArchetypes.EXPORT);
        ActBean bean = new ActBean(export);
        Party customer = TestHelper.createCustomer();
        ActBean docbean = new ActBean(doc);
        docbean.addParticipation(CustomerArchetypes.CUSTOMER_PARTICIPATION,customer);
        docbean.save();
        bean.addParticipation(ExportArchetypes.EXPORTER_PARTICIPATION, customer);
        bean.addRelationship("actRelationship.exportItem", doc);
        bean.save();
        Party importer = TestHelper.createImporter(true);
        JXPathContext ctx = createContext(doc);
        ActRelationship rel = bean.getRelationship("actRelationship.exportItem");
        assertEquals(rel, ctx.getValue("openvpms:get(.,'export')"));
        assertEquals(export, ctx.getValue("openvpms:get(.,'export.source')"));
        assertNull( ctx.getValue("openvpms:get(.,'export.source.importer')"));
        assertNull(ctx.getValue("export:getImporter(.)"));
        bean.addParticipation("participation.importer", importer);
        bean.save();
        assertEquals(importer, ctx.getValue("export:getImporter(.)"));
    }

    @Test
    public void testGetPatients() throws Exception {
        Act doc = (Act) create("act.exportDocumentAttachment");
        Act export = (Act) create(ExportArchetypes.EXPORT);
        ActBean bean = new ActBean(export);
        Party customer = TestHelper.createCustomer();
        ActBean docbean = new ActBean(doc);
        docbean.addParticipation(CustomerArchetypes.CUSTOMER_PARTICIPATION, customer);
        docbean.save();
        bean.addParticipation(ExportArchetypes.EXPORTER_PARTICIPATION, customer);
        bean.addRelationship("actRelationship.exportItem", doc);
        bean.save();
        JXPathContext ctx = createContext(doc);
        assertNull(ctx.getValue("export:getPatients(.)"));
        Party patient = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();
        bean.addParticipation(ExportArchetypes.PATIENT_PARTICIPATION, patient);
        bean.save();
        assertEquals(patient, ctx.getValue("export:getPatients(.)[1]"));
        bean.addParticipation(ExportArchetypes.PATIENT_PARTICIPATION, patient2);
        bean.save();
        assertEquals(patient, ctx.getValue("export:getPatients(.)[1]"));
        assertEquals(patient2, ctx.getValue("export:getPatients(.)[2]"));
    }

    private JXPathContext createContext(IMObject object) {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        ArchetypeServiceFunctions functions = new ArchetypeServiceFunctions(service, lookups);
        ExportFunctions exportFunctions = new ExportFunctions(service, lookups);
        PartyFunctions partyFunctions = new PartyFunctions(service, lookups, new PatientRules(null, getArchetypeService(), getLookupService()));
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(new ObjectFunctions(functions, "openvpms"));
        library.addFunctions(new ObjectFunctions(partyFunctions, "party"));
        library.addFunctions(new ObjectFunctions(exportFunctions, "export"));
        return JXPathHelper.newContext(object, library);
    }

    private Act createExportDoc(){
        Act doc = (Act) create("act.exportDocumentAttachment");
        Act export = (Act) create(ExportArchetypes.EXPORT);
        ActBean bean = new ActBean(export);
        Party customer = TestHelper.createCustomer();
        ActBean docbean = new ActBean(doc);
        docbean.addParticipation(CustomerArchetypes.CUSTOMER_PARTICIPATION, customer);
        docbean.save();
        bean.addParticipation(ExportArchetypes.EXPORTER_PARTICIPATION, customer);
        bean.addRelationship("actRelationship.exportItem", doc);
        bean.save();
        return bean.getAct();
    }

    @Before
    public void setup() throws Exception {
        Lookup country1 = TestHelper.getLookup("lookup.country", "AU", "Australia", true);
        country1.setDefaultLookup(true);
        save(country1);
        Lookup state = TestHelper.getLookup("lookup.state", "VIC", "Victoria", country1, "lookupRelationship.countryState");
        state.setDefaultLookup(true);
        save(state);
    }

}
