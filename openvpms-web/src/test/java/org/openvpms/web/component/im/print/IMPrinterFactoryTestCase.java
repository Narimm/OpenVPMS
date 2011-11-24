/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.print;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentActAttachmentPrinter;
import org.openvpms.web.component.im.doc.DocumentActPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;


/**
 * {@link IMPrinterFactory} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-11-20 00:45:43Z $
 */
public class IMPrinterFactoryTestCase extends AbstractAppTest {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The document template.
     */
    private Entity documentTemplate;


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
    @Override
    public void setUp() {
        super.setUp();
        service = ServiceHelper.getArchetypeService();
        documentTemplate = (Entity) service.create("entity.documentTemplate");
        documentTemplate.setName("XTestDocumentTemplate"
                                 + System.currentTimeMillis());
        service.save(documentTemplate);
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
            IMObject object = service.create(s);
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
                service.save(documentTemplate);
                bean.addParticipation("participation.documentTemplate",
                                      documentTemplate);
            }
        }
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, new LocalContext());
        IMPrinter printer = IMPrinterFactory.create(object, locator);
        assertNotNull(printer);
        assertEquals(type, printer.getClass());
    }

}
