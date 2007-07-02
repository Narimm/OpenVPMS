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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test that ability to create and query on parties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServicePartyTestCase extends
        AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;

    /**
     * A reference to the hibernate session factory
     */
    private SessionFactory sessionFactory;


    /**
     * Test the creation of a simple contact with a contact classification.
     */
    public void testSimplePartyWithContactCreation() throws Exception {
        Lookup classification = createLookup("EMAIL");
        service.save(classification);
        Lookup classification1 = createLookup("HOME");
        service.save(classification1);

        Party person = createPerson("MR", "Jim", "Alateras");
        person.addContact(createContact(classification));
        person.addContact(createContact(classification1));
        service.save(person);

        // try the hql query
        Query query = sessionFactory.openSession().createQuery(
                "select party from " + Party.class.getName()
                        + " as party inner join party.contacts as contact "
                        + "left outer join contact.classifications as "
                        + "classification "
                        + "where party.uid = :uid and "
                        + "contact.archetypeId.shortName = :shortName "
                        + "and classification.name = :classification");
        query.setParameter("uid", person.getUid());
        query.setParameter("shortName", "contact.location");
        query.setParameter("classification", classification.getName());
        assertEquals(1, query.list().size());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
                };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.service = (ArchetypeService)applicationContext.getBean(
                "archetypeService");
        this.sessionFactory = (SessionFactory)applicationContext.getBean(
                "sessionFactory");
    }

    /**
     * Create a person with the specified title, firstName and lastName.
     *
     * @param title the title
     * @param firstName the first name
     * @param lastName the last name
     * @return a new person
     */
    private Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party)service.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /**
     * Create a contact with the specified classification.
     *
     * @param classification the classification
     * @return a new contact
     */
    private Contact createContact(Lookup classification) {
        Contact contact = (Contact)service.create("contact.location");

        contact.getDetails().put("address", "kalulu rd");
        contact.getDetails().put("suburb", "Belgrave");
        contact.getDetails().put("postcode", "3160");
        contact.getDetails().put("state", "VIC");
        contact.getDetails().put("country", "AU");
        contact.addClassification(classification);

        return contact;
    }

    /**
     * Creates a lookup with the specified code.
     *
     * @param code the code of the lookup
     * @return a new lookup
     */
    private Lookup createLookup(String code) {
        return LookupUtil.createLookup("lookup.contactPurpose", code);
    }
}
