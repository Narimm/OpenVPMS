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

import org.apache.commons.lang.StringUtils;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

/**
 * Test the entity service with acts
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("entity-service-appcontext.xml")
public class ActTestCase extends AbstractArchetypeServiceTest {

    /**
     * Test the creation of an act.
     */
    @Test
    public void testSimpleActCreation() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);

        Act act = createAct("wake up");
        save(act);
        Participation participation = createParticipation("part1", act, person);
        act.addParticipation(participation);
        save(act);
    }

    /**
     * Create 3 participations for the same entity and then use the entity
     * service get to retrieve them all
     */
    @Test
    public void testGetParticipantsQuery() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);

        Act act1 = createAct("wake up");
        save(act1);
        Act act2 = createAct("lunch");
        save(act2);
        Act act3 = createAct("dinner");
        save(act3);

        Participation participation1 = createParticipation("part1", act1, person);
        Participation participation2 = createParticipation("part2", act2, person);
        Participation participation3 = createParticipation("part3", act3, person);
        act1.addParticipation(participation1);
        save(act1);
        act2.addParticipation(participation2);
        save(act2);
        act3.addParticipation(participation3);
        save(act3);

        List participations = ArchetypeQueryHelper.getParticipations(
                getArchetypeService(), person.getObjectReference(), "participation.simple",
                null, null,
                null, null, true, 0, ArchetypeQuery.ALL_RESULTS).getResults();
        assertEquals(3, participations.size());
    }

    /**
     * Create 2 participations for the same entity and then use the archetype
     * service to retrieve the acts for the specified entity
     */
    @Test
    public void testGetActsByEntityIdQuery() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);

        Act act1 = createAct("wake up");
        save(act1);
        Act act2 = createAct("lunch");
        save(act2);

        Participation participation1 = createParticipation("part1", act1, person);
        Participation participation2 = createParticipation("part2", act2, person);
        act1.addParticipation(participation1);
        save(act1);
        act2.addParticipation(participation2);
        save(act2);

        List acts = ArchetypeQueryHelper.getActs(getArchetypeService(),
                                                 person.getObjectReference(), "participation.simple",
                                                 "act", "simple", null, null, null, null, null, true, 0,
                                                 ArchetypeQuery.ALL_RESULTS).getResults();
        assertEquals(2, acts.size());
    }


    /**
     * Test bug OVPMS-219.
     */
    @Test
    public void testOVPMS219() {
        Act act1 = createAct("act1");
        save(act1);
        Act act2 = createAct("act2");
        save(act2);
        act1.addSourceActRelationship(createActRelationship(act1, act2));
        save(act1);
        act1 = (Act) get(act1.getObjectReference());
        for (ActRelationship theRel : act1.getSourceActRelationships()) {
            act2 = (Act) get(theRel.getTarget());
        }
        save(act2);
        save(act1);
    }

    /**
     * Exercise the getActs query.
     */
    @Test
    public void testGetActsQuery() {
        // get the initial act count
        int actCount = ArchetypeQueryHelper.getActs(getArchetypeService(),
                                                    "act", null, null, null, null, null, null, false, 1,
                                                    ArchetypeQuery.ALL_RESULTS).getResults().size();

        // create an act and check the count again
        Act act1 = createAct("wake up");
        save(act1);
        int actCount1 = ArchetypeQueryHelper.getActs(getArchetypeService(),
                                                     "act", null, null, null, null, null, null, false, 1,
                                                     ArchetypeQuery.ALL_RESULTS).getResults().size();
        assertEquals(actCount + 1, actCount1);

        // create multiple acts and check the count again
        act1 = createAct("i want to wake up");
        save(act1);
        act1 = createAct("wake up now");
        save(act1);
        actCount1 = ArchetypeQueryHelper.getActs(getArchetypeService(),
                                                 "act", null, null, null, null, null, null, false, 1,
                                                 ArchetypeQuery.ALL_RESULTS).getResults().size();
        assertEquals(actCount + 3, actCount1);

        // check that it retrieves null result set correctly
        try {
            ArchetypeQueryHelper.getActs(getArchetypeService(), "jimmya-act", null,
                                         null, null, null, null, null, false, 1, ArchetypeQuery.ALL_RESULTS)
                    .getResults().size();
            fail("This request should not have completed");
        } catch (ArchetypeServiceException exception) {
            if (exception.getErrorCode() != ArchetypeServiceException.ErrorCode.FailedToExecuteQuery) {
                fail("Incorrect exception received: " + exception.getErrorCode());
            }
        }
    }

    /**
     * Test for bug 229.
     */
    @Test
    public void testOVPMS229() {
        // get the initial act count
        int actCount = ArchetypeQueryHelper.getActs(getArchetypeService(),
                                                    "act", null, null, null, null, null, null, false, 1,
                                                    ArchetypeQuery.ALL_RESULTS).getResults().size();

        // create an act and check the count again
        Act act1 = createAct("wake up jim");
        save(act1);
        int actCount1 = ArchetypeQueryHelper.getActs(getArchetypeService(),
                                                     "act", null, null, null, null, null, null, false, 1,
                                                     ArchetypeQuery.ALL_RESULTS).getResults().size();
        assertEquals(actCount + 1, actCount1);
    }

    /**
     * Test that we can use the archetype service function resolve in an
     * xpath expression
     */
    @Test
    public void testResolveInDerivedValue() {
        Act act1 = createAct("my act");
        save(act1);
        Act act2 = createAct("your act");
        save(act2);
        ActRelationship rel = createActRelationship(act1, act2);
        getArchetypeService().deriveValues(rel);
        assertFalse(StringUtils.isEmpty(rel.getName()));
        assertFalse(StringUtils.isEmpty(rel.getDescription()));
        act1.addActRelationship(rel);
        save(act1);

        Act tmp = (Act) get(act1.getObjectReference());
        assertNotNull(tmp);
        assertFalse(StringUtils.isEmpty(rel.getName()));
        assertFalse(StringUtils.isEmpty(rel.getDescription()));
    }

    /**
     * Create a person.
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    private Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party) create("party.person");

        person.getDetails().put("title", title);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("lastName", lastName);

        return person;
    }

    /**
     * Create a participation
     *
     * @param name   the name of the participation
     * @param act    the act that the entity is participating in.
     * @param entity the entity in the participation
     * @return Participation
     */
    private Participation createParticipation(String name, Act act, Entity entity) {
        Participation participation = (Participation) create("participation.simple");
        participation.setName(name);
        participation.setEntity(entity.getObjectReference());
        participation.setAct(act.getObjectReference());

        return participation;
    }

    /**
     * Create a simple act
     *
     * @param name the name of the act
     * @return Act
     */
    private Act createAct(String name) {
        Act act = (Act) create("act.simple");
        act.setName(name);
        act.setDescription(name);

        return act;
    }

    /**
     * Create a simple act relationship
     *
     * @param source the source act
     * @param target the target act
     * @return ActRelationship
     */
    private ActRelationship createActRelationship(Act source, Act target) {
        ActRelationship rel = (ActRelationship) create("actRelationship.simple");
        rel.setName(source.getName() + "-" + target.getName());
        rel.setSource(source.getObjectReference());
        rel.setTarget(target.getObjectReference());

        return rel;
    }

}
