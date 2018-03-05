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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.print;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.report.ReportFactory;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentActAttachmentPrinter;
import org.openvpms.web.component.im.doc.DocumentActPrinter;
import org.openvpms.web.component.im.doc.FileNameFormatter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * {@link IMPrinterFactory} test case.
 *
 * @author Tim Anderson
 */
public class IMPrinterFactoryTestCase extends ArchetypeServiceTest {

    /**
     * The document template.
     */
    private Entity documentTemplate;

    /**
     * The report factory.
     */
    @Autowired
    ReportFactory reportFactory;

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules rules;

    /**
     * Verifies that a {@link IMObjectReportPrinter} is returned when no other
     * class is configured.
     */
    @Test
    public void testCreateDefaultPrinter() {
        checkCreate("party.customerperson", IMObjectReportPrinter.class);
    }

    /**
     * Verifies that a {@link DocumentActPrinter} is returned for
     * <em>act.*DocumentForm</em> and <em>act.*DocumentLetter</em>.
     */
    @Test
    public void testCreateDocumentActPrinter() {
        checkCreate("act.*DocumentForm", DocumentActPrinter.class);
        checkCreate("act.*DocumentLetter", DocumentActPrinter.class);
    }

    /**
     * Verifies that a {@link DocumentActAttachmentPrinter} is returned for
     * <em>act.*DocumentAttachment</em> and <em>act.*DocumentImage</em>
     */
    @Test
    public void testCreateDocumentActAttachmentPrinter() {
        checkCreate("act.*DocumentAttachment",
                    DocumentActAttachmentPrinter.class);
        checkCreate("act.*DocumentImage",
                    DocumentActAttachmentPrinter.class);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        documentTemplate = (Entity) create("entity.documentTemplate");
        documentTemplate.setName("XTestDocumentTemplate" + System.currentTimeMillis());
        save(documentTemplate);
    }

    /**
     * Verifies that the printer returned by {@link IMPrinterFactory#create}
     * matches that expected.
     *
     * @param shortName the archetype short name. May contain wildcards
     * @param type      the expected editor class
     */
    private void checkCreate(String shortName, Class type) {
        String[] shortNames = DescriptorHelper.getShortNames(shortName);
        assertTrue(shortNames.length > 0);
        for (String s : shortNames) {
            IMObject object = create(s);
            assertNotNull(object);
            checkCreate(object, type);
        }
    }

    /**
     * Verifies that the printer returned by {@link IMPrinterFactory#create}
     * matches that expected.
     *
     * @param object the object to print
     * @param type   the expected editor class
     */
    private void checkCreate(IMObject object, Class type) {
        if (object instanceof DocumentAct) {
            ActBean bean = new ActBean((Act) object);
            if (bean.hasNode("documentTemplate")) {
                save(documentTemplate);
                bean.addParticipation("participation.documentTemplate", documentTemplate);
            }
        }
        LocalContext context = new LocalContext();
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, context);
        FileNameFormatter formatter = new FileNameFormatter(getArchetypeService(), getLookupService(), rules);
        Converter converter = Mockito.mock(Converter.class);
        ReporterFactory reporterFactory = new ReporterFactory(reportFactory, formatter, getArchetypeService(),
                                                              getLookupService(), converter);
        IMPrinterFactory factory = new IMPrinterFactory(reporterFactory, getArchetypeService());
        IMPrinter printer = factory.create(object, locator, context);
        assertNotNull(printer);
        assertEquals(type, printer.getClass());
    }

}
