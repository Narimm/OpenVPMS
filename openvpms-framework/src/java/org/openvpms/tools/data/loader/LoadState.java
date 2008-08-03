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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
     * Child objects of this state.
     */
    private final Set<IMObject> childObjects = new HashSet<IMObject>();

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
     * A map of unsaved references to their corresponding updaters.
     */
    private Map<IMObjectReference, UnsavedRefUpdater> unsaved;

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
     * Returns the objects to save.
     *
     * @return the objects to save
     */
    public Set<IMObject> getObjects() {
        ObjectCollector collector = new ObjectCollector();
        collector.visit(this);
        Set<IMObject> result = new LinkedHashSet<IMObject>(childObjects);
        result.addAll(collector.getObjects());
        return result;
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
    public Set<DeferredUpdater> getDeferred() {
        DeferredCollector collector = new DeferredCollector();
        collector.visit(this);
        return collector.getUpdaters();
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
        return new CompleteVistor().visit(this);
    }

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
                        deferred.remove(getId());
                        return true;
                    }
                    return false;
                }
            };
            deferred.put(childUpdater.getId(), childUpdater);
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

        addChild(node, id, context);
    }

    /**
     * Returns the set of unsaved object references reachable from this state.
     *
     * @return the set of unsaved object references
     */
    public Set<IMObjectReference> getUnsaved() {
        UnsavedCollector collector = new UnsavedCollector();
        collector.visit(this);
        return collector.getUnsaved();
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
                deferred.put(id, new DeferredUpdater(id) {
                    public boolean update(IMObjectReference reference,
                                          LoadContext context) {
                        if (setReference(descriptor, id, context)) {
                            deferred.remove(getId());
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
                unsaved.put(ref, new UnsavedRefUpdater(this, ref, descriptor));
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
            childObjects.add(child);
            result = true;

        } else {
            deferred.put(id, new DeferredUpdater(id) {
                public boolean update(IMObjectReference reference,
                                      LoadContext context) {
                    if (addChild(node, id, context)) {
                        deferred.remove(id);
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
     * Visitor that collects the IMObject from each state.
     */
    private static class ObjectCollector extends Visitor {

        /**
         * The collected objects.
         */
        private Set<IMObject> objects = new LinkedHashSet<IMObject>();

        /**
         * Returns the collected objects.
         *
         * @return the collected object
         */
        public Set<IMObject> getObjects() {
            return objects;
        }

        /**
         * Visits each state reachable from the specified state
         *
         * @param state the starting state
         * @return <tt>true</tt>
         */
        @Override
        public boolean visit(LoadState state) {
            addVisited(state);
            visitChildren(state);
            doVisit(state);
            return true;
        }

        /**
         * Visits the specified state.
         *
         * @param state the state to visit
         * @return <tt>true</tt> to visit any other state, <tt>false</tt> to
         *         terminate
         */
        protected boolean doVisit(LoadState state) {
            objects.add(state.getObject());
            return true;
        }
    }

    /**
     * Visitor that determines if the state is complete.
     */
    private static class CompleteVistor extends Visitor {

        /**
         * Determines if the state is complete.
         *
         * @param state the state
         * @return <tt>true</tt> if there are no deferred updaters, otherwise
         *         <tt>false</tt>
         */
        public boolean doVisit(LoadState state) {
            return state.deferred.isEmpty();
        }
    }

    /**
     * Visitor that collects deferred updaters from each state.
     */
    private static class DeferredCollector extends Visitor {

        /**
         * The collected updaters.
         */
        private Set<DeferredUpdater> updaters;

        /**
         * Helper empty set.
         */
        private static final Set<DeferredUpdater> EMPTY
                = Collections.emptySet();

        /**
         * Returns the collected deferred updaters.
         *
         * @return the deferred updaters.
         */
        public Set<DeferredUpdater> getUpdaters() {
            return (updaters != null) ? updaters : EMPTY;
        }

        /**
         * Collects deferred updaters from the state.
         *
         * @param state the state
         * @return <tt>true</tt>
         */
        protected boolean doVisit(LoadState state) {
            Map<String, DeferredUpdater> deferred = state.deferred;
            if (deferred != null && !state.deferred.isEmpty()) {
                if (updaters == null) {
                    updaters = new HashSet<DeferredUpdater>();
                }
                updaters.addAll(deferred.values());
            }
            return true;
        }

    }

    /**
     * Visitor that collects unsaved references from each state.
     */
    private static class UnsavedCollector extends Visitor {

        /**
         * The collected references.
         */
        private Set<IMObjectReference> refs;

        /**
         * Helper empty set.
         */
        private static final Set<IMObjectReference> EMPTY
                = Collections.emptySet();

        /**
         * Returns the unsaved object references.
         *
         * @return the unsaved references
         */
        public Set<IMObjectReference> getUnsaved() {
            return (refs != null) ? refs : EMPTY;
        }

        /**
         * Collects unsaved references from the specified state.
         *
         * @param state the state
         * @return <tt>true</tt>
         */
        @Override
        public boolean visit(LoadState state) {
            LoadState parent = state.parent;
            if (parent != null && parent.getObject().isNew()) {
                addUnsaved(parent.getObject().getObjectReference());
            }
            return super.visit(state);
        }

        /**
         * Collects unsaved references.
         *
         * @param state the state
         * @return <tt>true</tt>
         */
        protected boolean doVisit(LoadState state) {
            IMObject object = state.getObject();
            if (object.isNew()) {
                addUnsaved(object.getObjectReference());
            }
            if (state.unsaved != null) {
                for (IMObjectReference ref : state.unsaved.keySet()) {
                    addUnsaved(ref);
                }
            }
            return true;
        }

        /**
         * Adds an unsaved reference.
         *
         * @param ref the reference
         */
        private void addUnsaved(IMObjectReference ref) {
            if (refs == null) {
                refs = new HashSet<IMObjectReference>();
            }
            refs.add(ref);
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
