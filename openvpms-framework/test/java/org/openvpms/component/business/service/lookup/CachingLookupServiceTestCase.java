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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.lookup;

import org.junit.After;
import org.junit.Before;
import org.openvpms.component.business.service.cache.BasicEhcacheManager;


/**
 * Tests the {@link CachingLookupService}.
 *
 * @author Tim Anderson
 */
public class CachingLookupServiceTestCase extends AbstractLookupServiceTest {

    /**
     * The cache manager.
     */
    private BasicEhcacheManager cacheManager;

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception {
        cacheManager = new BasicEhcacheManager(100);
        CachingLookupService service = new CachingLookupService(getArchetypeService(), getDAO(), cacheManager);
        setLookupService(service);
    }

    /**
     * Cleans up after the test
     */
    @After
    public void tearDown() {
        ((CachingLookupService)getLookupService()).destroy();
        cacheManager.destroy();
    }

}
