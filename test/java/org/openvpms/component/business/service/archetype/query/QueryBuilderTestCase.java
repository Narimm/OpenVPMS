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

package org.openvpms.component.business.service.archetype.query;

// spring-context
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.SortOrder;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework

// log4j
import org.apache.log4j.Logger;

/**
 * Test that generic query builder
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class QueryBuilderTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(QueryBuilderTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private QueryBuilder builder;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(QueryBuilderTestCase.class);
    }

    /**
     * Default constructor
     */
    public QueryBuilderTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/archetype/query/query-appcontext.xml" 
                };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.builder = (QueryBuilder)applicationContext.getBean("queryBuilder");
    }
    
    /**
     * Test the query by uid and archetype Id
     */
    public void testQueryByUidAndArchetypeId() 
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("openvpms-party-person.person.1.0"), false)
                    .add(new NodeConstraint("uid", "1"));
        logger.debug(builder.build(query).getQueryString());
    }
    
    /**
     * Test the query by uid and archetype short name
     */
    public void testQueryByUidAndArchetypeShortName() 
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("person.person", false, false)
                    .add(new NodeConstraint("uid", "1"));
        logger.debug(builder.build(query).getQueryString());
    }
    
    /**
     * Test the query by person and name
     */
    public void testQueryByArchetypeIdAndName() 
    throws Exception {
        ArchetypeQuery query =  new ArchetypeQuery(
                new ArchetypeId("openvpms-party-person.person.1.0"), false)
                    .add(new NodeConstraint("name", "sa*"));
        logger.debug(builder.build(query).getQueryString());
    }
    
    /**
     * Test the query on archetype id, name and a sort criteria
     */
    public void testQueryByArchetypeIdAndNameAndSort() 
    throws Exception {
        ArchetypeQuery query =  new ArchetypeQuery(
                new ArchetypeId("openvpms-party-person.person.1.0"), false)
                    .add(new NodeConstraint("name", "sa*")
                            .setSortOrder(SortOrder.Ascending))
                    .add(new CollectionNodeConstraint("contacts",
                            new ArchetypeShortNameConstraint("contact.location", false, false)));
        logger.debug(builder.build(query).getQueryString());
    }
        
    /**
     * Test the query on archetype short name, name and a sort criteria
     */
    public void testQueryByArchetypeShortNameAndNameAndSort() 
    throws Exception {
        ArchetypeQuery query =  new ArchetypeQuery(
                new ArchetypeShortNameConstraint(new String[]{"person.person", "organization.organization"}, false, false))
                    .add(new NodeConstraint("uid", "1"))
                    .add(new NodeConstraint("name", "sa*")
                            .setSortOrder(SortOrder.Ascending))
                    .add(new CollectionNodeConstraint("contacts",
                            new ArchetypeShortNameConstraint("contact.location", false, false)));
        logger.debug(builder.build(query).getQueryString());    
    }

}
