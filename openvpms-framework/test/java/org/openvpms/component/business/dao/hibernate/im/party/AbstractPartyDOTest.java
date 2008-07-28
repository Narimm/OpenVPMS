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
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDOHelper;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Base class for persistent party test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractPartyDOTest extends HibernateInfoModelTestCase {

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
     * Creates a new contact.
     *
     * @return a new contact
     */
    protected ContactDO createContact() {
        ContactDO contact = new ContactDOImpl();
        contact.setArchetypeId(CONTACT);
        contact.setDetails(createSimpleAttributeMap());
        return contact;
    }

    /**
     * Creates a classification lookup with the specified code.
     *
     * @param code the code of the classification
     * @return a new classification
     */
    protected LookupDO createClassification(String code) {
        return LookupDOHelper.createLookup("lookup.classification", code);
    }

    /**
     * Creates a person.
     *
     * @return a new person
     */
    protected PartyDO createPerson() {
        PartyDO party = new PartyDOImpl(PERSON);
        party.setName("Foo Bar");
        party.setDescription("A person");
        party.setDetails(createSimpleAttributeMap());
        return party;
    }

    /**
     * Creates a patient.
     *
     * @return a new patient
     */
    protected PartyDO createPatient() {
        PartyDO party = new PartyDOImpl(PATIENT);
        party.setName("Spot");
        party.setDescription("A patient");
        party.setDetails(createSimpleAttributeMap());
        return party;
    }

}
