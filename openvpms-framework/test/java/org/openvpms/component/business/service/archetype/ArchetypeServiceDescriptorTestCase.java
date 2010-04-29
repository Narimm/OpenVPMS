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

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Test the persistence side of the archetype service
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("archetype-service-appcontext.xml")
public class ArchetypeServiceDescriptorTestCase extends AbstractArchetypeServiceTest {

    /**
     * Create and save a simple {@link ArchetypeDescriptor} using this service
     */
    @Test
    public void testCreateArchetypeDescriptor() {
        ArchetypeDescriptor desc = (ArchetypeDescriptor) create("descriptor.archetype");
        String name = "archetype.dummy-" + System.currentTimeMillis() + ".1.0";
        desc.setName(name);
        desc.setClassName(getClass().getName());
        desc.setDisplayName("archetype.dummy");
        desc.setDescription("Archetype Dummy v1.0");
        desc.setLatest(true);

        save(desc);
    }

    /**
     * Creation and save of an invalid {@link ArchetypeDescriptor} using this
     * service
     */
    @Test
    public void testCreateInvalidArchetypeDescriptor() {
        ArchetypeDescriptor desc = (ArchetypeDescriptor) create(
                "descriptor.archetype");
        assertNotNull(desc);

        String name = "archetype.dummy-" + System.currentTimeMillis() + ".1.0";
        desc.setName(name);
        desc.setDisplayName("archetype.dummy");
        desc.setDescription("Archetype Dummy v1.0");
        desc.setLatest(true);

        try {
            save(desc);
            fail("No class name supplied so this should have failed");
        } catch (ValidationException ex) {
            // check that we have a single error
            assertTrue(ex.getErrors().size() == 1);
        }
    }

    /**
     * Create a simple archetype descriptor with node descriptor
     */
    @Test
    public void testCreateArchetypeWithNodeDescriptor() {
        String name = "archetype.dummy-" + System.currentTimeMillis() + ".1.0";
        ArchetypeDescriptor desc = createArchetypeDescriptor(
                name, Entity.class.getName(),
                "archetype.dummy", true);

        NodeDescriptor ndesc = createNodeDescriptor("name", "/name",
                                                    "java.lang.String", 1, 1);
        desc.addNodeDescriptor(ndesc);
        save(desc);
        ArchetypeDescriptor reloaded = get(desc);
        assertNotNull(reloaded);
        assertEquals(1, reloaded.getNodeDescriptors().size());

        ArchetypeQuery query = new ArchetypeQuery("descriptor.archetype", false, true)
                .add(new NodeConstraint("type", desc.getName()));
        List<IMObject> objs = get(query);
        assertTrue(objs.size() > 0);
    }

    /**
     * Tests the fix for OBF-174.
     */
    @Test
    public void testOBF174() {
        String name = "archetype.dummy-" + System.currentTimeMillis() + ".1.0";
        ArchetypeDescriptor desc = createArchetypeDescriptor(
                name, Entity.class.getName(),
                "archetype.dummy", true);

        NodeDescriptor ndesc = createNodeDescriptor("name", "/name",
                                                    "java.lang.String", 1, 1);
        AssertionDescriptor assertion = (AssertionDescriptor) create("assertion.regularExpression");
        IMObjectBean bean = new IMObjectBean(assertion);
        bean.setValue("expressionValue", "[a-zA-Z]+");
        bean.setValue("errorMessage", "invalid name");
        ndesc.addAssertionDescriptor(assertion);
        desc.addNodeDescriptor(ndesc);

        save(desc);
        save(ndesc);     // verify the node descriptor and assertion
        save(assertion); // descriptor can be re-saved

        ArchetypeDescriptor desc2 = get(desc);
        assertNotNull(desc);

        // check that the versions are the same
        assertEquals(desc.getVersion(), desc2.getVersion());
        assertEquals(1, desc2.getNodeDescriptors().size());
        NodeDescriptor ndesc2 = desc2.getAllNodeDescriptors().get(0);
        assertEquals(ndesc.getVersion(), ndesc2.getVersion());
        assertEquals(1, ndesc2.getAssertionDescriptors().size());
        AssertionDescriptor assertion2
                = ndesc2.getAssertionDescriptorsAsArray()[0];
        assertEquals(assertion.getVersion(), assertion2.getVersion());
    }

    /**
     * Test that we can clone and save the cloned object in to the database.
     * TODO - cloning is broken as it only does a shallow copy
     *
     * @throws Exception for any error
     */
    @Test
    public void testCloneOnCreateArchetypeWithNodeDescriptor() throws Exception {
        removeDescriptor("archetype.dummy.1.0");
        removeDescriptor("archetype.dummy.2.0");
        ArchetypeDescriptor desc = createArchetypeDescriptor(
                "archetype.dummy.1.0", getClass().getName(),
                "archetype.dummy", true);

        NodeDescriptor ndesc = createNodeDescriptor("name", "/name",
                                                    "java.lang.String", 1, 1);
        desc.addNodeDescriptor(ndesc);

        save(desc);

        // clone the object and resave it
        ArchetypeDescriptor copy = (ArchetypeDescriptor) desc.clone();
        copy.setName("archetype.dummy.2.0");
        copy.setClassName(Entity.class.getName());
        assertFalse(copy.getName().equals(desc.getName()));
        assertFalse(copy.getClassName().equals(desc.getClassName()));
        save(copy);

        // retrieve the cloned and saved object and ensure that the values
        // are correct
        ArchetypeDescriptor obj = get(copy);
        assertNotNull(obj);
        assertEquals(1, obj.getNodeDescriptors().size());
        assertEquals(copy.getName(), obj.getName());
        assertEquals(copy.getClassName(), obj.getClassName());

        // clone the object again 
        ArchetypeDescriptor copy2 = (ArchetypeDescriptor) copy.clone();
        save(copy2);

        // retrieve the object again and check the info
        obj = get(copy2);
        assertNotNull(obj);
        assertEquals(copy2.getName(), obj.getName());
        assertEquals(copy2.getClassName(), obj.getClassName());
    }

    /**
     * Test fix for OVPMS-194.
     */
    @Test
    public void testOVPMS194() {
        ArchetypeDescriptor adesc = getArchetypeDescriptor("descriptor.archetype");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("nodeDescriptors");
        if (ndesc.isCollection()) {
            Collection values = (Collection) ndesc.getValue(adesc);
            assertTrue(values != null);
        } else {
            fail("This should have been a collection");
        }
    }

    /**
     * Test for the improvement specified in OVPMS-261
     */
    @Test
    public void testOVPMS261() {
        IArchetypeService service = getArchetypeService();
        List<String> shortNames = service.getArchetypeShortNames("entityRelationship.*", false);
        assertTrue(shortNames.size() > 0);

        for (String shortName : shortNames) {
            if (!shortName.matches("entityRelationship\\..*")) {
                fail(shortName + " does not match expression entityRelationship.*");
            }
        }

        shortNames = service.getArchetypeShortNames("entityRelationship.an*", false);
        assertTrue(shortNames.size() > 0);

        for (String shortName : shortNames) {
            if (!shortName.matches("entityRelationship\\.an.*")) {
                fail(shortName + " does not match expression entityRelationship.an*");
            }
        }
    }

    /**
     * Verifies that multiple archetype descriptors can be saved via the
     * {@link IArchetypeService#save(Collection<IMObject>)} method.
     */
    @Test
    public void testSaveCollection() {
        String name1 = "archetype1.dummy" + System.currentTimeMillis() + ".1.0";
        String name2 = "archetype2.dummy" + System.currentTimeMillis() + ".1.0";

        ArchetypeDescriptor desc1 = createArchetypeDescriptor(
                name1, getClass().getName(), "archetype1.dummy", true);
        NodeDescriptor ndesc1 = createNodeDescriptor("name", "/name",
                                                     "java.lang.String", 1, 1);
        desc1.addNodeDescriptor(ndesc1);

        ArchetypeDescriptor desc2 = createArchetypeDescriptor(
                name2, getClass().getName(), "archetype2.dummy", true);
        NodeDescriptor ndesc2 = createNodeDescriptor("name", "/name",
                                                     "java.lang.String", 1, 1);
        desc2.addNodeDescriptor(ndesc2);

        // check the initial values of the ids
        assertEquals(-1, desc1.getId());
        assertEquals(-1, desc2.getId());

        // save the archetype descriptors
        Collection<ArchetypeDescriptor> col = Arrays.asList(desc1, desc2);
        save(col);

        // verify the ids have updated
        assertFalse(desc1.getId() == -1);
        assertFalse(desc2.getId() == -1);

        assertEquals(0, desc1.getVersion());
        assertEquals(0, desc2.getVersion());

        // verify the versions don't update until a change is made
        save(col);
        assertEquals(0, desc1.getVersion());
        assertEquals(0, desc2.getVersion());

        // make a change to each and re-save
        desc1.setDisplayName("changed");
        desc2.setDisplayName("changed");

        save(col);
        assertEquals(1, desc1.getVersion());
        assertEquals(1, desc2.getVersion());
    }

    /**
     * Verifies that an archetype descriptor can be replaced with one of
     * the same name in a transaction.
     */
    @Test
    public void testReplace() {
        String name = "archetype.dummy" + System.currentTimeMillis() + ".1.0";
        final ArchetypeDescriptor desc1 = createArchetypeDescriptor(
                name, getClass().getName(), "archetype.dummy", true);
        NodeDescriptor ndesc1 = createNodeDescriptor("name", "/name",
                                                     "java.lang.String", 1, 1);
        desc1.addNodeDescriptor(ndesc1);
        save(desc1);

        final ArchetypeDescriptor desc2 = createArchetypeDescriptor(
                name, getClass().getName(), "archetype.dummy", true);
        NodeDescriptor ndesc2 = createNodeDescriptor("name2", "/name",
                                                     "java.lang.String", 1, 1);
        // try and save desc2. Should fail.
        try {
            save(desc2);
            fail("Expected save with non-unique name to fail");
        } catch (Exception expected) {
        }

        desc2.addNodeDescriptor(ndesc2);

        // now remove desc1, and save desc2 in a transaction
        PlatformTransactionManager txnManager = (PlatformTransactionManager)
                applicationContext.getBean("txnManager");
        TransactionTemplate template = new TransactionTemplate(txnManager);
        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                remove(desc1);
                save(desc2);
                return null;
            }
        });

        // verify the original descriptor can't be retrieved
        assertNull(get(desc1));

        ArchetypeDescriptor reloaded = get(desc2);
        assertNotNull(reloaded.getNodeDescriptor("name2"));
    }


    /**
     * Creates a new archetype descriptor.
     *
     * @param qName       the qualified name
     * @param className   the class name
     * @param displayName the display name
     * @param latest      indicates if the descriptor is the latest instance
     * @return a new archetype descriptor
     */
    private ArchetypeDescriptor createArchetypeDescriptor(String qName, String className, String displayName,
                                                          boolean latest) {
        ArchetypeDescriptor desc = (ArchetypeDescriptor) create("descriptor.archetype");
        desc.setName(qName);
        desc.setClassName(className);
        desc.setDisplayName(displayName);
        desc.setDescription(displayName);
        desc.setLatest(latest);

        return desc;
    }

    /**
     * Removes any archetype descriptor with the specified qualified name.
     *
     * @param qName the qualified name
     */
    private void removeDescriptor(String qName) {
        ArchetypeQuery query = new ArchetypeQuery("descriptor.archetype", false, true);
        query.add(new NodeConstraint("type", qName));
        Iterator<IMObject> iter = new IMObjectQueryIterator<IMObject>(query);
        while (iter.hasNext()) {
            remove(iter.next());
        }
    }

    /**
     * Creates a new node descriptor.
     *
     * @param name the node name
     * @param path the path
     * @param type the type
     * @param minC the min cardinality
     * @param maxC the max cardinality
     * @return a new descriptor
     */
    private NodeDescriptor createNodeDescriptor(String name, String path,
                                                String type, int minC,
                                                int maxC) {
        NodeDescriptor desc = (NodeDescriptor) create(
                "descriptor.node");
        desc.setName(name);
        desc.setPath(path);
        desc.setType(type);
        desc.setMinCardinality(minC);
        desc.setMaxCardinality(maxC);

        return desc;
    }


}
