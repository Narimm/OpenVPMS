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

package org.openvpms.tools.data.loader;

import com.thoughtworks.xstream.converters.basic.DateConverter;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import static org.openvpms.tools.data.loader.ArchetypeDataLoaderException.ErrorCode.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Manages the state of an {@link IMObject} being loaded by the
 * {@link DataLoader}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LoadState {

    /**
     * The id prefix.
     */
    public static final String ID_PREFIX = "id:";

    /**
     * The object.
     */
    private final IMObject object;

    /**
     * Child states of this state.
     */
    private final List<LoadState> childStates = new ArrayList<LoadState>();

    /**
     * The parent state. If <tt>null</tt> indicates there is no parent.
     */
    private final LoadState parent;

    /**
     * The object's archetype descriptor.
     */
    private final ArchetypeDescriptor descriptor;

    /**
     * A map of object identifiers to their corresponding deferred updaters.
     */
    private Map<String, DeferredUpdater> deferred
            = new HashMap<String, DeferredUpdater>();

    /**
     * Cache of local and child deferred updaters.
     */
    private Map<Integer, DeferredUpdater> deferredCache
            = new HashMap<Integer, DeferredUpdater>();

    /**
     * A map of unsaved references to their corresponding updaters.
     */
    private Map<IMObjectReference, UnsavedRefUpdater> unsaved
            = new HashMap<IMObjectReference, UnsavedRefUpdater>();

    /**
     * Cache of local and child unsaved references.
     */
    private Set<IMObjectReference> unsavedCache
            = new HashSet<IMObjectReference>();

    /**
     * The path that the object came from.
     */
    private final String path;

    /**
     * The line no. that the object came from.
     */
    private final int lineNo;


    /**
     * Creates a new <tt>LoadState</tt>.
     *
     * @param parent     the parent state. May be <tt>null</tt>
     * @param object     the object
     * @param descriptor the object's archetype descriptor
     * @param path       the path that the object came from
     * @param lineNo     the line number that the object came from
     */
    public LoadState(LoadState parent, IMObject object,
                     ArchetypeDescriptor descriptor, String path, int lineNo) {
        this.parent = parent;
        this.object = object;
        this.descriptor = descriptor;
        this.path = path;
        this.lineNo = lineNo;
    }

    /**
     * Returns the parent state.
     *
     * @return the parent state, or <tt>null</tt> if there is no parent
     */
    public LoadState getParent() {
        return parent;
    }

    /**
     * Returns the object.
     *
     * @return the object
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the archetype identifier.
     *
     * @return the archetype identifier
     */
    public ArchetypeId getArchetypeId() {
        return object.getArchetypeId();
    }

    /**
     * Sets a node value.
     *
     * @param name    the node name
     * @param value   the node value
     * @param context the load context
     */
    public void setValue(String name, String value, LoadContext context) {
        NodeDescriptor node = getNode(name);

        try {
            if (value.startsWith(ID_PREFIX)) {
                if (node.isCollection()) {
                    value = LoadCache.stripPrefix(value);
                    addChild(node, value, context);
                } else {
                    setReference(node, value, context);
                }
            } else if (node.isDate()) {
                node.setValue(object, getDate(value));
            } else {
                node.setValue(object, value);
            }
        } catch (Exception exception) {
            throw new ArchetypeDataLoaderException(
                    FailedToSetAtribute, exception, name, value, lineNo);
        }
    }

    /**
     * Returns the set of deferred updaters associated with this state and
     * any child state.
     *
     * @return the set of deferred updaters
     */
    public Collection<DeferredUpdater> getDeferred() {
        return deferredCache.values();
    }

    /**
     * Returns the object's archetype descriptor.
     *
     * @return the archetype descriptor
     */
    public ArchetypeDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Determines if this state is complete.
     *
     * @return <tt>true</tt> if this state and its related states don't have
     *         any deferred updaters
     */
    public boolean isComplete() {
        return deferredCache.isEmpty();
    }

    /**
     * Addsa a child object to the specified collection.
     *
     * @param collection the collection node name
     * @param child      the child object
     */
    public void addChild(String collection, final LoadState child) {
        final NodeDescriptor node = getNode(collection);
        if (!node.isCollection()) {
            throw new ArchetypeDataLoaderException(
                    ParentNotACollection, lineNo);
        }
        if (child.isComplete()) {
            node.addChildToCollection(object, child.getObject());
            childStates.add(child);
        } else {
            DeferredUpdater updater = child.getDeferred().iterator().next();
            DeferredUpdater childUpdater
                    = new DeferredUpdater(updater.getId()) {
                public boolean update(IMObjectReference reference,
                                      LoadContext context) {
                    if (child.isComplete()) {
                        node.addChildToCollection(object, child.getObject());
                        childStates.add(child);
                        removeDeferred(getId());
                        return true;
                    }
                    return false;
                }
            };
            addDeferred(childUpdater);
        }
    }

    /**
     * Adds a child object to the specified collection.
     *
     * @param collection the collection node name
     * @param id         the child object identifier
     * @param context    the load context
     */
    public void addChild(String collection, String id,
                         LoadContext context) {
        NodeDescriptor node = getNode(collection);
        if (!node.isCollection()) {
            throw new ArchetypeDataLoaderException(
                    ParentNotACollection, lineNo);
        }

        id = LoadCache.stripPrefix(id);
        addChild(node, id, context);
    }

    /**
     * Returns the set of unsaved object references reachable from this state.
     *
     * @return the set of unsaved object references
     */
    public Set<IMObjectReference> getUnsaved() {
        return unsavedCache;
    }

    /**
     * Update any unsaved references matching that supplied.
     *
     * @param reference the saved reference
     */
    public void update(IMObjectReference reference) {
        UnsavedUpdaterVisitor visitor = new UnsavedUpdaterVisitor(reference);
        visitor.visit(this);
    }

    /**
     * Removes an {@link UnsavedRefUpdater} for the specified reference.
     *
     * @param reference the reference
     */
    public void removeUnsaved(IMObjectReference reference) {
        unsaved.remove(reference);
        removeUnsavedCache(reference);
    }

    /**
     * Returns the path that this state was read from.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the line number that this state was read from.
     *
     * @return the line number
     */
    public int getLineNumber() {
        return lineNo;
    }

    /**
     * Sets a reference from an object identifier.
     * <p/>
     * If the reference can't be resolved, a {@link DeferredUpdater} is
     * registered for it.
     * <p/>
     * If the reference is unsaved, an {@link UnsavedRefUpdater} is registered.
     *
     * @param descriptor the node descriptor
     * @param id         the object identifier
     * @param context    the load context
     * @return <tt>true</tt> if the reference was set
     */
    private boolean setReference(final NodeDescriptor descriptor,
                                 final String id, LoadContext context) {
        boolean result = false;
        final IMObjectReference ref = context.getReference(id);
        if (ref == null) {
            if (!deferred.containsKey(id)) {
                addDeferred(new DeferredUpdater(id) {
                    public boolean update(IMObjectReference reference,
                                          LoadContext context) {
                        if (setReference(descriptor, id, context)) {
                            removeDeferred(getId());
                            return true;
                        }
                        return false;
                    }
                });
            }
        } else {
            descriptor.setValue(object, ref);
            result = true;
            if (ref.isNew()) {
                if (unsaved == null) {
                    unsaved = new HashMap<IMObjectReference,
                            UnsavedRefUpdater>();
                }
                addUnsaved(ref, descriptor);
            }
        }
        return result;
    }

    /**
     * Adds a child object to the specified collection.
     *
     * @param node    the collection node descriptor
     * @param id      the object identifier
     * @param context the load context
     * @return <tt>true</tt> if the object was added
     */
    private boolean addChild(final NodeDescriptor node, final String id,
                             LoadContext context) {
        boolean result = false;
        IMObject child = context.getObject(id);
        if (child != null) {
            node.addChildToCollection(object, child);
            result = true;

        } else {
            addDeferred(new DeferredUpdater(id) {
                public boolean update(IMObjectReference reference,
                                      LoadContext context) {
                    if (addChild(node, id, context)) {
                        removeDeferred(id);
                        return true;
                    }
                    return false;
                }
            });
        }
        return result;
    }

    /**
     * Returns the node descriptor with the specified name.
     *
     * @param name the node name
     * @return the corresponding node descriptor
     */
    private NodeDescriptor getNode(String name) {
        NodeDescriptor ndesc = descriptor.getNodeDescriptor(name);
        if (ndesc == null) {
            throw new ArchetypeDataLoaderException(
                    InvalidAttribute, lineNo, name, descriptor.getType());
        }
        return ndesc;
    }

    /**
     * Adds a deferred updater.
     *
     * @param updater the deferred upadtor
     */
    private void addDeferred(DeferredUpdater updater) {
        String id = updater.getId();
        removeDeferred(id);
        deferred.put(id, updater);
        addDeferredCache(updater);
    }

    /**
     * Adds a deferred updater to the cache, and propagates it to the parent,
     * if present.
     *
     * @param updater the deferred updater
     */
    private void addDeferredCache(DeferredUpdater updater) {
        deferredCache.put(System.identityHashCode(updater), updater);
        if (parent != null) {
            parent.addDeferredCache(updater);
        }
    }

    /**
     * Removes a deferred updater.
     *
     * @param id the id of the updater
     */
    private void removeDeferred(String id) {
        DeferredUpdater updater = deferred.remove(id);
        if (updater != null) {
            removeDeferredCache(updater);
        }
    }

    /**
     * Removes a deferred updater from the cache, and propagates the removal
     * to the parent, if present.
     *
     * @param updater the updater to remove
     */
    private void removeDeferredCache(DeferredUpdater updater) {
        deferredCache.remove(System.identityHashCode(updater));
        if (parent != null) {
            parent.removeDeferredCache(updater);
        }
    }

    /**
     * Adds a new unsaved reference updater.
     *
     * @param ref        the reference to update
     * @param descriptor the node descriptor
     */
    private void addUnsaved(IMObjectReference ref, NodeDescriptor descriptor) {
        unsaved.put(ref, new UnsavedRefUpdater(this, ref, descriptor));
        addUnsavedCache(ref);
    }

    /**
     * Adds an unsaved reference to the cache, and propagates it to the
     * parent, if present.
     *
     * @param reference the reference to add
     */
    private void addUnsavedCache(IMObjectReference reference) {
        unsavedCache.add(reference);
        if (parent != null) {
            parent.addUnsavedCache(reference);
        }
    }

    /**
     * Removes an unsaved reference updater from the cache, and propagates
     * the removal to the parent, if present.
     *
     * @param reference the reference of the updater to remove
     */
    private void removeUnsavedCache(IMObjectReference reference) {
        unsavedCache.remove(reference);
        if (parent != null) {
            parent.removeUnsaved(reference);
        }
    }

    /**
     * Helper to convert a string to a date.
     *
     * @param value the value to convert. May be <tt>null</tt>
     * @return the converted value, or <tt>null</tt>
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private Date getDate(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String defaultFormat = "yyyy-MM-dd HH:mm:ss.S z";
        String[] acceptableFormats = new String[]{
                "yyyy-MM-dd HH:mm:ss.S a",
                "yyyy-MM-dd HH:mm:ssz", "yyyy-MM-dd HH:mm:ss z",
                "yyyy-MM-dd HH:mm:ssa",
                "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy"};
        // the yyyy-mm-dd* formats are the DateConverter defaults.
        // Add dd/MM/yyyy formats for backwards compatibility with existing
        // data
        DateConverter converter = new DateConverter(defaultFormat,
                                                    acceptableFormats);
        return (Date) converter.fromString(value);
    }

    /**
     * Helper class to visit each reachable state once and only once.
     */
    private static abstract class Visitor {

        /**
         * The visited states.
         */
        private Set<LoadState> visited = new HashSet<LoadState>();


        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt> to visit any other state, <tt>false</tt> to
         *         terminate
         */
        protected abstract boolean doVisit(LoadState state);

        /**
         * Visits each state reachable from the specified state, invoking
         * {@link #doVisit(LoadState)}. If the method returns <tt>false</tt>,
         * iteration terminates.
         *
         * @param state the starting state
         * @return the result of {@link #doVisit(LoadState)}.
         */
        public boolean visit(LoadState state) {
            addVisited(state);
            boolean result = doVisit(state);
            if (result) {
                result = visitChildren(state);
            }
            return result;
        }

        /**
         * Invokes {@link #visit(LoadState)} for each child of the specified
         * state. If the method returns <tt>false</tt>, iteration terminates.
         *
         * @param state the state
         * @return the result of {@link #visit(LoadState)}
         */
        protected boolean visitChildren(LoadState state) {
            boolean result = true;
            for (LoadState child : state.childStates) {
                if (!visited.contains(child)) {
                    if (!visit(child)) {
                        result = false;
                        break;
                    }
                }
            }
            return result;
        }

        /**
         * Registers a state as being visited.
         *
         * @param state the state
         */
        protected void addVisited(LoadState state) {
            visited.add(state);
        }

    }

    /**
     * Visitor that updates unsaved references with their saved version.
     */
    private static class UnsavedUpdaterVisitor extends Visitor {

        /**
         * The saved reference.
         */
        private final IMObjectReference reference;


        /**
         * Creates a new <tt>UnsavedUpdaterVisitor</tt>.
         *
         * @param reference the saved reference
         */
        public UnsavedUpdaterVisitor(IMObjectReference reference) {
            this.reference = reference;
        }

        /**
         * Updates each unsaved reference that matches the saved one.
         *
         * @param state the state to check
         * @return <tt>true</tt>
         */
        protected boolean doVisit(LoadState state) {
            Map<IMObjectReference, UnsavedRefUpdater> unsaved = state.unsaved;
            if (unsaved != null) {
                UnsavedRefUpdater updater = unsaved.get(reference);
                if (updater != null) {
                    updater.update(reference);
                }
            }
            return true;
        }
    }

}
