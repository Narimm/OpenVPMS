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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.search.IPage;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// log4j
import org.apache.log4j.Logger;

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
            IPage<IMObject> objects = service.get("entity", "act", null, null, false, false, 0, rowsPerPage, null);
            int totalCount = objects.getTotalNumOfRows();
            int rowCount = objects.getNumOfRows();
            int pages = (totalCount % rowsPerPage) == 0 ? totalCount/rowsPerPage : totalCount /rowsPerPage + 1;
            if (logger.isDebugEnabled()) {
                logger.debug("Page 0 numofRows " + objects.getNumOfRows() + " totalCount " + totalCount + " rowCount " + rowCount);
            }
            
            for (int page = 1; page < pages; page++) {
                objects = service.get("entity", "act", null, null, false, false, page*rowsPerPage, rowsPerPage, null);
                rowCount += objects.getNumOfRows();
                if (logger.isDebugEnabled()) {
                    logger.debug("Page " + page + " numofRows " + objects.getNumOfRows() + " totalCount " + totalCount + " rowCount " + rowCount);
                }
                assertTrue(objects != null);
                assertTrue(objects.getNumOfRows() <= rowsPerPage);
                assertTrue(objects.getRows().size() <= rowsPerPage);
            }
            assertTrue(rowCount == totalCount);
        }
    }
    
    /**
     * Test pagination for more more than the total number of records
     */
    public void testPaginationWithOversizedPages()
    throws Exception {
        IPage<IMObject> objects = service.get("entity", "act", null, null, false, false, 0, 1, null);
        int totalCount = objects.getTotalNumOfRows();
        objects = service.get("entity", "act", null, null, false, false, 0, totalCount*2, null);
        assertTrue(objects.getTotalNumOfRows() == totalCount);
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
