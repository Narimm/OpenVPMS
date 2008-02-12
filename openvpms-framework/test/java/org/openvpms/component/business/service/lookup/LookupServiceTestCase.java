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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.lookup;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Collection;

/**
 * Tests the {@link LookupService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-11-27 05:03:46Z $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class LookupServiceTestCase extends
                                   AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service
     */
    private ArchetypeService service;

    /**
     * The lookup service.
     */
    private ILookupService lookupService;

    /**
     * Default constructor
     */
    public LookupServiceTestCase() {
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

    /**
     * Tests the {@link ILookupService#getLookup} method.
     */
    public void testGetLookup() throws Exception {
        Lookup lookup = LookupUtil.createLookup(service, "lookup.breed",
                                                "CANINE");
        String code = lookup.getCode();

        Lookup found = lookupService.getLookup("lookup.breed", code);
        assertNull(found);

        service.save(lookup);

        found = lookupService.getLookup("lookup.breed", code);
        assertNotNull(found);
        assertEquals(lookup.getObjectReference(), found.getObjectReference());
    }

    /**
     * Tests the {@link ILookupService#getLookups} method.
     */
    public void testGetLookups() throws Exception {
        Lookup lookup = LookupUtil.createLookup(service, "lookup.country",
                                                "AU");

        Collection<Lookup> lookups1
                = lookupService.getLookups("lookup.country");
        service.save(lookup);
        Collection<Lookup> lookups2
                = lookupService.getLookups("lookup.country");
        assertEquals(lookups1.size() + 1, lookups2.size());
        assertTrue(lookups2.contains(lookup));
    }

    /**
     * Tests the {@link ILookupService#getDefaultLookup} method.
     */
    public void testGetDefaultLookups() {
        // clean out existing lookups
        Collection<Lookup> lookups = lookupService.getLookups("lookup.country");
        for (Lookup lookup : lookups) {
            service.remove(lookup);
        }
        lookups = lookupService.getLookups("lookup.country");
        assertTrue(lookups.isEmpty());

        // save 2 new lookups
        Lookup au = (Lookup) service.create("lookup.country");
        au.setCode("AU");
        au.setDefaultLookup(true);

        Lookup uk = (Lookup) service.create("lookup.country");
        uk.setCode("UK");

        service.save(au);
        service.save(uk);

        Lookup lookup = lookupService.getDefaultLookup("lookup.country");
        assertEquals(au, lookup);
    }

    /* (non-Javadoc)
    * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
    */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (ArchetypeService) applicationContext.getBean(
                "archetypeService");
        lookupService = new LookupService(service);

    }
}
