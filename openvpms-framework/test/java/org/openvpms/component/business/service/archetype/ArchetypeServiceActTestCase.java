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

// java core
import java.math.BigDecimal;
import java.util.Date;

//spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

// log4j
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Test that ability to create and query on acts.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceActTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceActTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService service;
    


    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceActTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceActTestCase() {
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
     * Test the creation of a simple act
     */
    public void testSimpleActCreation()
    throws Exception {
        Party person = createPerson("Mr", "Jim", "Alateras");
        Act act = createSimpleAct("study", "inprogress");
        Participation participation = createSimpleParticipation("studyParticipation",
                person, act);
        person.addParticipation(participation);
        service.save(act);
        service.save(person);
        
        person = (Party)ArchetypeQueryHelper.getByUid(service, 
                person.getArchetypeId(), person.getUid());
        assertTrue(person != null);
        assertTrue(person.getParticipations().size() == 1);
        
        participation = person.getParticipations().iterator().next();
        act = (Act)ArchetypeQueryHelper.getByObjectReference(service, 
                participation.getAct());
        assertTrue(act != null);
        assertTrue(act.getParticipations().size() == 1);
    }
    
    /**
     * Test the search by acts function
     */
    @SuppressWarnings("unchecked")
    public void testGetActs()
    throws Exception {
        
        // create an act which participates in 5 acts
        Party person = createPerson("Mr", "Jim", "Alateras");
        for (int index = 0; index < 5; index++) {
            Act act = createSimpleAct("study" + index, "inprogress");
            service.save(act);
            Participation participation = createSimpleParticipation("studyParticipation",
                person, act);
            person.addParticipation(participation);
        }
        
        service.save(person);
      
        // now use the getActs request
        IPage<Act> acts = ArchetypeQueryHelper.getActs(service, 
                person.getObjectReference(), "participation.simple", "act", "simple",
            null, null, null, null, null, false, 0, ArchetypeQuery.ALL_ROWS);
        assertTrue(acts.getTotalNumOfRows() == 5);
        
        // now look at the paging aspects
        acts = ArchetypeQueryHelper.getActs(service, person.getObjectReference(), 
                "participation.simple", "act", "simple", null, null, null, null, null, false, 0, 1);
        assertTrue(acts.getTotalNumOfRows() == 5);
        assertTrue(acts.getRows().size() == 1);
        assertFalse(StringUtils.isEmpty(acts.getRows().get(0).getName()));
    }
    
    /**
     * Retrieve acts using a start and end date
     */
    public void testGetActsBetweenTimes()
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("act.simple", false, true).add(
                new NodeConstraint("startTime", RelationalOp.BTW, new Date(),
                        new Date(System.currentTimeMillis() + 2*60*60*1000))).add(
                                new NodeConstraint("name", "between"));
        int acount = service.get(query).getRows().size();
        service.save(createSimpleAct("between", "start"));
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);

        for (int index = 0; index < 5; index++) {
            service.save(createSimpleAct("between", "start"));
        }
        acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 6);
    }
    
    /**
     * Test ovpms-211
     */
    public void testOVPMS211()
    throws Exception {
        Act estimationItem = (Act)service.create("act.customerEstimationItem");
        setNodeValue(estimationItem, "fixedPrice", "1.0");
        setNodeValue(estimationItem, "lowQty", "2.0");
        setNodeValue(estimationItem, "lowUnitPrice", "3.0");
        setNodeValue(estimationItem, "highQty", "4.0");
        setNodeValue(estimationItem, "highUnitPrice", "5.0");
        try {
            service.save(estimationItem);
        } catch (ValidationException exception) {
            logger.error(exception);
        }
        
        Act estimation = (Act)service.create("act.customerEstimation");
        assertTrue(estimation != null);
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(estimation.getArchetypeId());
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("lowTotal");
        assertTrue(ndesc != null);
        assertTrue(ndesc.getValue(estimation).getClass().getName(),
                ndesc.getValue(estimation) instanceof BigDecimal);
        setNodeValue(estimation, "status", "In Progress");
        
        ActRelationship rel = (ActRelationship)service.create("actRelationship.customerEstimationItem");
        assertTrue(rel != null);
        setNodeValue(rel, "source", estimation.getObjectReference());
        setNodeValue(rel, "target", estimationItem.getObjectReference());
        estimation.addSourceActRelationship(rel);
        
        estimationItem = (Act)service.create("act.customerEstimationItem");
        setNodeValue(estimationItem, "fixedPrice", "2.0");
        setNodeValue(estimationItem, "lowQty", "3.0");
        setNodeValue(estimationItem, "lowUnitPrice", "4.0");
        setNodeValue(estimationItem, "highQty", "5.0");
        setNodeValue(estimationItem, "highUnitPrice", "6.0");
        try {
            service.save(estimationItem);
        } catch (ValidationException exception) {
            logger.error(exception);
        }
        
        rel = (ActRelationship)service.create("actRelationship.customerEstimationItem");
        assertTrue(rel != null);
        setNodeValue(rel, "source", estimation.getObjectReference());
        setNodeValue(rel, "target", estimationItem.getObjectReference());
        estimation.addSourceActRelationship(rel);
        try {
            service.save(estimation);
        } catch (ValidationException exception) {
            logger.error(exception);
        }
        
        estimation = (Act)ArchetypeQueryHelper.getByUid(service, 
                estimation.getArchetypeId(), estimation.getUid());
        assertTrue(estimation != null);
        assertTrue(((BigDecimal)getNodeValue(estimation, "lowTotal")).longValue() > 0);
        assertTrue(((BigDecimal)getNodeValue(estimation, "highTotal")).longValue() > 0);
    }
    
    /**
     * test ovpms-228
     */
    public void testOVPMS228()
    throws Exception {
        Act act = (Act)service.create("act.customerAccountPayment");
        assertTrue(act != null);
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(act.getArchetypeId());
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("amount");
        assertTrue(ndesc != null);
        ndesc.getValue(act);
        assertTrue(ndesc.getValue(act).getClass().getName(),
                ndesc.getValue(act) instanceof BigDecimal);
    }
    
    
    /**
     * Create a simple act
     * 
     * @param name
     *            the name of the act
     * @param status
     *            the status of the act
     * @return Act                        
     */
    private Act createSimpleAct(String name, String status) {
        Act act = (Act)service.create("act.simple");
        
        act.setName(name);
        act.setStatus(status);
        act.setActivityStartTime(new Date());
        act.setActivityEndTime(new Date(System.currentTimeMillis() + 2*60*60*1000));
        
        return act;
    }
    
    /**
     * Create a simple participation
     * 
     * @param name
     *            the name of the participation
     * @param entity
     *            the entity in the participation
     * @param act 
     *            the act in the participation                        
     */
    private Participation createSimpleParticipation(String name, Entity entity, 
            Act act) {
        Participation participation = (Participation)service.create("participation.simple");
        participation.setName(name);
        participation.setEntity(entity.getObjectReference());
        participation.setAct(act.getObjectReference());
        
        return participation;
    }
    
    /**
     * Create a person with the specified title, firstName and LastName
     * 
     * @param title
     * @param firstName
     * @param lastName
     * 
     * @return Person
     */
    private Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party)service.create("person.person");
        person.getDetails().setAttribute("lastName", lastName);
        person.getDetails().setAttribute("firstName", firstName);
        person.getDetails().setAttribute("title", title);
        
        return person;
    }

    /**
     * Set the specification node value
     * 
     * @param imobj
     *            the imobject
     * @param node
     *            the node name
     * @param value
     *            the node value
     */
    private void setNodeValue(IMObject imobj, String node, Object value) {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(imobj.getArchetypeId());
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor(node);
        assertTrue(ndesc != null);
        ndesc.setValue(imobj, value);
    }

    /**
     * Get the specification node value
     * 
     * @param imobj
     *            the imobject
     * @param node
     *            the node name
     * @return Object
     *            the node value
     */
    private Object getNodeValue(IMObject imobj, String node) {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(imobj.getArchetypeId());
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor(node);
        assertTrue(ndesc != null);
        return ndesc.getValue(imobj);
    }

}
