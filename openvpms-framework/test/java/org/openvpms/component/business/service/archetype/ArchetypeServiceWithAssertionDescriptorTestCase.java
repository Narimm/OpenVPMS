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

// java-core
import java.util.Hashtable;


// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheFS;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;

// openvpms-test-component
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test the management of assertions through the archetype service
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceWithAssertionDescriptorTestCase extends BaseTestCase {

    /**
     * Reference to the archetype service
     */
    private ArchetypeService service;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceWithAssertionDescriptorTestCase.class);
    }

    /**
     * Constructor for ArchetypeServiceTestCase.
     * 
     * @param name
     */
    public ArchetypeServiceWithAssertionDescriptorTestCase(String name) {
        super(name); 
    }

    /**
     * Test that we can successfully create all the archetypes loaded by the
     * service
     */
    public void testCreateDefaultObject() throws Exception {
        for (ArchetypeDescriptor descriptor : service
                .getArchetypeDescriptors()) {
            assertTrue("Creating " + descriptor.getName(), service
                    .create(descriptor.getType()) != null);
        }
    }

    /**
     * Test the creation of an archetypeRange assertion
     */
    public void testCreateArchetypeRange()
    throws Exception {
        AssertionDescriptor adesc = (AssertionDescriptor)service.create(
                "assertion.archetypeRange");
        assertTrue(adesc != null);
        PropertyMap pdesc = (PropertyMap)service.create(
                "assertion.archetypeRangeProperties");
        assertTrue(adesc != null);
        assertTrue(pdesc.getProperties().size() == 3);
        assertTrue(pdesc.getProperties().get("shortName") != null);
        
        ArchetypeDescriptor desc = service.getArchetypeDescriptor(
                adesc.getArchetypeId());
        assertTrue(desc != null);
        
        NodeDescriptor ndesc = desc.getNodeDescriptor("archetypes");
        assertTrue(ndesc != null); 
        
        ndesc.addChildToCollection(adesc, pdesc);
        assertTrue(adesc.getProperty("archetypes") != null);
        assertTrue(adesc.getProperty("archetypes") instanceof PropertyList);
        
        PropertyList archetypes = (PropertyList)adesc.getProperty("archetypes");
        assertTrue(archetypes.getProperties().size() == 1);
        for (NamedProperty archetype : archetypes.getProperties()) {
            assertTrue(archetype instanceof PropertyMap);
            PropertyMap map = (PropertyMap)archetype;
            assertTrue(map.getProperties().size() == 3);
            assertTrue(map.getProperties().get("shortName") != null);
            assertTrue(map.getProperties().get("minCardinality") != null);
            assertTrue(map.getProperties().get("maxCardinality") != null);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String) params.get("assertionFile");
        String archFile = (String) params.get("archetypeFile");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                archFile, assertionFile);
        service = new ArchetypeService(cache);
    }
}
