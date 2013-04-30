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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.macro.IMObjectVariables;
import org.openvpms.report.jasper.JRXMLDocumentHandler;
import org.openvpms.report.openoffice.OOBootstrapConnectionPool;
import org.openvpms.report.openoffice.OOSocketBootstrapService;
import org.openvpms.report.openoffice.OpenOfficeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ReportMacroRunnerTestCase}.
 *
 * @author Tim Anderson
 */
public class ReportMacroRunnerTestCase extends ArchetypeServiceTest {

    /**
     * The handlers for document serialisation.
     */
    private DocumentHandlers handlers;

    /**
     * The lookup service.
     */
    @Autowired
    ILookupService lookups;

    /**
     * The OpenOffice service starter.
     */
    private OOSocketBootstrapService bootstrapService;

    /**
     * The Jasper Report test report.
     */
    private static final String JASPER_REPORT = "report.jrxml";

    /**
     * The Word test report.
     */
    private static final String WORD_REPORT = "report.doc";

    /**
     * The OpenOffice test report.
     */
    private static final String OPEN_OFFICE_REPORT = "report.odt";


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        handlers = new DocumentHandlers();
        handlers.addDocumentHandler(new JRXMLDocumentHandler(getArchetypeService()));
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        if (bootstrapService != null) {
            bootstrapService.stop();
        }
    }

    /**
     * Verifies that a Jasper Report can be used as a report macro.
     */
    @Test
    public void testJasperReportMacro() {
        // creates a report
        Entity report = createReport(JASPER_REPORT);

        // create and run a report macro lookup that references the report
        String text = runMacro(report);

        // verify the macro output
        assertEquals("Foo Bar", text);
    }

    /**
     * Verifies that a Word document can be used as a report macro.
     */
    @Test
    public void testWordReport() {
        initOpenOffice();

        // create a report
        Entity report = createReport(WORD_REPORT);

        // create and run a report macro lookup that references the report
        String text = runMacro(report);

        // verify the macro output
        String expected = "First Name: Foo" + System.lineSeparator() + "Last Name: Bar";
        text = text.trim(); // remove extraneous whitespace
        assertEquals(expected, text);
    }

    /**
     * Verifies that an OpenOffice document can be used as a report macro.
     */
    @Test
    public void testOpenOfficeReport() {
        initOpenOffice();

        // creates a report
        Entity report = createReport(OPEN_OFFICE_REPORT);

        // create and run a report macro lookup that references the report
        String text = runMacro(report);

        // verify the macro output
        String expected = "First: Foo, Last: Bar";
        text = text.trim(); // remove extraneous whitespace
        assertEquals(expected, text);
    }

    /**
     * Runs a report macro supplying a $customer variable.
     *
     * @param report the entity.documentTemplate representing the report
     * @return the result of the macro
     */
    private String runMacro(Entity report) {
        // create a report macro lookup that references the report
        Lookup lookup = (Lookup) create(MacroArchetypes.REPORT_MACRO);
        IMObjectBean macroBean = new IMObjectBean(lookup);
        macroBean.setValue("report", report.getObjectReference());
        macroBean.setValue("expression", "$customer");

        // create a customer
        IArchetypeService service = getArchetypeService();
        Party customer = TestHelper.createCustomer("Foo", "Bar", false);
        IMObjectVariables variables = new IMObjectVariables(service, lookups);
        variables.add("customer", customer);

        // run the report macro against the customer
        ReportMacro macro = new ReportMacro(lookup, service);
        MacroContext context = new MacroContext(Collections.<String, Macro>emptyMap(), null, null, variables);
        ReportMacroRunner runner = new ReportMacroRunner(context, service, handlers);
        return runner.run(macro, "");
    }

    /**
     * Helper to create an <em>entity.documentTemplate</em>, <em>act.documentTemplate</em> and <em>document.other</em>
     * for the supplied report.
     *
     * @param report the report name
     * @return a new template
     */
    private Entity createReport(String report) {
        Entity entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplate template = new DocumentTemplate(entity, getArchetypeService());
        template.setName(report);
        template.setArchetype("REPORT");

        DocumentHandler documentHandler = handlers.get(report, null);
        Document doc = documentHandler.create(report, getClass().getResourceAsStream("/" + report), null, -1);

        DocumentAct act = (DocumentAct) create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        ActBean actBean = new ActBean(act);
        actBean.setValue("description", "Test macro");
        actBean.addNodeParticipation("template", entity);
        act.setDocument(doc.getObjectReference());

        save(act, entity, doc);

        return entity;
    }

    /**
     * Initialises OpenOffice.
     */
    private void initOpenOffice() {
        bootstrapService = new OOSocketBootstrapService(8100, false);
        OOBootstrapConnectionPool connectionPool = new OOBootstrapConnectionPool(bootstrapService);
        new OpenOfficeHelper(connectionPool, null);
    }
}
