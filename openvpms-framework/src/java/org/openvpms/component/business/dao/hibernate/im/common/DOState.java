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

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DOState {

    private final IMObjectDO object;
    private final IMObject source;

    private List<DeferredAssembler> deferred;
    private List<DOState> states;
    private List<ReferenceUpdater> updaters;

    public DOState(IMObjectDO object) {
        this(object, null);
    }

    public DOState(IMObjectDO object, IMObject source) {
        this.object = object;
        this.source = source;
    }

    public IMObjectDO getObject() {
        return object;
    }

    public IMObject getSource() {
        return source;
    }

    public void addState(DOState state) {
        if (states == null) {
            states = new ArrayList<DOState>();
        }
        states.add(state);
    }

    public void addDeferred(DeferredAssembler assembler) {
        if (deferred == null) {
            deferred = new ArrayList<DeferredAssembler>();
        }
        deferred.add(assembler);
    }

    public void addReferenceUpdater(ReferenceUpdater updater) {
        if (updaters == null) {
            updaters = new ArrayList<ReferenceUpdater>();
        }
        updaters.add(updater);
    }

    public Set<DeferredAssembler> getDeferred() {
        Set<DOState> visited = new HashSet<DOState>();
        return getDeferred(new HashSet<DeferredAssembler>(), visited);
    }

    public void removeDeferred(DeferredAssembler assembler) {
        deferred.remove(assembler);
    }

    public boolean isComplete() {
        Set<DOState> visited = new HashSet<DOState>();
        return isComplete(visited);
    }

    public void updateIds(Context context) {
        Set<DOState> visited = new HashSet<DOState>();
        updateIds(context, visited);
    }

    public void destroy() {
        Set<DOState> visited = new HashSet<DOState>();
        destroy(visited);
    }

    private boolean isComplete(Set<DOState> visited) {
        boolean result = true;
        visited.add(this);
        if (deferred != null && !deferred.isEmpty()) {
            result = false;
        } else if (states != null) {
            for (DOState state : states) {
                if (!visited.contains(state)) {
                    if (!state.isComplete(visited)) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void updateIds(Context context, Set<DOState> visited) {
        visited.add(this);
        if (source != null) {
            source.setId(object.getId());
            source.setVersion(object.getVersion());
        }
        if (updaters != null) {
            for (ReferenceUpdater updater  : updaters) {
                DOState state = context.getCached(updater.getReference());
                if (state != null) {
                    updater.update(state.getObject().getObjectReference());
                }
            }
        }
        if (states != null) {
            for (DOState state : states) {
                if (!visited.contains(state)) {
                    state.updateIds(context, visited);
                }
            }
        }
    }

    private Set<DeferredAssembler> getDeferred(Set<DeferredAssembler> set,
                                               Set<DOState> visited) {
        visited.add(this);
        if (deferred != null) {
            set.addAll(deferred);
        }
        if (states != null) {
            for (DOState state : states) {
                if (!visited.contains(state)) {
                    state.getDeferred(set, visited);
                }
            }
        }
        return set;
    }

    private void destroy(Set<DOState> visited) {
        visited.add(this);
        if (deferred != null) {
            deferred.clear();
        }
        if (updaters != null) {
            updaters.clear();
        }
        if (states != null && !states.isEmpty()) {
            for (DOState state : states) {
                if (!visited.contains(state)) {
                    state.destroy(visited);
                }
            }
        }
    }

}
