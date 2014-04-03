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

package org.openvpms.web.component.im.report;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_FORM;


/**
 * Tests the {@link ContextDocumentTemplateLocator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ContextDocumentTemplateLocatorTestCase extends AbstractAppTest {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The practice location.
     */
    private Party location;

    /**
     * The first template.
     */
    private Entity template1;

    /**
     * The second template.
     */
    private Entity template2;

    /**
     * The default template, if there are no templates associated with the location nor practice.
     */
    private DocumentTemplate defaultTemplate;

    /**
     * Tests templates returned when the
     * {@link ContextDocumentTemplateLocator#ContextDocumentTemplateLocator(IMObject, Context)} constructor is used.
     */
    @Test
    public void testObjectContext() {
        IMObject object = create(DOCUMENT_FORM);

        // create an empty context, and verify that the default template is returned
        Context context1 = createContext(null, null);
        DocumentTemplateLocator locator1 = new ContextDocumentTemplateLocator(object, context1);
        checkTemplate(defaultTemplate, locator1);

        // create a context with the practice set, and verify template1 (associated with practice) is returned
        Context context2 = createContext(practice, null);
        ContextDocumentTemplateLocator locator2 = new ContextDocumentTemplateLocator(object, context2);
        checkTemplate(template1, locator2);

        // create a context with the location set, and verify template2 (associated with location) is returned
        Context context3 = createContext(null, location);
        DocumentTemplateLocator locator3 = new ContextDocumentTemplateLocator(object, context3);
        checkTemplate(template2, locator3);

        // create a context with both practice and location set, and verify template2 (associated with location) is
        // returned
        Context context4 = createContext(practice, location);
        DocumentTemplateLocator locator4 = new ContextDocumentTemplateLocator(object, context4);
        checkTemplate(template2, locator4);
    }

    /**
     * Tests templates returned when the
     * {@link ContextDocumentTemplateLocator#ContextDocumentTemplateLocator(String, Context)} constructor is used.
     */
    @Test
    public void testShortNameContext() {
        Context context1 = createContext(null, null);
        DocumentTemplateLocator locator1 = new ContextDocumentTemplateLocator(DOCUMENT_FORM, context1);
        checkTemplate(defaultTemplate, locator1);

        Context context2 = createContext(practice, null);
        DocumentTemplateLocator locator2 = new ContextDocumentTemplateLocator(DOCUMENT_FORM, context2);
        checkTemplate(template1, locator2);

        Context context3 = createContext(null, location);
        DocumentTemplateLocator locator3 = new ContextDocumentTemplateLocator(DOCUMENT_FORM, context3);
        checkTemplate(template2, locator3);

        Context context4 = createContext(practice, location);
        DocumentTemplateLocator locator4 = new ContextDocumentTemplateLocator(DOCUMENT_FORM, context4);
        checkTemplate(template2, locator4);
    }

    /**
     * Tests templates returned when the
     * {@link ContextDocumentTemplateLocator#ContextDocumentTemplateLocator(DocumentTemplate, IMObject, Context)}
     * constructor is used.
     */
    @Test
    public void testTemplateObjectContext() {
        IMObject object = create(DOCUMENT_FORM);
        DocumentTemplate template3 = createDocumentTemplate(DOCUMENT_FORM);

        Context context1 = createContext(null, null);
        DocumentTemplateLocator locator1 = new ContextDocumentTemplateLocator(template3, object, context1);
        assertEquals(template3, locator1.getTemplate());

        Context context2 = createContext(practice, null);
        DocumentTemplateLocator locator2 = new ContextDocumentTemplateLocator(null, object, context2);
        checkTemplate(template1, locator2);
    }

    /**
     * Tests templates returned when the
     * {@link ContextDocumentTemplateLocator#ContextDocumentTemplateLocator(DocumentTemplate, String, Context)}
     * constructor is used.
     */
    @Test
    public void testTemplateShortNameContext() {
        DocumentTemplate template3 = createDocumentTemplate(DOCUMENT_FORM);

        Context empty = createContext(null, null);
        DocumentTemplateLocator locator1 = new ContextDocumentTemplateLocator(template3, DOCUMENT_FORM, empty);
        assertEquals(template3, locator1.getTemplate());

        DocumentTemplateLocator locator2 = new ContextDocumentTemplateLocator(null, DOCUMENT_FORM, empty);
        checkTemplate(defaultTemplate, locator2);

        Context context = createContext(null, location);
        DocumentTemplateLocator locator3 = new ContextDocumentTemplateLocator(null, DOCUMENT_FORM, context);
        checkTemplate(template2, locator3);
    }

    /**
     * Verifies that inactive templates aren't returned.
     */
    @Test
    public void testInactiveTemplate() {
        // when both practice and location set, the location template has precedence
        Context context = createContext(practice, location);
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(DOCUMENT_FORM, context);
        checkTemplate(template2, locator);

        // mark the location template inactive, and verify the practice template is returned
        template2.setActive(false);
        save(template2);
        checkTemplate(template1, locator);

        template1.setActive(false);
        save(template1);

        // verify that the default template is returned, if one is present
        if (defaultTemplate != null) {
            if (!ObjectUtils.equals(defaultTemplate, template1) && !ObjectUtils.equals(defaultTemplate, template2)) {
                checkTemplate(defaultTemplate, locator);
                defaultTemplate.setActive(false);
                defaultTemplate.save();

                DocumentTemplate newDefaultTemplate = getDefaultTemplate(DOCUMENT_FORM);
                if (newDefaultTemplate != null) {
                    assertTrue(newDefaultTemplate.isActive());
                }
                checkTemplate(newDefaultTemplate, locator);
                if (defaultTemplate != null) {
                    assertFalse(ObjectUtils.equals(defaultTemplate, newDefaultTemplate));
                }
            }
        } else {
            assertNull(locator.getTemplate());
        }
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        practice = TestHelper.getPractice();
        EntityBean bean = new EntityBean(practice);
        for (EntityRelationship r : bean.getNodeRelationships("templates")) {
            bean.removeRelationship(r);
        }
        location = TestHelper.createLocation();
        template1 = createTemplate(DOCUMENT_FORM);
        template2 = createTemplate(DOCUMENT_FORM);

        addRelationship(practice, template1); // associate template1 with the practice
        addRelationship(location, template2); // associate template2 with the location
        defaultTemplate = getDefaultTemplate(DOCUMENT_FORM);
    }

    /**
     * Checks the template returned by the locator against the expected template.
     *
     * @param expected the expected template. May be <tt>null</tt>
     * @param locator  the locator to use
     */
    private void checkTemplate(Entity expected, DocumentTemplateLocator locator) {
        DocumentTemplate template = null;
        if (expected != null) {
            template = new DocumentTemplate(expected, getArchetypeService());
        }
        checkTemplate(template, locator);
    }

    /**
     * Checks the template returned by the locator against the expected template.
     *
     * @param expected the expected template. May be <tt>null</tt>
     * @param locator  the locator to use
     */
    private void checkTemplate(DocumentTemplate expected, DocumentTemplateLocator locator) {
        assertEquals(expected, locator.getTemplate());
    }

    /**
     * Creates and saves a new template for the given archetype short name.
     *
     * @param shortName the archetype short name
     * @return a new template
     */
    private Entity createTemplate(String shortName) {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("name", "XTestTemplate-" + System.currentTimeMillis());
        bean.setValue("archetype", shortName);
        bean.save();
        return template;
    }

    /**
     * Creates and saves a new template for the given archetype short name.
     *
     * @param shortName the archetype short name
     * @return a new template
     */
    private DocumentTemplate createDocumentTemplate(String shortName) {
        return new DocumentTemplate(createTemplate(shortName), getArchetypeService());
    }

    /**
     * Adds a relationship between a location/practice and a template.
     *
     * @param organisation the location or practice
     * @param template     the template
     * @return the new relationship
     */
    private EntityRelationship addRelationship(Party organisation, Entity template) {
        EntityBean bean = new EntityBean(template);
        EntityRelationship relationship =
                bean.addRelationship("entityRelationship.documentTemplatePrinter", organisation);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("printerName", "test");
        return relationship;
    }

    /**
     * Creates a new context.
     *
     * @param practice the practice. May be <tt>null</tt>
     * @param location the location. May be <tt>null</tt>
     * @return a new context
     */
    private Context createContext(Party practice, Party location) {
        Context context = new LocalContext();
        context.setPractice(practice);
        context.setLocation(location);
        return context;
    }

    /**
     * Returns the default template for a given archetype short name.
     *
     * @param shortName the short name
     * @return the default template, or <tt>null</tt> if none is found
     */
    private DocumentTemplate getDefaultTemplate(String shortName) {
        TemplateHelper helper = new TemplateHelper(ServiceHelper.getArchetypeService());
        return helper.getDocumentTemplate(shortName);
    }

}
