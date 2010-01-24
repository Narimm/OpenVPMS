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

import static org.junit.Assert.assertNotNull;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;


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
     * @return a new object
     */
    public static IMObject create(String shortName) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        IMObject object = service.create(shortName);
        assertNotNull(object);
        return object;
    }

    /**
     * Saves one or more objects.
     *
     * @param objects the objects to save
     */
    public static <T extends IMObject> void save(T... objects) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        service.save(Arrays.asList(objects));
    }

    /**
     * Creates a new customer.
     *
     * @return a new customer
     */
    public static Party createCustomer() {
        return createCustomer(false);
    }

    /**
     * Creates a new customer.
     *
     * @param save if <tt>true</tt> save it
     * @return a new customer
     */
    public static Party createCustomer(boolean save) {
        return createCustomer("foo", "xyz", save);
    }

    /**
     * Creates a new customer.
     *
     * @param firstName the customer's first name
     * @param lastName  the customer's last name
     * @param save      if <tt>true</tt> save it
     * @return a new customer
     */
    public static Party createCustomer(String firstName, String lastName, boolean save) {
        Party party = (Party) create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("firstName", firstName);
        bean.setValue("lastName", lastName);
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        party.addContact(contact);
        if (save) {
            bean.save();
        }
        return party;
    }

    /**
     * Creates a new patient.
     *
     * @return a new patient
     */
    public static Party createPatient() {
        return createPatient(false);
    }

    /**
     * Creates a new patient.
     *
     * @param save if <tt>true</tt> save it
     * @return a new patient
     */
    public static Party createPatient(boolean save) {
        return createPatient("Fido", save);
    }

    /**
     * Creates a new patient.
     *
     * @param name the patient name
     * @param save if <tt>true</tt>, save it
     * @return a new patient
     */
    public static Party createPatient(String name, boolean save) {
        Party party = (Party) create("party.patientpet");
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("name", name);
        bean.setValue("species", "Canine");
        if (save) {
            bean.save();
        }
        return party;
    }

    /**
     * Creates a new product.
     *
     * @return a new product
     */
    public static Product createProduct() {
        return createProduct(false);
    }

    /**
     * Creates a new product.
     *
     * @param save if <tt>true</tt> save it
     * @return a new product
     */
    public static Product createProduct(boolean save) {
        Product product = (Product) create("product.medication");
        product.setName("Flea powder");
        if (save) {
            save(product);
        }
        return product;
    }
}
