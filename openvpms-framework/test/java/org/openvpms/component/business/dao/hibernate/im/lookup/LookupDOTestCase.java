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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.component.business.dao.hibernate.im.lookup.LookupDOHelper.createLookup;

/**
 * Tests the {@link LookupDOImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupDOTestCase extends HibernateInfoModelTestCase {

    /**
     * The initial no. of lookups in the database.
     */
    private int lookups;


    /**
     * Test the creation of lookups.
     */
    @Test
    public void testCreate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        LookupDO dog = createLookup("lookup.species", "DOG", "dog");
        session.save(dog);
        LookupDO cat = createLookup("lookup.species", "CAT", "cat");
        session.save(cat);
        LookupDO mouse = createLookup("lookup.species", "MOUSE", "mouse");
        session.save(mouse);
        LookupDO rabbit = createLookup("lookup.species", "RABBIT", "rabbit");
        session.save(rabbit);
        LookupDO cow = createLookup("lookup.species", "COW", "cow");
        session.save(cow);
        LookupDO donkey = createLookup("lookup.species", "DONKEY", "donkey");
        session.save(donkey);
        tx.commit();

        checkLookup(dog);
        checkLookup(cat);
        checkLookup(mouse);
        checkLookup(rabbit);
        checkLookup(cow);
        checkLookup(donkey);

        // ensure that there are 6 more lookups
        assertEquals(lookups + 6, count(LookupDOImpl.class));
    }

    /**
     * Test the creation, find and update of a lookup.
     */
    @Test
    public void testUpdate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        LookupDO lookup = LookupDOHelper.createLookup("lookup.breed", "JIMMY");
        session.save(lookup);
        tx.commit();

        // ensure that there is one more lookup
        assertEquals(lookups + 1, count(LookupDOImpl.class));

        // retrieve, update, and save
        tx = session.beginTransaction();
        lookup = (LookupDO) session.load(LookupDOImpl.class, lookup.getId());
        assertNotNull(lookup);
        lookup.setCode(lookup.getCode());
        session.update(lookup);
        tx.commit();

        // ensure that there is still one more lookup
        assertEquals(lookups + 1, count(LookupDOImpl.class));
    }

    /**
     * Test the creation and deletion of a simple lookup.
     */
    @Test
    public void testDelete() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        LookupDO lookup = createLookup("lookup.breed", "JOHHNY");
        session.save(lookup);
        tx.commit();

        // ensure that there is one more lookup
        assertEquals(lookups + 1, count(LookupDOImpl.class));

        // retrieve and delete
        tx = session.beginTransaction();
        lookup = (LookupDO) session.load(LookupDOImpl.class, lookup.getId());
        assertNotNull(lookup);
        session.delete(lookup);
        tx.commit();

        assertNull(reload(lookup));

        // check lookup count
        assertEquals(lookups, count(LookupDOImpl.class));
    }

    /**
     * Verifies that two lookups with the same code and short name are equal.
     */
    @Test
    public void testEquals() {
        ArchetypeId species = new ArchetypeId("lookup.species");
        ArchetypeId animal = new ArchetypeId("lookup.animal");

        LookupDO canine1 = new LookupDOImpl(species, "CANINE");
        LookupDO canine2 = new LookupDOImpl(species, "CANINE");
        LookupDO speciesFeline = new LookupDOImpl(species, "FELINE");
        LookupDO animalFeline = new LookupDOImpl(animal, "FELINE");

        assertTrue(canine1.equals(canine2));        // same archetype and code
        assertFalse(canine1.equals(speciesFeline)); // different code
        assertFalse(speciesFeline.equals(animalFeline)); // different archetype
    }

    /**
     * Verifies that a lookup with the same archetype and code as an existing
     * lookup cannot be saved.
     */
    @Test
    public void testDuplicate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        LookupDO lookup1 = LookupDOHelper.createLookup("lookup.species",
                                                       "CANINE");
        session.save(lookup1);
        tx.commit();

        // create a duplicate
        LookupDO lookup2 = new LookupDOImpl(lookup1.getArchetypeId(),
                                            lookup1.getCode());
        try {
            tx = session.beginTransaction();
            session.save(lookup2);
            tx.commit();
            fail("Expected duplicate lookup insertion to fail");
        } catch (Exception expected) {
            // do nothing
        }
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        lookups = count(LookupDOImpl.class);
    }

    /**
     * Reloads a lookup and verifies the archetype, code and name match that
     * expected.
     *
     * @param lookup the lookup
     */
    private void checkLookup(LookupDO lookup) {
        String code = lookup.getCode();
        String name = lookup.getName();
        String shortName = lookup.getArchetypeId().getShortName();
        lookup = reload(lookup);
        assertNotNull(lookup);

        assertEquals(shortName, lookup.getArchetypeId().getShortName());
        assertEquals(code, lookup.getCode());
        assertEquals(name, lookup.getName());
    }

}
