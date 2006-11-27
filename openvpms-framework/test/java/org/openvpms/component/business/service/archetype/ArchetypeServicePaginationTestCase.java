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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.HashSet;
import java.util.Set;

/**
 * Test that pagination works on the IArchetypeService
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServicePaginationTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServicePaginationTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServicePaginationTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServicePaginationTestCase() {
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
     * Test that we can paginate the objects of type entity act
     */
    public void testPaginationOnEntityAct() throws Exception {
        for (int index = 0; index < 10; index++) {
            int rowsPerPage = index + 1;
            IPage<IMObject> objects = ArchetypeQueryHelper.get(service,
                    "entity", "act", null, null, false, 0, rowsPerPage);
            int totalCount = objects.getTotalResults();
            int rowCount = objects.getResults().size();
            int pages = (totalCount % rowsPerPage) == 0 ? totalCount/rowsPerPage : totalCount /rowsPerPage + 1;
            if (logger.isDebugEnabled()) {
                logger.debug("Page 0 numofRows " 
                        + objects.getResults().size()
                        + " totalCount " + totalCount + " rowCount " + rowCount);
            }
            
            for (int page = 1; page < pages; page++) {
                objects = ArchetypeQueryHelper.get(service, "entity", "act", 
                        null, null, false, page*rowsPerPage, rowsPerPage);
                rowCount += objects.getResults().size();
                if (logger.isDebugEnabled()) {
                    logger.debug("Page " + page + " numofRows " 
                            + objects.getResults().size()
                            + " totalCount " + totalCount + " rowCount " + rowCount);
                }
                assertTrue(objects != null);
                assertTrue(objects.getResults().size() <= rowsPerPage);
            }
            assertTrue(rowCount == totalCount);
        }
    }
    
    /**
     * Test pagination for more more than the total number of records
     */
    public void testPaginationWithOversizedPages()
    throws Exception {
        IPage<IMObject> objects = ArchetypeQueryHelper.get(service, "entity", 
                "act", null, null, false, 0, 1);
        int totalCount = objects.getTotalResults();
        objects = ArchetypeQueryHelper.get(service, "entity", "act", null, null, 
                false, 0, totalCount*2);
        assertTrue(objects.getTotalResults() == totalCount);
    }

    /**
     * Test pagination for distinct types. This also test the reopening
     * of OVPMS244
     */
    public void testOVPMS244()
    throws Exception {
        Set<String> linkIds = new HashSet<String>();
        int rowsPerPage = 10;
        for (int startRow = 0;; startRow +=10) {
            IPage<IMObject> objects = ArchetypeQueryHelper.get(service,
                    "common", "entityRelationship", "a*", null, false, 
                    startRow, rowsPerPage);
            for (IMObject object : objects.getResults()) {
                if (linkIds.contains(object.getLinkId())) {
                    fail("This row has already been returned");
                }
                linkIds.add(object.getLinkId());
            }
            
            if ((startRow + rowsPerPage) >= objects.getTotalResults()) {
                break;
            }
        }
    }
    
    /**
     * Test will null pagination
     */
    public void testWithNullPagination()
    throws Exception {
        IPage<IMObject> objects = ArchetypeQueryHelper.get(service, "entity", 
                "act", null, null, false, 0, 1);
        int totalCount = objects.getTotalResults();
        objects = ArchetypeQueryHelper.get(service, "entity", "act", null, null, 
                false, 0, ArchetypeQuery.ALL_RESULTS);
        assertTrue(objects.getTotalResults() == totalCount);
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean(
                "archetypeService");
    }
}
