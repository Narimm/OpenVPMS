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
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.ArchetypeProperty;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.CollectionNodeConstraint.JoinType;

// log4j
import org.apache.log4j.Logger;

/**
 * Test that ability to create and query on acts.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceQueryTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceQueryTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService service;
    


    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceQueryTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceQueryTestCase() {
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

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (IArchetypeService)applicationContext.getBean(
                "archetypeService");
    }
    
    /**
     * Test the query by code in the lookup entity. This will support 
     * OVPMS-35
     */
    public void testOVPMS35()
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.country", false, true).add(
                        new NodeConstraint("country", RelationalOp.EQ, "Belarus"));
        
        int acount = service.get(query).getRows().size();
        Lookup lookup = (Lookup)service.create("lookup.country");
        lookup.setValue("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);
    }
    
    /**
     * Test query by code with wildcard
     */
    public void testGetByCodeWithWildcard()
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.country", false, true).add(
                        new NodeConstraint("country", RelationalOp.EQ, "Bel*"));
        
        int acount = service.get(query).getRows().size();
        Lookup lookup = (Lookup)service.create("lookup.country");
        lookup.setValue("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);
    }
    
    /**
     * Test query by code with wild in short name
     */
    public void testGetCodeWithWildCardShortName()
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.cou*", false, true).add(
                new NodeConstraint("country", RelationalOp.EQ, "Bel*"));

        int acount = service.get(query).getRows().size();
        Lookup lookup = (Lookup)service.create("lookup.country");
        lookup.setValue("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);
    }
    
    /**
     * Test query by code with wild in short name and an order clause
     */
    public void testGetCodeWithWildCardShortNameAndOrdered()
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.cou*", false, true)
            .add(new NodeConstraint("country", RelationalOp.EQ, "Bel*"))
            .add(new NodeSortConstraint("country", true));

        int acount = service.get(query).getRows().size();
        Lookup lookup = (Lookup)service.create("lookup.country");
        lookup.setValue("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);
    }
    
    /**
     * Test OVPMS245
     */
    public void testOVPMS245()
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeShortNameConstraint("product.product", false, true))
                    .add(new CollectionNodeConstraint("classifications", true)
                            .setJoinType(JoinType.LeftOuterJoin)
                            .add(new OrConstraint()
                                .add(new ArchetypeNodeConstraint(ArchetypeProperty.ConceptName, RelationalOp.IsNULL))
                                .add(new AndConstraint()
                                        .add(new ArchetypeNodeConstraint(ArchetypeProperty.ConceptName, RelationalOp.EQ, "species"))
                                        .add(new NodeConstraint("name", RelationalOp.EQ, "Canine"))
                                        .add(new NodeSortConstraint("name", true)))));
        
        IPage<IMObject> page = service.get(query);
        assertTrue(page != null);
    }
    
}
