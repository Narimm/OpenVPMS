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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.openoffice;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReport;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * {@link OpenOfficeIMObjectReport} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OpenOfficeIMObjectReportTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;


    /**
     * Tests reporting.
     */
    public void testReport() throws IOException {
        File file = new File("src/test/reports/act.customerEstimation.odt");
        Document doc = DocumentHelper.create(file, DocFormats.ODT_TYPE,
                                             handlers);

        IMObjectReport report = new OpenOfficeIMObjectReport(doc, handlers);
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setValue("startTime", java.sql.Date.valueOf("2006-08-04"));
        act.setValue("lowTotal", new BigDecimal("100"));
        act.setParticipant("participation.customer", party);

        Document result = report.generate(
                Arrays.asList((IMObject) act.getAct()),
                new String[]{DocFormats.ODT_TYPE});
        Map<String, String> fields = getFields(result);
        assertEquals("4/08/2006", fields.get("startTime"));  // @todo localise
        assertEquals("$100.00", fields.get("lowTotal"));
        assertEquals("J", fields.get("firstName"));
        assertEquals("Zoo", fields.get("lastName"));
        assertEquals("2.00", fields.get("expression"));
        assertEquals("1234 Foo St\nMelbourne VIC 3001",
                     fields.get("address"));
        assertEquals("Invalid node name: invalid", fields.get("invalid"));
    }

    /**
     * Returns the user fields in a document.
     *
     * @param document an OpenOffice document
     * @return a map of user field names and their corresponding values
     */
    private Map<String, String> getFields(Document document) {
        Map<String, String> fields = new HashMap<String, String>();
        OpenOfficeDocument doc = new OpenOfficeDocument(
                document, OpenOfficeHelper.getService(), handlers);
        for (String name : doc.getUserFieldNames()) {
            fields.put(name, doc.getUserField(name));
        }
        return fields;
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
        handlers = (DocumentHandlers) applicationContext.getBean(
                "documentHandlers");
        assertNotNull(service);
        assertNotNull(handlers);
    }

    /**
     * Helper to create a new object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    private IMObject create(String shortName) {
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }

    /**
     * Helper to create a new object wrapped in a bean.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    private IMObjectBean createBean(String shortName) {
        return new IMObjectBean(create(shortName));
    }

    /**
     * Helper to create a new act wrapped in a bean.
     *
     * @param shortName the act short name
     * @return a new act
     */
    private ActBean createAct(String shortName) {
        return new ActBean((Act) create(shortName));
    }

    /**
     * Helper to create and save a customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        IMObjectBean bean = createBean("party.customerperson");
        bean.setValue("firstName", "J");
        bean.setValue("lastName", "Zoo");
        IMObjectBean contact = createBean("contact.location");
        contact.setValue("address", "1234 Foo St");
        contact.setValue("suburb", "Melbourne");
        contact.setValue("postcode", "3001");
        contact.setValue("preferred", "true");
        bean.addValue("contacts", contact.getObject());
        bean.save();
        return (Party) bean.getObject();
    }

}
