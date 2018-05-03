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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.model.ServiceBusConfig;
import org.openvpms.smartflow.model.event.Event;
import org.openvpms.smartflow.model.event.NotesEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ScheduledDispatcher}.
 *
 * @author Tim Anderson
 */
public class ScheduledDispatcherTestCase extends AbstractDispatcherTest {

    /**
     * The transaction manager.
     */
    @Autowired
    PlatformTransactionManager transactionManager;

    /**
     * The patient rules.
     */
    @Autowired
    PatientRules rules;

    /**
     * The queue dispatcher factory.
     */
    private TestQueueDispatcherFactory factory;

    /**
     * The practice service.
     */
    private PracticeService practiceService;

    /**
     * The scheduled dispatcher.
     */
    private ScheduledDispatcher dispatcher;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        FlowSheetServiceFactory sfsFactory = new FlowSheetServiceFactory("http://bogus", "1", getArchetypeService(),
                                                                         getLookupService(),
                                                                         Mockito.mock(DocumentHandlers.class),
                                                                         Mockito.mock(MedicalRecordRules.class));
        Party practice = TestHelper.getPractice();
        practiceService = Mockito.mock(PracticeService.class);
        when(practiceService.getServiceUser()).thenReturn(new User());
        when(practiceService.getPractice()).thenReturn(practice);

        factory = new TestQueueDispatcherFactory(sfsFactory, getArchetypeService(), getLookupService(),
                                                 transactionManager, practiceService, rules);
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        if (dispatcher != null) {
            dispatcher.destroy();
        }
    }

    /**
     * Tests dispatching.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDispatch() throws Exception {
        // set up some locations and their corresponding queues
        Party location1 = createLocation("location1", "A");
        Party location2 = createLocation("location2", "B");
        Party location3 = createLocation("location2", "C");
        TestQueue queue1 = new TestQueue(5);
        TestQueue queue2 = new TestQueue(10);
        TestQueue queue3 = new TestQueue(2);
        factory.setQueue(location1, queue1);
        factory.setQueue(location2, queue2);
        factory.setQueue(location3, queue3);

        // set up the dispatchers. Only register location1, and location2 initially.
        QueueDispatchers dispatchers = new QueueDispatchers(factory);
        dispatchers.add(location1);
        dispatchers.add(location2);

        // set up the scheduled dispatcher with a 1 second poll interval
        dispatcher = new ScheduledDispatcher(dispatchers, practiceService);
        dispatcher.setPollInterval(1);
        dispatcher.dispatch();
        Thread.sleep(2000);

        // verify the queues have been read
        assertEquals(5, queue1.getRead());
        assertEquals(10, queue2.getRead());
        assertEquals(0, queue3.getRead());  // location3 not yet registered

        // add some more messages to queue1
        queue1.addMessages(10);
        Thread.sleep(2000);

        // verify the queues have been read
        assertEquals(15, queue1.getRead());
        assertEquals(10, queue2.getRead());
        assertEquals(0, queue3.getRead());

        // now register location3
        dispatchers.add(location3);
        Thread.sleep(2000);

        // verify the queues have been read
        assertEquals(15, queue1.getRead());
        assertEquals(10, queue2.getRead());
        assertEquals(2, queue3.getRead());
    }

    /**
     * Verifies that the dispatcher continues to dispatch messages after failure.
     *
     * @throws Exception for any eror
     */
    @Test
    public void testFailure() throws Exception {
        Party location1 = createLocation("location1", "A");
        Party location2 = createLocation("location2", "B");
        TestQueue queue1 = new TestQueue(5);
        TestQueue queue2 = new TestQueue(10);

        // set up the queues to fail after the 1st and 5th message respectively.
        queue1.setFail(1);
        queue2.setFail(5);
        factory.setQueue(location1, queue1);
        factory.setQueue(location2, queue2);

        QueueDispatchers dispatchers = new QueueDispatchers(factory);
        dispatchers.add(location1);
        dispatchers.add(location2);
        dispatcher = new ScheduledDispatcher(dispatchers, practiceService);
        dispatcher.setPollInterval(1);
        dispatcher.setFailureInterval(1);
        dispatcher.dispatch();
        Thread.sleep(4000);

        // verify the queues have been read
        assertEquals(5, queue1.getRead());
        assertEquals(10, queue2.getRead());
    }

    /**
     * A test queue that returns up to {@code count} messages.
     */
    private static class TestQueue implements Queue {

        /**
         * The total number of messages in the queue.
         */
        private int count;

        /**
         * The number of read messages.
         */
        private int read = 0;

        /**
         * The message to throw an exception at, or {@code -1} if no exception should be thrown.
         */
        private int fail = -1;


        /**
         * Constructs a {@link TestQueue}.
         *
         * @param count the initial number of messages in the queue
         */
        public TestQueue(int count) {
            this.count = count;
        }

        /**
         * Returns the next message from the queue.
         * <br/>
         * If the number of read messages == {@link #setFail(int) fail}, a {@code ServiceException} will be thrown,
         * and {@link #setFail(int) fail} reset to {@code -1}, allowing the next call to succeed.
         *
         * @return the next message, or {@code null} if there are no more messages.
         * @throws ServiceException if a service exception is encountered
         */
        @Override
        public synchronized BrokeredMessage next() throws ServiceException {
            if (read < count) {
                if (read == fail) {
                    fail = -1;
                    throw new ServiceException("Simulated failure");
                }
                ++read;
                return new BrokeredMessage();
            }
            return null;
        }

        /**
         * Add messages to the queue.
         *
         * @param count the number of messages to add
         */
        public synchronized void addMessages(int count) {
            this.count += count;
        }

        /**
         * Returns the number of read messages.
         *
         * @return the number of read messages
         */
        public synchronized int getRead() {
            return read;
        }

        /**
         * Sets the message to fail at.
         *
         * @param fail the message to fail at, or {@code -1} to not fail
         */
        public synchronized void setFail(int fail) {
            this.fail = fail;
        }

        /**
         * Removes the message from the queue.
         *
         * @param message the message to remove
         * @throws ServiceException if a service exception is encountered
         */
        @Override
        public void remove(BrokeredMessage message) throws ServiceException {

        }
    }

    /**
     * A {@link QueueDispatcherFactory} that allows {@link Queue} instances to be registered for particular locations,
     * to be used when a {@link QueueDispatcher} is created.
     */
    private class TestQueueDispatcherFactory extends QueueDispatcherFactory {

        /**
         * The queues, keyed on location.
         */
        private Map<Party, Queue> queues = new HashMap<>();

        /**
         * Constructs a {@link TestQueueDispatcherFactory}.
         *
         * @param factory            the factory for SFS services
         * @param service            the archetype service
         * @param lookups            the lookup service
         * @param transactionManager the transaction manager
         * @param practiceService    the practice service
         * @param rules              the patient rules
         */
        public TestQueueDispatcherFactory(FlowSheetServiceFactory factory, IArchetypeService service,
                                          ILookupService lookups, PlatformTransactionManager transactionManager,
                                          PracticeService practiceService, PatientRules rules) {
            super(factory, service, lookups, transactionManager, practiceService, rules);
        }

        /**
         * Registers a queue for the specified location.
         *
         * @param location the location
         * @param queue    the queue
         */
        public void setQueue(Party location, Queue queue) {
            queues.put(location, queue);
        }

        @Override
        public QueueDispatcher createQueueDispatcher(final Party location) {
            ServiceBusConfig config = new ServiceBusConfig();
            EventDispatcher dispatcher = createEventDispatcher(location);
            return new QueueDispatcher(location, config, dispatcher, new ObjectMapper(), transactionManager) {
                @Override
                protected Queue createQueue(String connectionString, String queueName) {
                    Queue queue = queues.get(location);
                    return queue != null ? queue : Mockito.mock(Queue.class);
                }

                @Override
                protected Event getEvent(BrokeredMessage message) throws IOException, ServiceException {
                    return new NotesEvent();
                }
            };
        }

    }

}
