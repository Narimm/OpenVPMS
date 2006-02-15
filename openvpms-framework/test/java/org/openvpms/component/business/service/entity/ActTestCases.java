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

package org.openvpms.component.business.service.entity;

//spring-context
import java.util.List;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * Test the entity service with acts
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActTestCases extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ActTestCases.class);
    
    /**
     * Holds a reference to the archetype service
     */
    private IArchetypeService archetypeService;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ActTestCases.class);
    }

    /**
     * Default constructor
     */
    public ActTestCases() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/entity/entity-service-appcontext.xml" 
                };
    }

    /**
     * Test the creation of an act
     */
    public void testSimpleActCreation()
    throws Exception {
        Person person = createPerson("Mr", "Jim", "Alateras");
        archetypeService.save(person);
        
        Act act = createAct("wake up");
        archetypeService.save(act);
        Participation participation = createParticipation("part1", act, person);
        act.addParticipation(participation);
        archetypeService.save(act);
    }

    /**
     * Create 3 participations for the same entity and then use the entity 
     * service get to retrieve them all
     */
    public void testGetParticipantsQuery()
    throws Exception {
        Person person = createPerson("Mr", "Jim", "Alateras");
        archetypeService.save(person);
        
        Act act1 = createAct("wake up");
        archetypeService.save(act1);
        Act act2 = createAct("lunch");
        archetypeService.save(act2);
        Act act3 = createAct("dinner");
        archetypeService.save(act3);
        
        Participation participation1 = createParticipation("part1", act1, person);
        Participation participation2 = createParticipation("part2", act2, person);
        Participation participation3 = createParticipation("part3", act3, person);
        act1.addParticipation(participation1);
        archetypeService.save(act1);
        act2.addParticipation(participation2);
        archetypeService.save(act2);
        act3.addParticipation(participation3);
        archetypeService.save(act3);

        person = (Person)archetypeService.getById(person.getArchetypeId(), person.getUid());
        assertTrue(person != null);
        assertTrue(person.getParticipations().size() == 3);
        
        List<Participation> participations = archetypeService.getParticipations(
                person.getUid(), null, null, null, null, null, true);
        assertTrue(participations.size() == 3);
    }
    
    /**
     * Create 2 participations for the same entity and then use the archetype
     * service to retrieve the acts for the specified entity
     */
    public void testGetActsByEntityIdQuery()
    throws Exception {
        Person person = createPerson("Mr", "Jim", "Alateras");
        archetypeService.save(person);
        
        Act act1 = createAct("wake up");
        archetypeService.save(act1);
        Act act2 = createAct("lunch");
        archetypeService.save(act2);
        
        Participation participation1 = createParticipation("part1", act1, person);
        Participation participation2 = createParticipation("part2", act2, person);
        act1.addParticipation(participation1);
        archetypeService.save(act1);
        act2.addParticipation(participation2);
        archetypeService.save(act2);

        List<Act> acts = archetypeService.getActs(
                person.getUid(), null, null, null, null, null, null, null, 
                null, true);
        assertTrue(acts.size() == 2);
    }
    
    /**
     * Exercise the getActs query 
     */
    public void testGetActsQuery()
    throws Exception {
        // get the initial act count
        int actCount = archetypeService.getActs("act", null, null, null, null, 
                null, null, false).size();

        // create an act and check the count again
        Act act1 = createAct("wake up");
        archetypeService.save(act1);
        int actCount1 = archetypeService.getActs("act", null, null, null, null, 
                null, null, false).size();
        assertTrue(actCount1 == (actCount + 1));

        // create multiple acts and check the count again
        act1 = createAct("i want to wake up");
        archetypeService.save(act1);
        act1 = createAct("wake up now");
        archetypeService.save(act1);
        actCount1 = archetypeService.getActs("act", null, null, null, null, 
                null, null, false).size();
        assertTrue(actCount1 == (actCount + 3));
        
        // check that it retrieves null result set correctly
        assertTrue(archetypeService.getActs("jimmya-act", null, null, null, 
                null, null, null, false).size() == 0);
    }
    
    
    /**
     * Create a person
     * 
     * @param title
     *            the person's title
     * @param firstName
     *            the person's first name
     * @param lastName
     *            the person's last name            
     * @return Person                  
     */
    private Person createPerson(String title, String firstName, String lastName) {
        Entity entity = (Entity)archetypeService.create("person.person");
        assertTrue(entity instanceof Person);
        
        Person person = (Person)entity;
        person.setTitle(title);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        
        return person;
    }
    
    /**
     * Create a participation
     * 
     * @param name
     *            the name of the participation
     * @param act 
     *            the act that the entity is participating in.            
     * @param entity
     *            the entity in the participation            
     * @return Participation
     */
    private Participation createParticipation(String name, Act act, Entity entity) {
        Participation participation = (Participation)archetypeService.create("participation.simple");
        participation.setName(name);
        participation.setEntity(new IMObjectReference(entity));
        participation.setAct(new IMObjectReference(act));
        
        return participation;
    }
    
    /**
     * Create a simple act
     *
     * @param name
     *          the name of the act
     * @return Act
     */
    private Act createAct(String name) {
        Act act = (Act)archetypeService.create("act.simple");
        act.setName(name);
        
        return act;
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.archetypeService = (IArchetypeService)applicationContext.getBean("archetypeService");
    }

}
