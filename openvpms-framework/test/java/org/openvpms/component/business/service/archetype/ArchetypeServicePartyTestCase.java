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
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test that ability to create and query on parties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
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

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServicePartyTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServicePartyTestCase() {
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
     * Test the creation of a simple contact with a contact classification
     */
    @SuppressWarnings("unchecked")
    public void testSimplePartyWithContactCreation()
    throws Exception {
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
                        + "where contact.archetypeId.entityName = :entityName "
                        + "and contact.archetypeId.concept = :concept "
                        + "and classification.name = :classification");
        query.setParameter("entityName", "contact");
        query.setParameter("concept", "location");
        query.setParameter("classification", "email");
        query.list();
    }

    /**
     * Create a person with the specified title, firstName and LastName
     *
     * @param title
     * @param firstName
     * @param lastName
     *
     * @return Person
     */
    public Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party)service.create("person.person");
        person.getDetails().setAttribute("lastName", lastName);
        person.getDetails().setAttribute("firstName", firstName);
        person.getDetails().setAttribute("title", title);

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

        contact.getDetails().setAttribute("address", "kalulu rd");
        contact.getDetails().setAttribute("suburb", "Belgrave");
        contact.getDetails().setAttribute("postcode", "3160");
        contact.getDetails().setAttribute("state", "VIC");
        contact.getDetails().setAttribute("country", "AU");
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
        Lookup result = (Lookup) service.create("lookup.contactPurpose");
        result.setCode(code);
        return result;
    }
}
