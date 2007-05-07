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

package org.openvpms.report;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.List;


/**
 * Abstract base class for tests using the archetype service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ArchetypeServiceTest extends
                                           AbstractDependencyInjectionSpringContextTests {

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Helper to create a new object.
     *
     * @param shortName the archetype short name
     * @return the new object
     */
    protected IMObject create(String shortName) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
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
        IMObject contact = null;
        List<IMObject> contacts = bean.getValues("contacts");
        for (IMObject c : contacts) {
            if (TypeHelper.isA(c, "contact.location")) {
                contact = c;
                break;
            }
        }
        assertNotNull(contact);
        IMObjectBean contactBean = new IMObjectBean(contact);
        contactBean.setValue("address", "1234 Foo St");
        contactBean.setValue("suburb", "Melbourne");
        contactBean.setValue("state", "VIC");
        contactBean.setValue("postcode", "3001");
        contactBean.setValue("preferred", "true");
        bean.addValue("contacts", contactBean.getObject());
        bean.save();
        return (Party) bean.getObject();
    }
}
