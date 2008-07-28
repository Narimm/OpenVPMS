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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DOState {

    private final IMObjectDO object;
    private IMObject source;
    private final boolean isNew;
    private long version;

    private List<DeferredAssembler> deferred;
    private Map<IMObjectDO, DOState> states;
    private List<ReferenceUpdater> updaters;

    public DOState(IMObjectDO object) {
        this(object, null);
    }

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

    public IMObjectDO getObject() {
        return object;
    }

    public IMObject getSource() {
        return source;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
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
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return object.hashCode();
    }

    public void addState(DOState state) {
        if (states == null) {
            states = new LinkedHashMap<IMObjectDO, DOState>();
        }
        states.put(state.getObject(), state);
    }

    public void removeState(IMObjectDO object) {
        if (states != null) {
            states.remove(object);
        }
    }

    public void addDeferred(DeferredAssembler assembler) {
        if (deferred == null) {
            deferred = new ArrayList<DeferredAssembler>();
        }
        deferred.add(assembler);
    }

    public Set<DeferredAssembler> getDeferred() {
        DeferredCollector collector = new DeferredCollector();
        visit(collector);
        return collector.getAssemblers();
    }

    public void addReferenceUpdater(ReferenceUpdater updater) {
        if (updaters == null) {
            updaters = new ArrayList<ReferenceUpdater>();
        }
        updaters.add(updater);
    }

    public void removeDeferred(DeferredAssembler assembler) {
        deferred.remove(assembler);
    }

    public boolean isComplete() {
        return visit(new CompleteVistor());
    }

    public void updateIds(Context context) {
        visit(new IdUpdater(context));
    }

    public void rollbackIds() {
        visit(new IdReverter());
    }

    public Set<IMObjectDO> getObjects() {
        ObjectCollector collector = new ObjectCollector();
        visit(collector);
        return collector.getObjects();
    }

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
            updaters.clear();
        }
    }

    public void destroy() {
        visit(new Cleaner());
    }

    private boolean visit(Visitor visitor) {
        return visitor.visit(this);
    }

    private static abstract class Visitor {

        private Set<DOState> visited = new HashSet<DOState>();

        public boolean visit(DOState state) {
            addVisited(state);
            boolean result = doVisit(state);
            if (result) {
                result = visitChildren(state);
            }
            return result;
        }

        protected boolean visitChildren(DOState state) {
            boolean result = true;
            Map<IMObjectDO, DOState> states = state.states;
            if (states != null) {
                for (DOState child : states.values()) {
                    if (!visited.contains(child)) {
                        if (!visit(child)) {
                            result = false;
                            break;
                        }
                    }
                }
            }
            return result;
        }

        protected void addVisited(DOState state) {
            visited.add(state);
        }

        public abstract boolean doVisit(DOState state);

    }

    private static class CompleteVistor extends Visitor {

        public boolean doVisit(DOState state) {
            List<DeferredAssembler> deferred = state.deferred;
            return !(deferred != null && !deferred.isEmpty());
        }
    }

    private static class IdUpdater extends Visitor {

        private final Context context;

        public IdUpdater(Context context) {
            this.context = context;
        }

        public boolean doVisit(DOState state) {
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

    private static class IdReverter extends Visitor {
        public boolean doVisit(DOState state) {
            if (state.isNew) {
                IMObject source = state.source;
                if (source != null) {
                    source.setId(-1);
                    source.setVersion(state.version);
                }
            }
            if (state.updaters != null) {
                for (ReferenceUpdater updater : state.updaters) {
                    updater.revert();
                }
            }
            return true;
        }
    }

    private static class DeferredCollector extends Visitor {

        private Set<DeferredAssembler> assemblers;

        private static final Set<DeferredAssembler> EMPTY
                = Collections.emptySet();

        public boolean doVisit(DOState state) {
            List<DeferredAssembler> deferred = state.deferred;
            if (deferred != null && !state.deferred.isEmpty()) {
                if (assemblers == null) {
                    assemblers = new LinkedHashSet<DeferredAssembler>();
                }
                assemblers.addAll(deferred);
            }
            return true;
        }

        public Set<DeferredAssembler> getAssemblers() {
            return (assemblers != null) ? assemblers : EMPTY;
        }
    }

    private static class Cleaner extends Visitor {

        @Override
        public boolean visit(DOState state) {
            addVisited(state);
            visitChildren(state);
            doVisit(state);
            return true;
        }

        public boolean doVisit(DOState state) {
            List<DeferredAssembler> deferred = state.deferred;
            if (deferred != null) {
                deferred.clear();
            }
            List<ReferenceUpdater> updaters = state.updaters;
            if (updaters != null) {
                updaters.clear();
            }
            Map<IMObjectDO, DOState> states = state.states;
            if (states != null) {
                states.clear();
            }
            return false;
        }
    }

    private static class ObjectCollector extends Visitor {
        Set<IMObjectDO> objects = new LinkedHashSet<IMObjectDO>();

        public Set<IMObjectDO> getObjects() {
            return objects;
        }

        @Override
        public boolean visit(DOState state) {
            addVisited(state);
            visitChildren(state);
            doVisit(state);
            return true;
        }

        public boolean doVisit(DOState state) {
            objects.add(state.getObject());
            return true;
        }
    }

}
