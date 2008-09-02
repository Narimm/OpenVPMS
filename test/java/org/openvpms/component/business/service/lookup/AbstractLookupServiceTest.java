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
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Arrays;
import java.util.Collection;


/**
 * Tests the {@link ILookupService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-11-27 05:03:46Z $
 */
@SuppressWarnings("HardCodedStringLiteral")
public class AbstractLookupServiceTest
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
     * Default constructor
     */
    public AbstractLookupServiceTest() {
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
        Collection<Lookup> lookups1
                = lookupService.getLookups("lookup.country");

        Lookup lookup = LookupUtil.createLookup("lookup.country", "AU");
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
        removeLookups("lookup.country");

        Lookup au = LookupUtil.createLookup(service, "lookup.country", "AU");
        au.setDefaultLookup(true);

        Lookup uk = LookupUtil.createLookup(service, "lookup.country", "UK");

        service.save(au);
        service.save(uk);

        Lookup lookup = lookupService.getDefaultLookup("lookup.country");
        assertEquals(au, lookup);
    }

    /**
     * Tests the {@link ILookupService#getSourceLookups(Lookup)} and
     * {@link ILookupService#getTargetLookups(Lookup)} method.
     */
    public void testGetSourceAndTargetLookups() {
        removeLookups("lookup.country");
        removeLookups("lookup.state");

        Lookup au = LookupUtil.createLookup("lookup.country", "AU");
        Lookup uk = LookupUtil.createLookup("lookup.country", "UK");

        service.save(au);
        service.save(uk);

        Lookup vic = LookupUtil.createLookup("lookup.state", "VIC");
        LookupUtil.addRelationship(service, "lookupRelationship.countryState",
                                   au, vic);
        service.save(Arrays.asList(au, vic));

        assertEquals(0, lookupService.getSourceLookups(au).size());
        Collection<Lookup> targets = lookupService.getTargetLookups(au);
        assertEquals(1, targets.size());
        assertTrue(targets.contains(vic));

        assertEquals(0, lookupService.getTargetLookups(vic).size());
        Collection<Lookup> sources = lookupService.getSourceLookups(vic);
        assertEquals(1, sources.size());
        assertTrue(sources.contains(au));
    }

    /**
     * Tests that lookup updates and removals are reflected by the service.
     */
    public void testUpdateRemove() {
        removeLookups("lookup.country");
        removeLookups("lookup.state");

        Lookup au = LookupUtil.createLookup("lookup.country", "AU");
        service.save(au);

        Lookup vic = LookupUtil.createLookup("lookup.state", "VIC");
        LookupRelationship rel = LookupUtil.addRelationship(
                service, "lookupRelationship.countryState", au, vic);
        service.save(Arrays.asList(au, vic));

        au = (Lookup) service.get(au.getObjectReference());
        vic = (Lookup) service.get(vic.getObjectReference());

        assertEquals(au, lookupService.getLookup("lookup.country",
                                                 au.getCode()));
        assertEquals(vic, lookupService.getLookup("lookup.state",
                                                  vic.getCode()));

        au.setDescription("Australia");
        service.save(au);

        assertEquals(au.getDescription(), lookupService.getLookup(
                "lookup.country", au.getCode()).getDescription());

        au.removeLookupRelationship(rel);
        vic.removeLookupRelationship(rel);
        service.save(Arrays.asList(au, vic));
        service.remove(vic);

        assertEquals(au, lookupService.getLookup("lookup.country",
                                                 au.getCode()));
        assertNull(lookupService.getLookup("lookup.state", vic.getCode()));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Registers the lookup service.
     *
     * @param service the lookup service
     */
    protected void setLookupService(ILookupService service) {
        lookupService = service;
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    protected ILookupService getLookupService() {
        return lookupService;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
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
     * Helper to remove all lookups for the specified archetype short name.
     *
     * @param shortName the archetype short name
     */
    protected void removeLookups(String shortName) {
        Collection<Lookup> lookups = lookupService.getLookups(shortName);
        for (Lookup lookup : lookups) {
            lookup = (Lookup) service.get(lookup.getObjectReference());
            service.remove(lookup);
        }

    }

}
