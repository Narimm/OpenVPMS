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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.lookup;

import net.sf.ehcache.Cache;
import org.junit.Before;


/**
 * Tests the {@link CachingLookupService}.
 *
 * @author Tim Anderson
 */
public class CachingLookupServiceTestCase extends AbstractLookupServiceTest {

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception {
        Cache cache = (Cache) applicationContext.getBean("lookupCache");
        setLookupService(new CachingLookupService(getArchetypeService(), getDAO(), cache));
    }

}
