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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.workflow.messaging;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.webcontainer.ContainerContext;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.archetype.rules.workflow.MessageStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.component.im.query.ParticipantConstraint;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


/**
 * Monitors updates to <em>act.userMessage</em> and <em>act.systemMessage</em> acts, and notifies registered listeners.
 *
 * @author Tim Anderson
 */
public class MessageMonitor {

    public static interface MessageListener {

        void onMessage(Act message);

    }

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The interval to poll the server for new messages, in seconds.
     */
    private int pollInterval = DEFAULT_POLL_INTERVAL;

    /**
     * The listeners.
     */
    private Map<IMObjectReference, List<Listener>> listeners = new HashMap<IMObjectReference, List<Listener>>();

    /**
     * The default interval to poll the server for new messages, in seconds.
     */
    private static final int DEFAULT_POLL_INTERVAL = 30;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MessageMonitor.class);


    /**
     * Constructs a {@link MessageMonitor}.
     *
     * @param service the archetype service
     */
    public MessageMonitor(IArchetypeService service) {
        this.service = service;
        IArchetypeServiceListener listener = new AbstractArchetypeServiceListener() {
            public void saved(IMObject object) {
                onMessage((Act) object);
            }
        };
        service.addListener(MessageArchetypes.USER, listener);
        service.addListener(MessageArchetypes.SYSTEM_MESSAGES, listener);
    }

    /**
     * Determines if there are any unread (i.e <em>PENDING</em>) messages for a user.
     *
     * @param user the user
     * @return {@code true} if there are unread messages; otherwise {@code false}
     */
    public boolean hasNewMessages(User user) {
        ArchetypeQuery query = new ArchetypeQuery(MessageQuery.ARCHETYPES, true, true);
        query.add(Constraints.eq("status", MessageStatus.PENDING));
        query.add(new ParticipantConstraint("to", "participation.user", user));
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<Act>(query);
        return iterator.hasNext();
    }

    /**
     * Adds a listener.
     *
     * @param user     the user to add the listener for
     * @param listener the listener
     */
    public synchronized void addListener(User user, MessageListener listener) {
        IMObjectReference userRef = user.getObjectReference();
        List<Listener> userListeners = listeners.get(userRef);
        if (userListeners == null) {
            userListeners = new ArrayList<Listener>();
            listeners.put(userRef, userListeners);
        } else {
            purge(userListeners);
        }
        userListeners.add(new Listener(listener, pollInterval));
    }

    /**
     * Sets the interval to poll the server for new messages, in seconds.
     * <p/>
     * Note that the poll interval of existing listeners will not change.
     *
     * @param interval the interval, in seconds
     */
    public void setPollInterval(int interval) {
        if (interval > 0) {
            pollInterval = interval;
        }
    }

    /**
     * Removes a listener.
     *
     * @param user     the user to remove the listener for
     * @param listener the listener
     */
    public synchronized void removeListener(User user, MessageListener listener) {
        IMObjectReference userRef = user.getObjectReference();
        List<Listener> userListeners = listeners.get(userRef);
        if (userListeners != null) {
            purge(userListeners);
            for (ListIterator<Listener> iter = userListeners.listIterator(); iter.hasNext(); ) {
                Listener state = iter.next();
                if (ObjectUtils.equals(state.getMessageListener(), listener)) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    /**
     * Invoked when a message is updated.
     * </p>
     * This notifies any registered listeners that the message is addressed to.
     *
     * @param message the message
     */
    private void onMessage(Act message) {
        Listener[] array = getListeners(message);
        if (array != null) {
            for (Listener listener : array) {
                listener.queue(message);
            }
        }
    }

    /**
     * Returns the listeners registered for the <em>"to"</em> user of the message.
     *
     * @param message the message
     * @return the registered, listeners, or {@code null} if none are registered
     */
    private synchronized Listener[] getListeners(Act message) {
        Listener[] result = null;
        ActBean bean = new ActBean(message, service);
        IMObjectReference to = bean.getNodeParticipantRef("to");
        if (to != null) {
            List<Listener> list = listeners.get(to);
            if (list != null) {
                purge(list);
                result = list.toArray(new Listener[list.size()]);
            }
        }
        return result;
    }

    /**
     * Removes any listeners that have been garbage collected.
     *
     * @param listeners the listeners
     */
    private void purge(List<Listener> listeners) {
        for (ListIterator<Listener> iter = listeners.listIterator(); iter.hasNext(); ) {
            Listener listener = iter.next();
            if (!listener.active()) {
                listener.destroy();
                iter.remove();
            }
        }
    }

    private static class Listener {

        /**
         * Reference to the application.
         */
        final WeakReference<ApplicationInstance> appRef;

        /**
         * Reference to the listener.
         */
        final WeakReference<MessageListener> listenerRef;

        /**
         * Application task queue.
         */
        final TaskQueueHandle taskQueue;

        /**
         * Hash code for the listener.
         */
        final int hashCode;

        /**
         * Constructs a {@link Listener}.
         *
         * @param listener     the listener to delegate messages to
         * @param pollInterval the interval to poll the server, in seconds
         */
        public Listener(MessageListener listener, int pollInterval) {
            ApplicationInstance app = ApplicationInstance.getActive();
            if (app == null) {
                throw new IllegalStateException("No current ApplicationInstance");
            }
            appRef = new WeakReference<ApplicationInstance>(app);
            listenerRef = new WeakReference<MessageListener>(listener);
            taskQueue = app.createTaskQueue();
            ContainerContext context
                    = (ContainerContext) app.getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);

            if (context != null) {
                context.setTaskQueueCallbackInterval(taskQueue, pollInterval * 1000);
            }
            hashCode = listener.hashCode();
        }

        /**
         * Returns the message listener.
         *
         * @return the message listener, or {@code null} if it has been garbage collected
         */
        public MessageListener getMessageListener() {
            return listenerRef.get();
        }

        public boolean active() {
            return appRef.get() != null && getMessageListener() != null;
        }

        public void queue(final Act message) {
            ApplicationInstance app = appRef.get();
            if (app != null) {
                app.enqueueTask(taskQueue, new Runnable() {
                    public void run() {
                        MessageListener l = getMessageListener();
                        if (l != null) {
                            try {
                                l.onMessage(message);
                            } catch (Throwable exception) {
                                log.error("MessageListener threw exception, ignoring", exception);
                            }
                        }
                    }
                });
            }
        }

        /**
         * Destroys this listener.
         */
        public void destroy() {
            ApplicationInstance app = appRef.get();
            if (app != null) {
                app.removeTaskQueue(taskQueue);
            }
        }

        /**
         * Returns a hash code value for the object.
         */
        @Override
        public int hashCode() {
            return hashCode;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Listener) {
                Listener other = (Listener) obj;
                return ObjectUtils.equals(listenerRef.get(), other.listenerRef.get());
            }
            return false;
        }

    }
}
