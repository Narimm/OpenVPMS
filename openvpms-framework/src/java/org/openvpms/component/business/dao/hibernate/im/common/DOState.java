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

import org.hibernate.StaleObjectStateException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Manages the state of an {@link IMObjectDO} as it is being assembled by an
 * {@link Assembler}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DOState {

    /**
     * The object.
     */
    private final IMObjectDO object;

    /**
     * The source object. May be <tt>null<tt>.
     */
    private IMObject source;

    /**
     * Determines if the object was new when the state was first created.
     */
    private final boolean isNew;

    /**
     * The source version.
     */
    private long version;

    /**
     * The deferred assemblers.
     */
    private List<DeferredAssembler> deferred;

    /**
     * The reference updaters.
     */
    private List<ReferenceUpdater> updaters;

    /**
     * The reference update reverters, used to revert reference updates
     * on transaction rollback
     */
    private Map<IMObjectReference, ReferenceUpdater> reverters;

    /**
     * Child states of this state.
     */
    private Map<String, DOState> states;

    /**
     * Creates a new <tt>DOState</tt> for an object retrieved from the
     * database.
     *
     * @param object the object
     */
    public DOState(IMObjectDO object) {
        this(object, null);
    }

    /**
     * Creates a new <tt>DOState</tt> for an object being assembled
     * from an {@link IMObject}.
     *
     * @param object the object
     * @param source the source object
     */
    public DOState(IMObjectDO object, IMObject source) {
        this.object = object;
        this.source = source;
        isNew = (source != null) && source.isNew();
        version = (source != null) ? source.getVersion() : 0;
        if (!isNew && source != null) {
            if (source.getVersion() != object.getVersion()) {
                throw new StaleObjectStateException(object.getClass().getName(),
                                                    object.getId());
            }
        }
    }

    /**
     * Returns the object.
     *
     * @return the object
     */
    public IMObjectDO getObject() {
        return object;
    }

    /**
     * Returns the source object.
     *
     * @return the source object, or <tt>null</tt> if the object is not
     *         being assembled from an {@link IMObject}.
     */
    public IMObject getSource() {
        return source;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p/>
     * Note that this uses the underlying {@link #getObject() object} to perform
     * equality. If the object has been lazily loaded, this will force it to
     * load.
     *
     * @param other the reference object with which to compare.
     * @return <tt>true</tt> if this object is the same as the obj
     *         argument; <tt>false</tt> otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof DOState) {
            return object.equals(((DOState) other).object);
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     * <p/>
     * Note that this uses the underlying {@link #getObject() object} to
     * calculate the hash code. If the object has been lazily loaded, this will
     * force it to load.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return object.hashCode();
    }

    /**
     * Adds a state that is related to this state.
     *
     * @param state the related state
     */
    public void addState(DOState state) {
        if (states == null) {
            states = new LinkedHashMap<String, DOState>();
        } else if (!state.isUninitialised()) {
            // remove any existing mapping for the state. Only relevant if
            // the object has been loaded subsequent to the state being added
            removeState(state.getObject());
        }
        states.put(state.getKey(), state);
    }

    /**
     * Removes a related state.
     *
     * @param object the object associated with the state
     */
    public void removeState(IMObjectDO object) {
        if (states != null) {
            if (states.remove(getKey(object)) == null) {
                if (!HibernateHelper.isUnintialised(object)
                        && object.getId() != -1) {
                    // the object may have been loaded subsequent to the state
                    // being added, in which case its key has changed
                    object = (IMObjectDO) HibernateHelper.deproxy(object);
                    Class impl = object.getClass();
                    while (impl != IMObjectDOImpl.class
                            && impl != Object.class) {
                        String key = impl.getName() + "#" + object.getId();
                        if (states.remove(key) != null) {
                            break;
                        }
                        impl = impl.getSuperclass();
                    }
                }
            }
        }
    }

    /**
     * Adds a deferred assembler.
     *
     * @param assembler the assembler to add
     */
    public void addDeferred(DeferredAssembler assembler) {
        if (deferred == null) {
            deferred = new ArrayList<DeferredAssembler>();
        }
        deferred.add(assembler);
    }

    /**
     * Removes a deferred assembler.
     *
     * @param assembler the assembler to remove
     */
    public void removeDeferred(DeferredAssembler assembler) {
        deferred.remove(assembler);
    }

    /**
     * Returns the deferred assemblers.
     *
     * @return the deferred assemblers
     */
    public Set<DeferredAssembler> getDeferred() {
        DeferredCollector collector = new DeferredCollector();
        collector.visit(this);
        return collector.getAssemblers();
    }

    /**
     * Adds a reference updater.
     *
     * @param updater the reference updater to add
     */
    public void addReferenceUpdater(ReferenceUpdater updater) {
        if (updaters == null) {
            updaters = new ArrayList<ReferenceUpdater>();
        }
        updaters.add(updater);
    }

    /**
     * Determines if the object is complete. It is considered complete if
     * it or any of its related states have no deferred updaters registers.
     *
     * @return <tt>true</tt> if the object is complete; otherwise <tt>false</tt>
     */
    public boolean isComplete() {
        return new CompleteVistor().visit(this);
    }

    /**
     * Updates the identifiers and versions of the {@link IMObject}s associated
     * with this object.
     * <p/>
     * This is used to propagate identifier and version changes after a
     * transaction commits.
     *
     * @param context the context
     */
    public void updateIds(Context context) {
        new IdUpdater(context).visit(this);
    }

    /**
     * Reverts the identifiers and versions of the {@link IMObject}s associated
     * with this object.
     * <p/>
     * This is used to rollback identifier and version changes after a
     * transaction rolls back.
     */
    public void rollbackIds() {
        new IdReverter().visit(this);
    }

    /**
     * Returns the object, and any related objects added via {@link #addState}.
     *
     * @return the objects associated with the state
     */
    public Collection<IMObjectDO> getObjects() {
        ObjectCollector collector = new ObjectCollector();
        collector.visit(this);
        return collector.getObjects();
    }

    /**
     * Updates the state with a new instance of the source.
     *
     * @param source the new source instance
     * @throws StaleObjectStateException if the old and new versions aren't
     *                                   the same
     */
    public void update(IMObject source) {
        if (source.getVersion() != object.getVersion()) {
            throw new StaleObjectStateException(object.getClass().getName(),
                                                object.getId());
        }
        this.source = source;
        if (deferred != null) {
            deferred.clear();
        }
        if (updaters != null) {
            if (reverters == null) {
                reverters = new HashMap<IMObjectReference, ReferenceUpdater>();
            }
            for (ReferenceUpdater updater : updaters) {
                if (!reverters.containsKey(updater.getReference())) {
                    reverters.put(updater.getReference(), updater);
                }
            }
            updaters.clear();
        }
    }

    /**
     * Destroys the state.
     */
    public void destroy() {
        new Cleaner().visit(this);
    }

    /**
     * Determines if the object associated with the state is an object which
     * is yet to be loaded from the database.
     *
     * @return <tt>true</tt> if the object is yet to be loaded from the database
     */
    public boolean isUninitialised() {
        return HibernateHelper.isUnintialised(object);
    }

    /**
     * Returns a key for this state, to be used in sets and maps, to avoid
     * loading the underlying object from the database.
     *
     * @return a unique identifier for the state
     */
    protected String getKey() {
        return getKey(object);
    }

    /**
     * Returns a key for an object, to be used in sets and maps, to avoid
     * loading objects from the database.
     * <p/>
     * If the object is loaded, its link identifier will be used, otherwise
     * the concatenation of its persistent class name and id will be used.
     *
     * @param object the object
     * @return a unique identifier for the object
     */
    private String getKey(IMObjectDO object) {
        String id = null;
        if (object instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) object;
            LazyInitializer init = proxy.getHibernateLazyInitializer();
            if (init.isUninitialized()) {
                id = init.getPersistentClass().getName()
                        + "#" + init.getIdentifier().toString();
            }
        }
        if (id == null) {
            id = object.getLinkId();
        }
        return id;
    }

    /**
     * Helper class to visit each reachable state once and only once.
     */
    private static abstract class Visitor {

        /**
         * The visited states, used to avoid visiting a state more than once.
         */
        private Set<String> visited = new HashSet<String>();

        /**
         * Visits each state reachable from the specified state, invoking
         * {@link #doVisit(DOState)}. If the method returns <tt>false</tt>,
         * iteration terminates.
         *
         * @param state the starting state
         * @return the result of {@link #doVisit(DOState)}.
         */
        public boolean visit(DOState state) {
            addVisited(state);
            boolean result = doVisit(state);
            if (result) {
                result = visitChildren(state);
            }
            return result;
        }

        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt> to visit any other state, <tt>false</tt> to
         *         terminate
         */
        protected abstract boolean doVisit(DOState state);

        /**
         * Invokes {@link #visit(DOState)} for each child of the specified
         * state. If the method returns <tt>false</tt>, iteration terminates.
         *
         * @param state the state
         * @return the result of {@link #visit(DOState)}
         */
        protected boolean visitChildren(DOState state) {
            boolean result = true;
            Map<String, DOState> states = state.states;
            if (states != null) {
                for (DOState child : states.values()) {
                    if (!visited.contains(child.getKey())) {
                        if (!visit(child)) {
                            result = false;
                            break;
                        }
                    }
                }
            }
            return result;
        }

        /**
         * Marks a state visited.
         *
         * @param state the visited state
         */
        protected void addVisited(DOState state) {
            visited.add(state.getKey());
        }
    }

    /**
     * Visitor that determines if a state is complete. A state is complete if
     * the are no deferred assemblers associated with it or its associated
     * states.
     */
    private static class CompleteVistor extends Visitor {

        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt> to visit any other state, <tt>false</tt> to
         *         terminate
         */
        protected boolean doVisit(DOState state) {
            List<DeferredAssembler> deferred = state.deferred;
            return !(deferred != null && !deferred.isEmpty());
        }
    }

    /**
     * Visitor that updates the {@link IMObject} and the objects they reference
     * with the ids and versions of their corresponding {@link IMObjectDO}s.
     */
    private static class IdUpdater extends Visitor {

        /**
         * The context.
         */
        private final Context context;


        /**
         * Creates a new <tt>IdUpdater</tt>.
         *
         * @param context the context
         */
        public IdUpdater(Context context) {
            this.context = context;
        }

        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt>
         */
        protected boolean doVisit(DOState state) {
            IMObjectDO object = state.getObject();
            IMObject source = state.getSource();
            if (source != null) {
                source.setId(object.getId());
                source.setVersion(object.getVersion());
            }
            List<ReferenceUpdater> updaters = state.updaters;
            if (updaters != null) {
                for (ReferenceUpdater updater : updaters) {
                    DOState target = context.getCached(updater.getReference());
                    if (target != null) {
                        IMObjectReference ref
                                = target.getObject().getObjectReference();
                        updater.doUpdate(ref);
                    }
                }
            }
            return true;
        }
    }

    /**
     * Visitor that reverts the ids and versions of {@link IMObject}s and the
     * objects they reference.
     */
    private static class IdReverter extends Visitor {

        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt>
         */
        protected boolean doVisit(DOState state) {
            if (state.isNew) {
                IMObject source = state.source;
                if (source != null) {
                    source.setId(-1);
                    source.setVersion(state.version);
                }
            }
            if (state.reverters != null) {
                for (ReferenceUpdater reverter : state.reverters.values()) {
                    reverter.revert();
                }
            }
            return true;
        }
    }

    /**
     * Visitor that collects {@link DeferredAssembler}s.
     */
    private static class DeferredCollector extends Visitor {

        /**
         * The collected assemblers.
         */
        private Set<DeferredAssembler> assemblers;

        /**
         * Empty helper.
         */
        private static final Set<DeferredAssembler> EMPTY
                = Collections.emptySet();

        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt>
         */
        protected boolean doVisit(DOState state) {
            List<DeferredAssembler> deferred = state.deferred;
            if (deferred != null && !state.deferred.isEmpty()) {
                if (assemblers == null) {
                    assemblers = new LinkedHashSet<DeferredAssembler>();
                }
                assemblers.addAll(deferred);
            }
            return true;
        }

        /**
         * Returns the collected assemblers.
         *
         * @return the assemblers
         */
        public Set<DeferredAssembler> getAssemblers() {
            return (assemblers != null) ? assemblers : EMPTY;
        }
    }

    /**
     * Visitor that destroys states.
     */
    private static class Cleaner extends Visitor {

        /**
         * Visits each state reachable from the specified state, invoking
         * {@link #doVisit(DOState)}.
         *
         * @param state the starting state
         * @return <tt>true</tt>
         */
        @Override
        public boolean visit(DOState state) {
            addVisited(state);
            visitChildren(state);
            doVisit(state);
            return true;
        }

        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt>
         */
        protected boolean doVisit(DOState state) {
            List<DeferredAssembler> deferred = state.deferred;
            if (deferred != null) {
                deferred.clear();
            }
            List<ReferenceUpdater> updaters = state.updaters;
            if (updaters != null) {
                updaters.clear();
            }
            Map<IMObjectReference, ReferenceUpdater> reverters
                    = state.reverters;
            if (reverters != null) {
                reverters.clear();
            }
            Map<String, DOState> states = state.states;
            if (states != null) {
                states.clear();
            }
            return true;
        }
    }

    /**
     * Visitor that collects {@link IMObjectDO}s.
     */
    private static class ObjectCollector extends Visitor {

        /**
         * The collected objects.
         */
        private final Map<String, IMObjectDO> objects
                = new LinkedHashMap<String, IMObjectDO>();

        /**
         * Returns the collected objects.
         *
         * @return the collected objects
         */
        public Collection<IMObjectDO> getObjects() {
            return objects.values();
        }

        /**
         * Visits each state reachable from the specified state, invoking
         * {@link #doVisit(DOState)}. If the method returns <tt>false</tt>,
         * iteration terminates.
         *
         * @param state the starting state
         * @return <tt>true</tt>
         */
        @Override
        public boolean visit(DOState state) {
            addVisited(state);
            visitChildren(state);
            doVisit(state);
            return true;
        }

        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt>
         */
        protected boolean doVisit(DOState state) {
            IMObjectDO object = state.getObject();
            objects.put(state.getKey(), object);
            return true;
        }
    }

}
