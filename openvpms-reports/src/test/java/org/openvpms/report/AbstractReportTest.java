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

package org.openvpms.report;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;


/**
 * Abstract base class for report tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractReportTest extends ArchetypeServiceTest {

    /**
     * The document handlers.
     */
    @Autowired
    protected DocumentHandlers handlers;

    /**
     * Helper to load a document from a file.
     *
     * @param path     the file path
     * @param mimeType the mime type
     * @return a new document
     */
    protected Document getDocument(String path, String mimeType) {
        File file = new File(path);
        return DocumentHelper.create(file, mimeType, handlers);
    }

    /**
     * Returns the document handlers.
     *
     * @return the document handlers
     */
    protected DocumentHandlers getHandlers() {
        return handlers;
    }

    /**
     * Helper to create a new object wrapped in a bean.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    protected IMObjectBean createBean(String shortName) {
        return new IMObjectBean(create(shortName));
    }

    /**
     * Helper to create a new act wrapped in a bean.
     *
     * @param shortName the act short name
     * @return a new act
     */
    protected ActBean createAct(String shortName) {
        return new ActBean((Act) create(shortName));
    }

    /**
     * Helper to create and save a new customer with firstName 'J', lastName
     * 'Zoo', address '1234 Foo St', suburb 'Melbourne' and postcode '3001'.
     *
     * @return a new customer
     */
    protected Party createCustomer() {
        return createCustomer("J", "Zoo");
    }

    /**
     * Helper to create and save a new customer with the specified names,
     * address '1234 Foo St', suburb 'Melbourne' and postcode '3001'.
     *
     * @param firstName the first name
     * @param lastName  the last name
     * @return a new customer
     */
    protected Party createCustomer(String firstName, String lastName) {
        IMObjectBean bean = createBean("party.customerperson");
        bean.setValue("firstName", firstName);
        bean.setValue("lastName", lastName);
        IMObjectBean contactBean = createBean("contact.location");
        contactBean.setValue("address", "1234 Foo St");
        Lookup state = TestHelper.getLookup("lookup.state", "VIC", "VIC", true);
        Lookup suburb = TestHelper.getLookup("lookup.suburb", "MELBOURNE",
                                             "Melbourne", state, "lookupRelationship.stateSuburb");
        contactBean.setValue("suburb", suburb.getCode());
        contactBean.setValue("state", state.getCode());
        contactBean.setValue("postcode", "3001");
        contactBean.setValue("preferred", "true");
        bean.addValue("contacts", contactBean.getObject());
        bean.save();
        return (Party) bean.getObject();
    }

}
