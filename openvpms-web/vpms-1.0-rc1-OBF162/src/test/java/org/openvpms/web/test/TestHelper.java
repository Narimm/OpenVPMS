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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.test;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.system.ServiceHelper;


/**
 * Test helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TestHelper {

    /**
     * Creates a new object.
     *
     * @param shortName the archetype short name
     * @return a new object, or <code>null</code> if the short name doesn't
     *         correspond to a valid archetype
     */
    public static IMObject create(String shortName) {
        return ServiceHelper.getArchetypeService().create(shortName);
    }

    /**
     * Creates a new customer.
     *
     * @return a new customer
     */
    public static Party createCustomer() {
        Party party = (Party) create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("firstName", "foo");
        bean.setValue("lastName", "xyz");
        Contact contact = (Contact) create("contact.phoneNumber");
        party.addContact(contact);
        return party;
    }

    /**
     * Creates a new patient.
     *
     * @return a new patient
     */
    public static Party createPatient() {
        Party party = (Party) create("party.patientpet");
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("name", "Fido");
        bean.setValue("species", "Canine");
        return party;
    }

    /**
     * Creates a new product.
     *
     * @return a new product
     */
    public static Product createProduct() {
        Product product = (Product) create("product.medication");
        product.setName("Flea powder");
        return product;
    }

    /**
     * Creates a new participation.
     *
     * @param shortName the participation short name
     * @param entity    the entity
     * @param act       the act
     * @return a new participation
     */
    public static Participation createParticipation(String shortName,
                                                    IMObject entity,
                                                    Act act) {
        Participation participation = (Participation) create(shortName);
        participation.setEntity(entity.getObjectReference());
        participation.setAct(act.getObjectReference());
        return participation;
    }

}
