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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.archetype.rules.doc;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;

import javax.print.attribute.standard.MediaTray;


/**
 * Tests the {@link DocumentTemplatePrinter} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentTemplatePrinterTestCase extends ArchetypeServiceTest {

    /**
     * Tests the default values of a new document template printer.
     */
    @Test
    public void testDefaults() {
        EntityRelationship relationship = (EntityRelationship) create(DocumentArchetypes.DOCUMENT_TEMPLATE_PRINTER);
        DocumentTemplatePrinter printer = new DocumentTemplatePrinter(relationship, getArchetypeService());

        assertNull(printer.getPrinterName());
        assertNull(printer.getPaperTray());
        assertTrue(printer.getInteractive());
        assertFalse(printer.getPrintAtCheckout());
        assertNull(printer.getMediaTray());
        assertNull(printer.getTemplate());
        assertNull(printer.getTemplateRef());
        assertNull(printer.getLocation());
        assertNull(printer.getLocationRef());
    }

    /**
     * Tests the {@link DocumentTemplatePrinter} setters and getters.
     */
    @Test
    public void testAccessors() {
        Entity entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplate template = new DocumentTemplate(entity, getArchetypeService());
        Party location = TestHelper.createLocation();
        DocumentTemplatePrinter printer = template.addPrinter(location);
        template.setName("template name");

        printer.setPrinterName("name");
        printer.setPaperTray("TOP");
        printer.setInteractive(false);
        printer.setPrintAtCheckout(true);
        save(entity, location);

        // reload the relationship
        printer = new DocumentTemplatePrinter(get(printer.getRelationship()), getArchetypeService());

        assertEquals("name", printer.getPrinterName());
        assertEquals("TOP", printer.getPaperTray());
        assertFalse(printer.getInteractive());
        assertTrue(printer.getPrintAtCheckout());
        assertEquals(MediaTray.TOP, printer.getMediaTray());
        assertEquals(entity, printer.getTemplate());
        assertEquals(entity.getObjectReference(), printer.getTemplateRef());
        assertEquals(location, printer.getLocation());
        assertEquals(location.getObjectReference(), printer.getLocationRef());

    }

}