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


package org.openvpms.component.business.dao.hibernate.im.act;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Tests the {@link ActDO} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActDOTestCase extends HibernateInfoModelTestCase {

    /**
     * The initial no. of acts in the database
     */
    private int acts;


    /**
     * Test the creation of an act.
     */
    public void testCreate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ActDO act1 = createAct("act1");
        ActDO act2 = createAct("act2");
        ActDO act3 = createAct("act3");
        session.save(act1);
        session.save(act2);
        session.save(act3);
        tx.commit();

        // check the row count
        assertEquals(acts + 3, count(ActDO.class));
    }

    /**
     * Test the modification of an act.
     */
    public void testUpdate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ActDO act1 = createAct("mact1");
        ActDO act2 = createAct("mact2");
        session.save(act1);
        session.save(act2);
        tx.commit();

        // check the row count
        assertEquals(acts + 2, count(ActDO.class));

        // modify the act
        tx = session.beginTransaction();
        act1 = (ActDO) session.get(ActDO.class, act1.getId());
        assertNotNull(act1);
        String description = "my first act";
        act1.setDescription(description);
        session.saveOrUpdate(act1);
        tx.commit();

        act1 = reload(act1);
        assertEquals(description, act1.getDescription());

        assertEquals(acts + 2, count(ActDO.class));
    }

    /**
     * Tests the deletion of an act.
     */
    public void testDelete() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ActDO act1 = createAct("dact1");
        ActDO act2 = createAct("dact2");
        ActDO act3 = createAct("dact3");
        session.save(act1);
        session.save(act2);
        session.save(act3);
        tx.commit();

        // check the row count
        assertEquals(acts + 3, count(ActDO.class));

        // delete a couple of acts
        tx = session.beginTransaction();
        session.delete(act1);
        session.delete(act2);
        tx.commit();

        assertNull(reload(act1));
        assertNull(reload(act2));

        assertEquals(acts + 1, count(ActDO.class));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        acts = count(ActDO.class);
    }

    /**
     * Create a simple act
     *
     * @param name the name of the act
     * @return Act
     */
    private ActDO createAct(String name) {
        ActDO act = new ActDO();
        act.setArchetypeId(new ArchetypeId("act.simple.1.0"));
        act.setName(name);

        return act;
    }
}
