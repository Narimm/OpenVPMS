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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Maintains information to be shared between {@link Assembler}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Context {

    /**
     * The assembler.
     */
    private final Assembler assembler;

    /**
     * The current hibernate session.
     */
    private final Session session;

    /**
     * Key used to bind the context with
     * <tt>TransactionSynchronizationManager.bindResource()</tt>
     */
    private final ResourceKey key;

    /**
     * A map of objects to their corresponding data object states.
     */
    private Map<IMObject, DOState> objectToDOMap
            = new HashMap<IMObject, DOState>();

    /**
     * A map of data objects to their corresponding objects.
     */
    private Map<IMObjectDO, IMObject> doToObjectMap
            = new HashMap<IMObjectDO, IMObject>();

    /**
     * A map of references to their corresponding data object states.
     */
    private Map<IMObjectReference, DOState> refToDOMap
            = new HashMap<IMObjectReference, DOState>();

    /**
     * The set of data objects that have been saved in the session.
     */
    private Set<DOState> saved = new HashSet<DOState>();

    /**
     * The set of data objects that are yet to be saved.
     */
    private Set<DOState> saveDeferred = new LinkedHashSet<DOState>();

    /**
     * The set of objects currently being assembled.
     */
    private Set<Object> assembling = new HashSet<Object>();

    /**
     * Determines if transaction synchronization is active.
     */
    private final boolean syncActive;

    /**
     * The context handler. May be <tt>null</tt>
     */
    private ContextHandler handler;

    /**
     * Creates a new <tt>Context</tt>.
     *
     * @param assembler  the assembler
     * @param session    the hibernate session
     * @param syncActive determines if transaction synchronization is active
     */
    private Context(Assembler assembler, Session session, boolean syncActive) {
        this.assembler = assembler;
        this.session = session;
        this.syncActive = syncActive;
        key = new ResourceKey(session);
    }

    /**
     * Returns the context for the given assembler and session and current
     * thread.
     * <p/>
     * If one does not exist, it will be created.
     *
     * @param assembler the assembler
     * @param session   the hibernate session
     * @return the context
     */
    public static Context getContext(Assembler assembler, Session session) {
        Context context;
        ResourceKey key = new ResourceKey(session);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            if (!TransactionSynchronizationManager.hasResource(key)) {
                context = new Context(assembler, session, true);
                TransactionSynchronizationManager.bindResource(
                        context.getResourceKey(), context);
                TransactionSynchronizationManager.registerSynchronization(
                        new ContextSynchronization(context));
            } else {
                context = (Context) TransactionSynchronizationManager.getResource(
                        key);
            }
        } else {
            context = new Context(assembler, session, false);
        }
        return context;
    }

    /**
     * Registers the context handler.
     *
     * @param handler the handler. May be <tt>null</tt>
     */
    public void setContextHandler(ContextHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns the assembler.
     *
     * @return the assembler
     */
    public Assembler getAssembler() {
        return assembler;
    }

    /**
     * Registers a data object as being assembled.
     *
     * @param state the data object state to register
     */
    public void addAssembling(DOState state) {
        assembling.add(state);
    }

    /**
     * Deregisters a data object as being assembled.
     *
     * @param state the data object state to deregister
     */
    public void removeAssembling(DOState state) {
        assembling.remove(state);
    }

    /**
     * Determines if a data object is being assembled.
     *
     * @param state the data object state
     * @return <tt>true</tt> if the object is being assembled; otherwise
     *         <tt>false</tt>
     */
    public boolean isAssembling(DOState state) {
        return assembling.contains(state);
    }

    /**
     * Registers an object as being assembled.
     *
     * @param object the object to register
     */
    public void addAssembling(IMObject object) {
        assembling.add(object);
    }

    /**
     * Deregisters an object as being assembled.
     *
     * @param object the object to deregister
     */
    public void removeAssembling(IMObject object) {
        assembling.remove(object);
    }

    /**
     * Determines if a data object is being assembled.
     *
     * @param object the object
     * @return <tt>true</tt> if the object is being assembled; otherwise
     *         <tt>false</tt>
     */
    public boolean isAssembling(IMObject object) {
        return assembling.contains(object);
    }

    /**
     * Returns the hibernate session.
     *
     * @return the hibernate session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Determines if transaction synchronization is active.
     * TODO - still required?
     *
     * @return <tt>true</tt> if synchronization is active, otherwise
     *         <tt>false</tt>
     */
    public boolean isSynchronizationActive() {
        return syncActive;
    }

    /**
     * Registers a data object as being assembled.
     *
     * @param target the object being assembled
     * @param source the object being assembled from
     */
    public void add(DOState target, IMObject source) {
        objectToDOMap.put(source, target);
        doToObjectMap.put(target.getObject(), source);
        refToDOMap.put(source.getObjectReference(), target);
    }

    /**
     * Registers an object as being assembled.
     *
     * @param target the object being assembled
     * @param source the object being assembled from
     */
    public void add(IMObject target, IMObjectDO source) {
        doToObjectMap.put(source, target);
    }

    /**
     * Removes a data object from the context and deletes it from the session.
     *
     * @param target the object to remove
     */
    public void remove(IMObjectDO target) {
        IMObject source = doToObjectMap.get(target);
        session.delete(target);
        doToObjectMap.remove(target);
        if (source != null) {
            DOState state = objectToDOMap.get(source);
            objectToDOMap.remove(source);
            refToDOMap.remove(source.getObjectReference());
            if (state != null) {
                saveDeferred.remove(state);
            }
        }
    }

    /**
     * Returns the assembled data object for the specified <tt>IMObject</tt>.
     *
     * @param source the source object
     * @return the assembled state, or <tt>null</tt> if none is found
     */
    public DOState getCached(IMObject source) {
        return objectToDOMap.get(source);
    }

    /**
     * Returns the assembled <tt>IMObject</tt> for the specified data object.
     *
     * @param source the data object
     * @return the corresponding <tt>IMObject</tt> or <tt>null<tt> if none is
     *         found
     */
    public IMObject getCached(IMObjectDO source) {
        return doToObjectMap.get(source);
    }

    /**
     * Returns the assembled data object state for the specified object
     * reference.
     *
     * @param reference the reference
     * @return the corresponding state, or <tt>null</tt> if none is found
     */
    public DOState getCached(IMObjectReference reference) {
        return refToDOMap.get(reference);
    }

    /**
     * Retrieves a data object given its reference.
     *
     * @param reference the reference
     * @param type      the data object type
     * @param impl      the data object implementation type
     * @return the corresponding object, or <tt>null</tt> if none is found
     */
    public <T extends IMObjectDO, Impl extends IMObjectDOImpl> T
            get(IMObjectReference reference, Class<T> type, Class<Impl> impl) {
        Object result = session.get(impl, reference.getId());
        if (result instanceof HibernateProxy) {
            HibernateProxy proxy = ((HibernateProxy) result);
            result = proxy.getHibernateLazyInitializer().getImplementation();
        }

        return type.cast(result);
    }

    /**
     * Destroys the context, releasing resources.
     */
    public void destroy() {
        for (DOState state : objectToDOMap.values()) {
            state.destroy();
        }
        objectToDOMap.clear();
        doToObjectMap.clear();
        refToDOMap.clear();
        saved.clear();
        saveDeferred.clear();
    }

    /**
     * Register a state whose save is deferred.
     *
     * @param state the state to register
     */
    public void addSaveDeferred(DOState state) {
        saveDeferred.add(state);
    }

    /**
     * Returns the set of states whose save is deferred.
     *
     * @return the states
     */
    public Set<DOState> getSaveDeferred() {
        return saveDeferred;
    }

    /**
     * Removes state from the set of save deferred states.
     *
     * @param state the state to remove
     */
    public void removeSaveDeferred(DOState state) {
        saveDeferred.remove(state);
    }

    /**
     * Registers a state as being saved.
     *
     * @param state the state to register
     */
    public void addSaved(DOState state) {
        saved.add(state);
    }

    /**
     * Returns the set of saved states.
     *
     * @return the saved states
     */
    public Set<DOState> getSaved() {
        return saved;
    }

    /**
     * Returns the resource key.
     *
     * @return the resource key
     */
    private Object getResourceKey() {
        return key;
    }

    /**
     * Helper class to trigger {@link ContextHandler} events.
     */
    private static class ContextSynchronization
            extends TransactionSynchronizationAdapter {

        private final Context context;

        public ContextSynchronization(Context context) {
            this.context = context;
        }

        @Override
        public void suspend() {
            TransactionSynchronizationManager.unbindResource(
                    context.getResourceKey());
        }

        @Override
        public void resume() {
            TransactionSynchronizationManager.bindResource(
                    context.getResourceKey(), context);
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            ContextHandler handler = context.handler;
            if (handler != null) {
                handler.preCommit(context);
            }
        }

        @Override
        public void afterCompletion(int status) {
            TransactionSynchronizationManager.unbindResource(
                    context.getResourceKey());
            ContextHandler handler = context.handler;
            if (handler != null) {
                if (status == STATUS_COMMITTED) {
                    handler.commit(context);
                } else {
                    handler.rollback(context);
                }
            }

            context.destroy();
        }

    }

    /**
     * Helper class for binding the context with
     * <tt>TransactionSynchronizationManager</tt>.
     */
    private static class ResourceKey {

        /**
         * The session.
         */
        private final Session session;


        /**
         * Creates a new <tt>ResourceKey</tt>.
         *
         * @param session the session
         */
        public ResourceKey(Session session) {
            this.session = session;
        }

        /**
         * Returns a hash code value for the object.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            return session.hashCode();
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return <tt>true</tt> if this object is the same as the obj
         *         argument; <tt>false</tt> otherwise.                               
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ResourceKey) {
                return session.equals(((ResourceKey) obj).session);
            }
            return false;
        }

    }
}
