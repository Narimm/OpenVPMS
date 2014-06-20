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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.doc;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DocumentTemplate} class.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateTestCase extends ArchetypeServiceTest {

    /**
     * Tests the default values of a new document template.
     */
    @Test
    public void testDefaults() {
        Lookup reportType = TestHelper.getLookup("lookup.reportType", "OTHER");
        reportType.setDefaultLookup(true);
        save(reportType);

        Entity entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplate template = new DocumentTemplate(entity, getArchetypeService());

        assertNull(template.getName());
        assertNull(template.getDescription());
        assertTrue(template.isActive());
        assertNull(template.getArchetype());
        assertEquals("0", template.getUserLevel());
        assertEquals(reportType.getCode(), template.getReportType());
        assertEquals(DocumentTemplate.PrintMode.CHECK_OUT, template.getPrintMode());
        assertNull(template.getPaperSize());
        assertEquals(DocumentTemplate.PORTRAIT, template.getOrientation());
        assertEquals(1, template.getCopies());
        checkEquals(BigDecimal.ZERO, template.getPaperHeight());
        checkEquals(BigDecimal.ZERO, template.getPaperWidth());
        assertEquals(DocumentTemplate.MM, template.getPaperUnits());
        assertNull(template.getEmailSubject());
        assertNull(template.getEmailText());
        assertNull(template.getMediaSize());
        assertEquals(OrientationRequested.PORTRAIT, template.getOrientationRequested());
        assertTrue(template.getPrinters().isEmpty());
        assertNull(template.getFileNameExpression());
    }

    /**
     * Tests the {@link DocumentTemplate} setters and getters.
     */
    @Test
    public void testAccessors() {
        Entity entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplate template = new DocumentTemplate(entity, getArchetypeService());

        Lookup reportType = TestHelper.getLookup("lookup.reportType", "XX_REPORT_TYPE");
        BigDecimal height = new BigDecimal("10.00");
        BigDecimal width = new BigDecimal("5.00");

        save(reportType);

        template.setName("test name");
        template.setDescription("test description");
        template.setActive(false);
        template.setArchetype("REPORT");
        template.setUserLevel("1");
        template.setReportType(reportType.getCode());
        template.setPrintMode(DocumentTemplate.PrintMode.IMMEDIATE);
        template.setPaperSize(DocumentTemplate.A5);
        template.setOrientation(DocumentTemplate.LANDSCAPE);
        template.setCopies(5);
        template.setPaperHeight(height);
        template.setPaperWidth(width);
        template.setPaperUnits(DocumentTemplate.MM);
        template.setEmailSubject("test subject");
        template.setEmailText("test text");
        template.save();

        template = new DocumentTemplate(get(entity), getArchetypeService());
        assertEquals("test name", template.getName());
        assertEquals("test description", template.getDescription());
        assertFalse(template.isActive());
        assertEquals("REPORT", template.getArchetype());
        assertEquals("1", template.getUserLevel());
        assertEquals(reportType.getCode(), template.getReportType());
        assertEquals(DocumentTemplate.PrintMode.IMMEDIATE, template.getPrintMode());
        assertEquals(DocumentTemplate.A5, template.getPaperSize());
        assertEquals(DocumentTemplate.LANDSCAPE, template.getOrientation());
        assertEquals(5, template.getCopies());
        checkEquals(height, template.getPaperHeight());
        checkEquals(width, template.getPaperWidth());
        assertEquals(DocumentTemplate.MM, template.getPaperUnits());
        assertEquals("test subject", template.getEmailSubject());
        assertEquals("test text", template.getEmailText());
        assertEquals(MediaSizeName.ISO_A5, template.getMediaSize());
        assertEquals(OrientationRequested.LANDSCAPE, template.getOrientationRequested());
    }

    /**
     * Tests the various {@link DocumentTemplate} printer methods.
     */
    @Test
    public void testGetPrinter() {
        Entity entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        DocumentTemplate template = new DocumentTemplate(entity, getArchetypeService());
        Party location1 = TestHelper.createLocation();
        Party location2 = TestHelper.createLocation();
        Party practice = TestHelper.getPractice();

        DocumentTemplatePrinter location1Printer = template.addPrinter(location1);
        DocumentTemplatePrinter practicePrinter = template.addPrinter(practice);
        assertEquals(location1Printer, template.getPrinter(location1));
        assertEquals(practicePrinter, template.getPrinter(practice));
        assertNull(template.getPrinter(location2));

        assertEquals(2, template.getPrinters().size());
        assertTrue(template.getPrinters().contains(location1Printer));
        assertTrue(template.getPrinters().contains(practicePrinter));
    }

    /**
     * Tests the {@link DocumentTemplate#getFileNameExpression()} method.
     */
    @Test
    public void testGetFileNameExpression() {
        Entity entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        Lookup lookup = (Lookup) create(DocumentArchetypes.FILE_NAME_FORMAT);

        DocumentTemplate template = new DocumentTemplate(entity, getArchetypeService());
        assertNull(template.getFileNameExpression());

        IMObjectBean bean = new IMObjectBean(lookup);
        String expression = "concat($file, ' - ', date:format(java.util.Date.new(), 'd MMM yyyy'))";
        bean.setValue("expression", expression);
        entity.addClassification(lookup);

        assertEquals(expression, template.getFileNameExpression());
    }
}
