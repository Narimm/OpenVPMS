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

package org.openvpms.component.business.service.archetype.descriptor;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentArchetypeDescriptorTestCase extends HibernateInfoModelTestCase {

    /**
     * main line
     * 
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentArchetypeDescriptorTestCase.class);
    }

    /**
     * Constructor for PersistentArchetypeDescriptorTestCase.
     * 
     * @param name
     */
    public PersistentArchetypeDescriptorTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test the creation of a simple archetype
     */
    public void testCreateSimpleArchetype() throws Exception {

        Session session = currentSession();
        Transaction tx = null;
        
        try {
            
            // get initial numbr of entries in address tabel
            int dcount = HibernateDescriptorUtil.getTableRowCount(session, "archetypeDescriptor");
            // execute the test
            tx = session.beginTransaction();
            ArchetypeDescriptor desc = createArchetypeDescriptor(
                    "party", "animal", "mypet", "1,0");
            
            session.save(desc);
            tx.commit();
            
            // ensure that there is still one more address
            int dcount1 = HibernateDescriptorUtil.getTableRowCount(session, "archetypeDescriptor");
            assertTrue(dcount1 == dcount + 1);
        } catch (Exception exception) { 
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }
    
    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        currentSession().flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

    /**
     * Create an archetype using the specified archetype id components.
     * 
     * @param rmName
     *            the reference model name
     * @param entityName
     *            the entity name
     * @param concept
     *            the concept that the archetype denotes
     * @param version
     *            the version of the archetype
     * @return ArchetypeDescriptor            
     */
    public ArchetypeDescriptor createArchetypeDescriptor(String rmName, String entityName,
        String concept, String version) {
        ArchetypeDescriptor desc =  new ArchetypeDescriptor(new ArchetypeId(
                "openvpms", rmName, entityName, concept, version));
        desc.setLatest(true);

        return desc;
    }
}
