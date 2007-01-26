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

// spring-context
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Collection;
import java.util.List;

/** 
 * Test the persistence side of the archetype service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceDescriptorTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceDescriptorTestCase.class);
    
    /**
     * static to hold all session
     */
    public static final ThreadLocal<Session> session = new ThreadLocal<Session>();

    /**
     * A Hibernate session factory.
     */
    private SessionFactory sessionFactory;
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceDescriptorTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceDescriptorTestCase() {
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

    /**
     * Create and save a simple {@link ArchetypeDescriptor} using this service
     */
    public void  testCreateArchetypeDescriptor()
    throws Exception {
        ArchetypeDescriptor desc = (ArchetypeDescriptor)service.create(
                "descriptor.archetype");
        assertTrue(desc != null);
        
        desc.setName("openvpms-test-archetype.dummy.1.0");
        desc.setClassName(this.getClass().getName());
        desc.setDisplayName("archetype.dummy");
        desc.setDescription("Archetype Dummy v1.0");
        desc.setLatest(true);
        
        try {
            service.save(desc);
        } catch (ValidationException ex) {
            dumpValidationException(ex);
            throw ex;
        }
    }
    
    /**
     * Creation and save of an invalid {@link ArchetypeDescriptor} using this 
     * service
     */
    public void testCreateInvalidArchetypeDescriptor()
    throws Exception {
        ArchetypeDescriptor desc = (ArchetypeDescriptor)service.create(
                "descriptor.archetype");
        assertTrue(desc != null);
        
        desc.setName("openvpms-test-archetype.dummy.1.0");
        desc.setDisplayName("archetype.dummy");
        desc.setDescription("Archetype Dummy v1.0");
        desc.setLatest(true);
        
        try {
            service.save(desc);
            fail("No class name supplied so this should have failed");
        } catch (ValidationException ex) {
            // check that we have a single error
            assertTrue(ex.getErrors().size() == 1);
        }
    }
    
    /**
     * Create a simple archetype descriptor with node descriptor
     */
    public void testCreateArhetypeWithNodeDescriptor() 
    throws Exception { 
        ArchetypeDescriptor desc = createArchetypeDescriptor(
                "openvpms-test-archetype.dummy.1.0", "thisClass", 
                "archetype.dummy", true);
        
        NodeDescriptor ndesc = createNodeDescriptor("name", "/name", 
                "java.lang.String", 1, 1);
        desc.addNodeDescriptor(ndesc);
        
        try {
            service.save(desc);
        } catch (ValidationException ex) {
            dumpValidationException(ex);
            throw ex;
        }
        
        IMObject obj = ArchetypeQueryHelper.getByUid(service, 
                desc.getArchetypeId(), desc.getUid());
        assertTrue(obj != null);
        assertTrue(obj instanceof ArchetypeDescriptor);
        assertTrue(((ArchetypeDescriptor)obj).getNodeDescriptors().size() == 1);
        
        ArchetypeQuery query = new ArchetypeQuery("descriptor.archetype", false, true)
            .add(new NodeConstraint("type", desc.getName()));
        List<IMObject> objs = service.get(query).getResults();
        assertTrue(objs.size() > 0);
    }
    
    /**
     * Test that we can clone and save the cloned object in to the database
     */
    public void testCloneOnCreateArchetypeWithNodeDescriptor()
    throws Exception {
        ArchetypeDescriptor desc = createArchetypeDescriptor(
                "openvpms-test-archetype.dummy.1.0", "thisClass", 
                "archetype.dummy", true);
        
        NodeDescriptor ndesc = createNodeDescriptor("name", "/name", 
                "java.lang.String", 1, 1);
        desc.addNodeDescriptor(ndesc);
        
        try {
            service.save(desc);
        } catch (ValidationException ex) {
            dumpValidationException(ex);
            throw ex;
        }

        // clone the object and resave it
        ArchetypeDescriptor copy = (ArchetypeDescriptor)desc.clone();
        copy.setName("openvpms-test-archetype.dummy.2.0");
        copy.setClassName("thisClassAgain");
        assertFalse(copy.getName().equals(desc.getName()));
        assertFalse(copy.getClassName().equals(desc.getClassName()));
        service.save(copy);

        // retrieve the cloned and saved object and ensure that the values
        // are correct
        ArchetypeDescriptor obj = (ArchetypeDescriptor)ArchetypeQueryHelper
            .getByUid(service, copy.getArchetypeId(), copy.getUid());
        assertNotNull(obj);
        assertEquals(1, obj.getNodeDescriptors().size());
        assertEquals(copy.getName(), obj.getName());
        assertEquals(copy.getClassName(), obj.getClassName());
        
        // clone the object again 
        ArchetypeDescriptor copy2 = (ArchetypeDescriptor)copy.clone();
        for (NodeDescriptor toRemove : copy2.getAllNodeDescriptors()) {
            copy2.removeNodeDescriptor(toRemove);

            // NOTE: need to explicitly remove the children of cloned objects
            // as they don't contain hibernate peristent collection instances.
            service.remove(toRemove);
        }
        assertEquals(0, copy2.getNodeDescriptors().size());
        service.save(copy2);
        
         // retrieve the object again and check the info
        obj = (ArchetypeDescriptor)ArchetypeQueryHelper.getByUid(service,
                copy2.getArchetypeId(), copy2.getUid());
        assertNotNull(obj);
        assertEquals(0, obj.getNodeDescriptors().size());
        assertTrue(copy2.getName().equals(obj.getName()));
        assertTrue(copy2.getClassName().equals(obj.getClassName()));
    }
    
    /**
     * Test loading arhetypes from file system in to the database
     */
    public void testLoadingArchetypesFromXML()
    throws Exception {
        //deleteAllArchetypeDescriptors();
        for (ArchetypeDescriptor desc : service.getArchetypeDescriptors()) {
            try {
                ArchetypeQuery query = new ArchetypeQuery("descriptor.archetype", 
                        false, true)
                        .add(new NodeConstraint("type", desc.getName()));
                List<IMObject> results = service.get(query).getResults();
                // only add if the entry does not exist
                if (results == null || results.size() == 0) {
                    service.save(desc);
                }
            } catch (ValidationException ex) {
                dumpValidationException(ex);
                throw ex;
            } 
        }
    }
    /**
     * Test fix for OVPMS-194
     */
    public void testOVPMS194()
    throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("descriptor.archetype");
        NodeDescriptor ndesc = adesc.getNodeDescriptor("nodeDescriptors");
        if (ndesc.isCollection()) {
              Collection values = (Collection)ndesc.getValue(adesc);
              assertTrue(values != null);
       } else {
           fail("This should have been a collection");
       }
    }
    
    /**
     * Test for the improvement specified in OVPMS-261
     */
    public void testOVPMS261()
    throws Exception {
        List<String> shortNames = service.getArchetypeShortNames("entityRelationship.*", false);
        assertTrue(shortNames != null);
        assertTrue(shortNames.size() > 0);
        
        for (String shortName : shortNames) {
            if (!shortName.matches("entityRelationship\\..*")) {
                fail(shortName + " does not match expression entityRelationship.*");
            }
        }

        shortNames = service.getArchetypeShortNames("entityRelationship.an*", false);
        assertTrue(shortNames != null);
        assertTrue(shortNames.size() > 0);
        
        for (String shortName : shortNames) {
            if (!shortName.matches("entityRelationship\\.an.*")) {
                fail(shortName + " does not match expression entityRelationship.an*");
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean(
                "archetypeService");
        
        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addClass(ArchetypeDescriptor.class);
        config.addClass(NodeDescriptor.class);
        config.addClass(AssertionDescriptor.class);
        config.addClass(ActionTypeDescriptor.class);
        config.addClass(AssertionTypeDescriptor.class);
        this.sessionFactory = config.buildSessionFactory();
    }
    
    /**
     * Display the validation exeption using the error category
     * 
     * @param exception
     */
    private void dumpValidationException(ValidationException exception) {
        for (ValidationError error : exception.getErrors()) {
            logger.error("Error in node:" + error.getNodeName() + 
                    " msg:" + error.getErrorMessage());
        }
        
    }
    
    /**
     * Create a {@link ArchetypeDescriptor} with the specified parameters
     * 
     * @return ArchetypeDescriptor
     */
    private ArchetypeDescriptor createArchetypeDescriptor(String qName, 
            String className, String displayName, boolean latest) {
        ArchetypeDescriptor desc = (ArchetypeDescriptor)service.create(
            "descriptor.archetype");
        assertTrue(desc != null);

        desc.setName(qName);
        desc.setClassName(className);
        desc.setDisplayName(displayName);
        desc.setDescription(displayName);
        desc.setLatest(latest);
        
        return desc;
    }
    /**
     * Create a {@link NodeDescriptor} with the specified parameters
     * 
     * @return NodeDescriptor
     */
    private NodeDescriptor createNodeDescriptor(String name, String path, 
            String type, int minC, int maxC) {
        NodeDescriptor desc = (NodeDescriptor)service.create("descriptor.node");
        desc.setName(name);
        desc.setPath(path);
        desc.setType(type);
        desc.setMinCardinality(minC);
        desc.setMaxCardinality(maxC);
        
        return desc;
    }
    
    /**
     * Do a bulk delete of the archetypes but ensure that the cascade works
     */
    @SuppressWarnings("unused")
    private void deleteAllArchetypeDescriptors() 
    throws Exception {
        Session session = currentSession();
        Query query = session.createQuery("delete from " +
                ArchetypeDescriptor.class.getName());
        query.executeUpdate();
        
    }
    /**
     * @return Returns the sessionFactory.
     */
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Get the current hibernate session
     * 
     * @return Session
     * @throws Exception
     */
    public Session currentSession() throws Exception {
        Session s = session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = getSessionFactory().openSession();
            session.set(s);
        }
        return s;
    }

}
