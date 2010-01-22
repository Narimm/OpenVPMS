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

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
    private Map<Object, Object> assembling = new IdentityHashMap<Object, Object>();

    /**
     * The references to assemble.
     */
    private List<DeferredReference> deferredRefs
            = new ArrayList<DeferredReference>();

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
        assembling.put(state, state);
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
        return assembling.containsKey(state);
    }

    /**
     * Registers an object as being assembled.
     *
     * @param object the object to register
     */
    public void addAssembling(IMObject object) {
        assembling.put(object, object);
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
        return assembling.containsKey(object);
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
        Object result = session.load(impl, reference.getId());
        return type.cast(result);
    }

    public void addDeferredReference(DeferredReference deferredReference) {
        deferredRefs.add(deferredReference);
    }

    public List<DeferredReference> getDeferredReferences() {
        return deferredRefs;
    }

    /**
     * Helper to retrieve the reference of an object. This avoids loading
     * the object if it isn't already present in the session.
     *
     * @param object the object
     * @param type   the implementation type
     * @return the object's reference. May be <tt>null</tt>
     */
    public IMObjectReference getReference(
            IMObjectDO object, Class<? extends IMObjectDOImpl> type) {
        if (Hibernate.isInitialized(object)) {
            return object.getObjectReference();
        }
        Query query = session.createQuery("select archetypeId, linkId from "
                + type.getName() + " where id=?");
        query.setParameter(0, object.getId());
        List result = query.list();
        if (!result.isEmpty()) {
            Object[] values = (Object[]) result.get(0);
            return new IMObjectReference((ArchetypeId) values[0],
                                         object.getId(), (String) values[1]);

        }
        return null;
    }

    /**
     * Helper to retrieve the references for a map of objects. This avoids
     * loading the object if it isn't already present in the session, as long
     * as the {@link IMObjectDO#getId()} method is the only method invoked.
     *
     * @param objects the objects
     * @param type    the implementation type
     * @return a map of the object ids to their corresponding references
     */
    public Map<Long, IMObjectReference> getReferences(
            Map<Long, IMObjectDO> objects,
            Class<? extends IMObjectDOImpl> type) {
        List<Long> ids = new ArrayList<Long>();
        Map<Long, IMObjectReference> result
                = new HashMap<Long, IMObjectReference>();
        for (Map.Entry<Long, IMObjectDO> entry : objects.entrySet()) {
            IMObjectDO object = entry.getValue();
            if (Hibernate.isInitialized(object)) {
                result.put(entry.getKey(), object.getObjectReference());
            } else {
                ids.add(entry.getKey());
            }
        }

        final int size = ids.size();
        if (size > 1) {
            // sort the ids so the references are retrieved in index order
            Collections.sort(ids);
        }
        int index = 0;
        while (index < size) {
            int max = index + 100 < size ? 100 : size - index;
            StringBuffer hql
                    = new StringBuffer("select id, archetypeId, linkId from ")
                    .append(type.getName()).append(" where id in (?");
            for (int i = 1; i < max; ++i) {
                hql.append(",?");
            }
            hql.append(")");
            Query query = session.createQuery(hql.toString());
            for (int i = 0; i < max; ++i) {
                query.setParameter(i, ids.get(i + index));
            }
            for (Object match : query.list()) {
                Object[] values = (Object[]) match;
                long id = (Long) values[0];
                ArchetypeId archId = (ArchetypeId) values[1];
                String linkId = (String) values[2];
                result.put(id, new IMObjectReference(archId, id, linkId));
            }
            index += max;
        }
        return result;
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
