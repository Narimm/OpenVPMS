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


package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.hibernate.Session;
import org.hibernate.Transaction;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import static org.openvpms.component.business.dao.hibernate.im.lookup.LookupDOHelper.createLookup;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * Tests the {@link LookupRelationshipDOImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupRelationshipDOTestCase extends HibernateInfoModelTestCase {

    /**
     * The initial no. of lookups in the database.
     */
    private int lookups;

    /**
     * The initial no. of lookup relationships in the database.
     */
    private int relationships;


    /**
     * Test the creation lookup relationships between a country and states.
     */
    @Test
    public void testCreate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        LookupDO country = createLookup("lookup.country", "AU", "Australia");
        LookupDO state1 = createLookup("lookup.state", "VIC", "Victoria");
        LookupDO state2 = createLookup("lookup.state", "NSW",
                                       "New South Wales");
        LookupDO state3 = createLookup("lookup.state", "TAS", "Tasmania");
        LookupDO state4 = createLookup("lookup.state", "SA", "South Australia");
        session.save(state1);
        session.save(state2);
        session.save(state3);
        session.save(state4);
        session.save(country);

        // create the relationships
        LookupRelationshipDO relationship = createRelationship(country, state1);
        session.save(relationship);
        relationship = createRelationship(country, state2);
        session.save(relationship);
        relationship = createRelationship(country, state3);
        session.save(relationship);
        relationship = createRelationship(country, state4);
        session.save(relationship);
        tx.commit();

        // ensure that the correct number of rows have been inserted
        assertEquals(lookups + 5, count(LookupDOImpl.class));
        assertEquals(relationships + 4, count(LookupRelationshipDOImpl.class));
    }

    /**
     * Test the deletion of a lookup and all its relationships.
     */
    @Test
    public void testDelete() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        LookupDO country = createLookup("lookup.xcountry", "GRE", "Greece");
        LookupDO city1 = createLookup("lookup.xcity", "Athens", null);
        LookupDO city2 = createLookup("lookup.xcity", "Hellas", null);
        LookupDO city3 = createLookup("lookup.xcity", "Limnos", null);
        session.save(city1);
        session.save(city2);
        session.save(city3);

        // create the relationship
        country.addSourceLookupRelationship(createRelationship(country, city1));
        country.addSourceLookupRelationship(createRelationship(country, city2));
        country.addSourceLookupRelationship(createRelationship(country, city3));
        session.save(country);
        tx.commit();

        // ensure that the correct number of rows have been inserted
        assertEquals(lookups + 4, count(LookupDOImpl.class));
        assertEquals(relationships + 3, count(LookupRelationshipDOImpl.class));

        // now delete the entity and all its relationships
        tx = session.beginTransaction();
        LookupDO lookup = (LookupDO) session.load(LookupDOImpl.class,
                                                  country.getId());

        // iterate through all the relationships and remove them
        LookupRelationshipDO[] rels = lookup.getSourceLookupRelationships()
                .toArray(new LookupRelationshipDO[lookup.getSourceLookupRelationships().size()]);
        for (LookupRelationshipDO rel : rels) {
            lookup.removeSourceLookupRelationship(rel);
        }
        session.save(lookup);
        tx.commit();

        // ensure that the correct number of rows have been deleted
        assertEquals(lookups + 4, count(LookupDOImpl.class));
        assertEquals(relationships, count(LookupRelationshipDOImpl.class));
    }

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        lookups = count(LookupDOImpl.class);
        relationships = count(LookupRelationshipDOImpl.class);
    }

    /**
     * Creates a new lookup relationship.
     *
     * @param country the country
     * @param state   the state
     * @return a new lookup relationship
     */
    private LookupRelationshipDO createRelationship(LookupDO country,
                                                    LookupDO state) {

        LookupRelationshipDO result = new LookupRelationshipDOImpl();
        ArchetypeId type = new ArchetypeId(
                "lookupRelationship.countryState.1.0");
        result.setArchetypeId(type);
        result.setSource(country);
        result.setTarget(state);
        return result;
    }

}
