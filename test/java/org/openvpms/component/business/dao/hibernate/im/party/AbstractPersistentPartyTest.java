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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.party;

import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.LookupUtil;


/**
 * Base class for persitent party test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractPersistentPartyTest extends HibernateInfoModelTestCase {

    /**
     * Person archetype Id.
     */
    private static final ArchetypeId PERSON = new ArchetypeId(
            "party.person.1.0");

    /**
     * Patient archetype Id.
     */
    private static final ArchetypeId PATIENT = new ArchetypeId(
            "party.animalpet.1.0");

    /**
     * Contact archetype Id.
     */
    private static final ArchetypeId CONTACT = new ArchetypeId(
            "contact.contact.1.0");


    /**
     * Set up the test data file, if the file exists.
     *
     * @throws Exception propagate exception to the caller
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

    /**
     * Creates a simple contact.
     *
     * @return a new contact
     */
    protected Contact createContact() {
        Contact contact = new Contact();
        contact.setArchetypeId(CONTACT);
        contact.setDetails(createSimpleAttributeMap());
        return contact;
    }

    /**
     * Create a simple classification lookup with the specified code.
     *
     * @param code the code of the classification
     * @return a new classification
     */
    protected Lookup createClassification(String code) {
        return LookupUtil.createLookup("lookup.classification", code);
    }

    /**
     * Creates a simple person.
     *
     * @return a new person
     */
    protected Party createPerson() {
        Party party = new Party(PERSON, "person", "a person");
        party.setDetails(createSimpleAttributeMap());
        return party;
    }

    /**
     * Creates a simple patient.
     *
     * @return a new patient
     */
    protected Party createPatient() {
        Party party = new Party(PATIENT, "patient", "a patient");
        party.setDetails(createSimpleAttributeMap());
        return party;
    }

}
