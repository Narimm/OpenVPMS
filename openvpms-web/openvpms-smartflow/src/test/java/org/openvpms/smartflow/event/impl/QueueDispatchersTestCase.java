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

package org.openvpms.smartflow.event.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link QueueDispatchers}.
 *
 * @author Tim Anderson
 */
public class QueueDispatchersTestCase extends AbstractDispatcherTest {

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules rules;

    /**
     * The practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

    /**
     * The dispatchers.
     */
    private QueueDispatchers dispatchers;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        FlowSheetServiceFactory sfsFactory = new FlowSheetServiceFactory("http://bogus", "1", getArchetypeService(),
                                                                         getLookupService(),
                                                                         Mockito.mock(DocumentHandlers.class),
                                                                         Mockito.mock(MedicalRecordRules.class));
        PracticeService practiceService = new PracticeService(getArchetypeService(), practiceRules, null);
        QueueDispatcherFactory factory = new QueueDispatcherFactory(sfsFactory, getArchetypeService(),
                                                                    getLookupService(), transactionManager,
                                                                    practiceService, rules) {
            @Override
            protected ServiceBusConfig getConfig(Party location) {
                return new ServiceBusConfig();
            }
        };
        dispatchers = new QueueDispatchers(factory);
    }

    /**
     * Verifies that a {@link QueueDispatcher} is created when a location is added for the first time.
     */
    @Test
    public void testAdd() {
        Party location1 = createLocation("location1", "A");
        Party location2 = createLocation("location2", null);
        Party location3 = createLocation("location3", "B");
        checkAdd(location1, true);
        checkAdd(location2, false);
        checkAdd(location3, true);

        checkDispatchers(location1, location3);

        // verify adding the same location does nothing
        checkAdd(location1, false);
        checkDispatchers(location1, location3);
    }

    /**
     * Verifies that a {@link QueueDispatcher} can be removed.
     */
    @Test
    public void testRemove() {
        Party location1 = createLocation("location1", "A");
        Party location2 = createLocation("location2", null);
        checkAdd(location1, true);
        checkAdd(location2, false);

        checkDispatchers(location1);
        dispatchers.remove(location2);
        checkDispatchers(location1);
        dispatchers.remove(location1);
        checkDispatchers();
    }

    /**
     * Verifies API keys can be updated.
     */
    @Test
    public void testUpdate() {
        Party location1 = createLocation("location1", "A");
        Party location2 = createLocation("location2", null);
        Party location3 = createLocation("location3", "C");
        checkAdd(location1, true);
        checkAdd(location2, false);
        checkAdd(location3, true);

        // verify only A and C have dispatchers
        checkDispatchers(location1, location3);

        setKey(location2, "B");
        checkAdd(location2, true);

        // verify A, B and C have dispatchers
        checkDispatchers(location1, location2, location3);

        // change B -> X. Should create a new dispatcher
        setKey(location3, "X");
        checkAdd(location3, true);
        checkDispatchers(location1, location2, location3);

        // now remove X. The dispatcher should be removed
        setKey(location3, null);
        checkAdd(location3, false);
        checkDispatchers(location1, location2);
    }

    /**
     * Verifies that when an API key is shared by multiple locations, and the key for the location which has a
     * QueueDispatcher registered is changed, a new QueueDispatcher registered for it.
     */
    @Test
    public void testSharedKey() {
        Party location1 = createLocation("location1", "A");
        Party location2 = createLocation("location2", "A");
        Party location3 = createLocation("location3", "A");

        // give the locations identifiers to make it deterministic when selecting which location will be allocated A
        // once location1 is changed
        location1.setId(1);
        location2.setId(2);
        location3.setId(3);
        checkAdd(location1, true);
        checkAdd(location2, false);
        checkAdd(location3, false);

        checkDispatchers(location1);

        setKey(location1, "B");
        checkAdd(location1, true);

        // location2 will now handle A as it has an identifier lower than location3
        checkDispatchers(location1, location2);
    }

    /**
     * Verifies that when an API key is shared by multiple locations, and the key for a location which doesn't have
     * a QueueDispatcher registered is changed, a new QueueDispatcher is registered for it.
     */
    @Test
    public void testSharedKeyChangeUnregisteredLocation() {
        Party location1 = createLocation("location1", "A");
        Party location2 = createLocation("location2", "A");
        Party location3 = createLocation("location3", "A");
        checkAdd(location1, true);
        checkAdd(location2, false);
        checkAdd(location3, false);

        checkDispatchers(location1);

        setKey(location3, "C");
        checkAdd(location3, true);

        checkDispatchers(location1, location3);
    }

    /**
     * Verifies that inactive locations don't have dispatchers registered.
     */
    @Test
    public void testInactiveLocation() {
        Party location1 = createLocation("location1", "A");
        Party location2 = createLocation("location2", "B");
        location2.setActive(false);
        checkAdd(location1, true);
        checkAdd(location2, false);
        checkDispatchers(location1);

        location1.setActive(false);
        checkAdd(location1, false);
        checkDispatchers();
    }

    /**
     * Checks adding a practice location to the dispatchers.
     *
     * @param location the practice location
     * @param added    if {@code true}, expect a {@link QueueDispatcher} to be registered
     * @return the dispatcher. May be {@code null}
     */
    private QueueDispatcher checkAdd(Party location, boolean added) {
        QueueDispatcher dispatcher = dispatchers.add(location);
        if (added) {
            assertNotNull(dispatcher);
            assertEquals(location, dispatcher.getLocation());
        } else {
            assertNull(dispatcher);
        }
        return dispatcher;
    }

    /**
     * Verifies that there is a dispatcher registered for each of the specified locations.
     *
     * @param locations the practice locations
     */
    private void checkDispatchers(Party... locations) {
        List<QueueDispatcher> list = dispatchers.getDispatchers();
        assertEquals(locations.length, list.size());
        for (Party location : locations) {
            assertTrue(exists(location, list));
        }
    }

    /**
     * Determines if there is a dispatcher for the specified location.
     *
     * @param location    the location
     * @param dispatchers the dispatchers
     * @return {@code true} if there is a dispatcher, otherwise {@code false}
     */
    private boolean exists(Party location, List<QueueDispatcher> dispatchers) {
        for (QueueDispatcher dispatcher : dispatchers) {
            if (dispatcher.getLocation().equals(location)) {
                return true;
            }
        }
        return false;
    }


}
