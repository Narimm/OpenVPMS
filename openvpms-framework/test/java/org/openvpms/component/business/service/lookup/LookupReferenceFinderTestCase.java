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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.lookup;

import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Map;


/**
 * Tests the {@link LookupReferenceFinder}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupReferenceFinderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service
     */
    private IArchetypeService service;

    /**
     * The lookup service.
     */
    private ILookupService lookupService;

    /**
     * Tests lookup references where the lookup is referenced by code.
     */
    public void testCodeLookup() {
        LookupReferenceFinder finder = new LookupReferenceFinder(service, lookupService);
        Map<NodeDescriptor, ArchetypeDescriptor> matches = finder.getCodeReferences("lookup.species");
        assertEquals(2, matches.size());
        checkReferences(matches, "party.patientpet", "species");
        checkReferences(matches, "party.animalpet", "species");
    }

    /**
     * Tests lookup references where the lookup is referenced by code, and the code is determined by the target
     * of a lookup relationship.
     */
    public void testTargetCodeLookup() {
        LookupReferenceFinder finder = new LookupReferenceFinder(service, lookupService);
        Map<NodeDescriptor, ArchetypeDescriptor> matches = finder.getCodeReferences("lookup.breed");
        assertEquals(3, matches.size());
        checkReferences(matches, "party.patientpet", "breed");
        checkReferences(matches, "party.animalpet", "breed");
        checkReferences(matches, "party.horsepet", "breed");
    }

    public void testArchetypeReference() {
        LookupReferenceFinder finder = new LookupReferenceFinder(service, lookupService);
        Map<NodeDescriptor, ArchetypeDescriptor> matches = finder.getArchetypeReferences("lookup.species");
        assertEquals(2, matches.size());
        checkReferences(matches, "lookupRelationship.speciesBreed", "source");
        checkReferences(matches, "product.product", "classifications");
    }

    /**
     * Sets up  the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        service = (IArchetypeService) applicationContext.getBean("archetypeService");
        lookupService = new LookupService(service, (IMObjectDAO) applicationContext.getBean("imObjectDao"));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/lookup/lookup-service-appcontext.xml"
        };
    }

    private void checkReferences(Map<NodeDescriptor, ArchetypeDescriptor> matches, String shortName, String node) {
        boolean found = false;
        for (NodeDescriptor descriptor : matches.keySet()) {
            if (descriptor.getName().equals(node)) {
                ArchetypeDescriptor archetype = matches.get(descriptor);
                if (archetype.getType().getShortName().equals(shortName)) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
    }
}
