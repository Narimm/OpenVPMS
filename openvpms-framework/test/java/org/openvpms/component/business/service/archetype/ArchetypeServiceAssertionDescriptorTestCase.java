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

import junit.framework.TestCase;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheFS;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;


/**
 * Test the management of assertions through the archetype service.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceAssertionDescriptorTestCase extends TestCase {

    /**
     * Reference to the archetype service
     */
    private ArchetypeService service;

    /**
     * Test that we can successfully create all the archetypes loaded by the
     * service.
     *
     * @throws Exception for any error
     */
    public void testCreateDefaultObject() throws Exception {
        for (ArchetypeDescriptor descriptor : service
                .getArchetypeDescriptors()) {
            assertTrue("Creating " + descriptor.getName(), service
                    .create(descriptor.getType()) != null);
        }
    }

    /**
     * Test the creation of an archetypeRange assertion.
     *
     * @throws Exception for any error
     */
    public void testCreateArchetypeRange() throws Exception {
        AssertionDescriptor adesc = (AssertionDescriptor) service.create(
                "assertion.archetypeRange");
        assertTrue(adesc != null);
        PropertyMap pdesc = (PropertyMap) service.create(
                "assertion.archetypeRangeProperties");
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


        PropertyList archetypes = (PropertyList) adesc.getProperty(
                "archetypes");
        assertTrue(archetypes.getProperties().size() == 1);
        for (NamedProperty archetype : archetypes.getProperties()) {
            assertTrue(archetype instanceof PropertyMap);
            PropertyMap map = (PropertyMap) archetype;
            assertTrue(map.getProperties().size() == 3);
            assertTrue(map.getProperties().get("shortName") != null);
            assertTrue(map.getProperties().get("minCardinality") != null);
            assertTrue(map.getProperties().get("maxCardinality") != null);
        }
    }

    /**
     * Sets up the test case.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        String assertionFile = "org/openvpms/archetype/assertionTypes.xml";
        String archFile = "org/openvpms/archetype/system/assertion/assertion.archetypeRange.adl";

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                archFile, assertionFile);
        service = new ArchetypeService(cache);
    }
}
