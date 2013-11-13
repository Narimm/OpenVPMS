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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceTestCase;
import org.openvpms.component.business.service.lookup.LookupUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Abstract base class for {@link IMObjectBean} tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class AbstractIMObjectBeanTestCase extends ArchetypeServiceTestCase {

    /**
     * Patient owner entity relationship.
     */
    public static final String OWNER = "entityRelationship.patientOwner";

    /**
     * Patient location entity relationship.
     */
    public static final String LOCATION = "entityRelationship.patientLocation";

    /**
     * Verifies that two lists of objects match.
     *
     * @param actual   the actual result
     * @param expected the expected result
     */
    protected <T> void checkEquals(List<T> actual, T... expected) {
        assertEquals(expected.length, actual.size());
        for (T e : expected) {
            boolean found = false;
            for (T a : actual) {
                if (e.equals(a)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Object not found: " + e, found);
        }
    }

    /**
     * Verifies that objects are in the expected order.
     *
     * @param objects  the actual objects
     * @param expected the expected object, in order
     */
    protected <T extends IMObject> void checkOrder(List<T> objects, IMObject... expected) {
        assertEquals(objects.size(), expected.length);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(objects.get(i), expected[i]);
        }
    }

    /**
     * Helper to create a customer.
     *
     * @return a new customer
     */
    protected Party createCustomer() {
        Party customer = (Party) create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("title", "MR");
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "Bar");
        bean.save();
        return customer;
    }

    /**
     * Helper to create a patient.
     *
     * @return a new patient
     */
    protected Party createPatient() {
        Party patient = (Party) create("party.patientpet");
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("name", "Fido");
        Lookup canine = LookupUtil.getLookup(getArchetypeService(), "lookup.species", "CANINE");
        bean.setValue("species", canine.getCode());
        bean.save();
        return patient;
    }

    /**
     * Helper to add an owner relationship.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return a new owner relationship
     */
    protected EntityRelationship addOwnerRelationship(Party customer, Party patient) {
        EntityRelationship relationship = (EntityRelationship) create(OWNER);
        addRelationship(customer, patient, relationship);
        return relationship;
    }

    /**
     * Helper to add a location relationship.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return a new location relationship
     */
    protected EntityRelationship addLocationRelationship(Party customer, Party patient) {
        EntityRelationship relationship = (EntityRelationship) create(LOCATION);
        addRelationship(customer, patient, relationship);
        return relationship;
    }

    /**
     * Helper to add a relationship.
     *
     * @param source       the customer
     * @param target       the patient
     * @param relationship the relationship to add
     */
    protected void addRelationship(Party source, Party target, EntityRelationship relationship) {
        relationship.setSource(source.getObjectReference());
        relationship.setTarget(target.getObjectReference());
        source.addEntityRelationship(relationship);
        target.addEntityRelationship(relationship);
    }

}
