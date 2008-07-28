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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.tools.data.loader.ArchetypeDataLoaderException.ErrorCode.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LoadState {

    public static final String ID_PREFIX = "id:";

    private final List<LoadState> children = new ArrayList<LoadState>();

    private final LoadState parent;
    private final IMObject object;

    private final ArchetypeDescriptor descriptor;

    private Map<String, DeferredUpdater> deferred
            = new HashMap<String, DeferredUpdater>();

    private Map<IMObjectReference, UnsavedRefUpdater> unsaved;

    private final String path;
    private final int lineNo;

    public LoadState(LoadState parent, IMObject object,
                     ArchetypeDescriptor descriptor, String path,
                     int lineNo) {
        this.parent = parent;
        this.object = object;
        this.descriptor = descriptor;
        this.path = path;
        this.lineNo = lineNo;
    }

    public LoadState getParent() {
        return parent;
    }

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

    public Set<DeferredUpdater> getDeferred() {
        DeferredCollector collector = new DeferredCollector();
        collector.visit(this);
        return collector.getUpdaters();
    }

    public void removeDeferred(DeferredUpdater updater) {
        deferred.remove(updater.getId());
    }

    public IMObject getObject() {
        return object;
    }

    public ArchetypeId getArchetypeId() {
        return object.getArchetypeId();
    }

    public ArchetypeDescriptor getDescriptor() {
        return descriptor;
    }

    public boolean isComplete() {
        return new CompleteVistor().visit(this);
    }

    public String getPath() {
        return path;
    }

    public int getLineNumber() {
        return lineNo;
    }

    private boolean setReference(final NodeDescriptor descriptor,
                                 final String id, LoadContext context) {
        boolean result = false;
        final IMObjectReference ref = context.getReference(id);
        if (ref == null) {
            if (!deferred.containsKey(id)) {
                deferred.put(id, new AbstractDeferredUpdater(id) {
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

    public void removeUnsaved(IMObjectReference reference) {
        unsaved.remove(reference);
    }

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

    public void addChild(String collection, final LoadState child) {
        final NodeDescriptor node = getNode(collection);
        if (!node.isCollection()) {
            throw new ArchetypeDataLoaderException(
                    ParentNotACollection, lineNo);
        }
        if (child.isComplete()) {
            node.addChildToCollection(object, child.getObject());
            children.add(child);
        } else {
            DeferredUpdater updater = child.getDeferred().iterator().next();
            DeferredUpdater childUpdater
                    = new AbstractDeferredUpdater(updater.getId()) {
                public boolean update(IMObjectReference reference,
                                      LoadContext context) {
                    if (child.isComplete()) {
                        node.addChildToCollection(object, child.getObject());
                        children.add(child);
                        deferred.remove(getId());
                        return true;
                    }
                    return false;
                }
            };
            deferred.put(childUpdater.getId(), childUpdater);
        }
    }

    public void addChild(String collection, String id,
                         LoadContext context) {
        NodeDescriptor node = getNode(collection);
        if (!node.isCollection()) {
            throw new ArchetypeDataLoaderException(
                    ParentNotACollection, lineNo);
        }

        addChild(node, id, context);
    }

    private boolean addChild(final NodeDescriptor node, final String id,
                             LoadContext context) {
        boolean result = false;
        IMObject child = getObject(id, context);
        if (child != null) {
            node.addChildToCollection(object, child);
            result = true;

        } else {
            deferred.put(id, new AbstractDeferredUpdater(id) {
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

    private IMObject getObject(String id, LoadContext context) {
        IMObjectReference ref = context.getReference(id);
        IMObject object = null;
        if (ref != null) {
            IArchetypeService service = context.getService();
            if (context.validateOnly()) {
                object = service.create(ref.getArchetypeId());
            } else if (!ref.isNew()) {
                object = service.get(ref);
            }
        }
        return object;
    }

    public Set<IMObjectReference> getUnsaved() {
        UnsavedCollector collector = new UnsavedCollector();
        collector.visit(this);
        return collector.getUnsaved();
    }

    public void update(IMObjectReference ref) {
        UnsavedUpdaterVisitor visitor = new UnsavedUpdaterVisitor(ref);
        visitor.visit(this);
    }

    private static abstract class Visitor {

        private Set<LoadState> visited = new HashSet<LoadState>();

        public boolean visit(LoadState state) {
            addVisited(state);
            boolean result = doVisit(state);
            if (result) {
                result = visitChildren(state);
            }
            return result;
        }

        protected boolean visitChildren(LoadState state) {
            boolean result = true;
            for (LoadState child : state.children) {
                if (!visited.contains(child)) {
                    if (!visit(child)) {
                        result = false;
                        break;
                    }
                }
            }
            return result;
        }

        protected void addVisited(LoadState state) {
            visited.add(state);
        }

        protected abstract boolean doVisit(LoadState state);

    }


    private static class CompleteVistor extends Visitor {

        public boolean doVisit(LoadState state) {
            return state.deferred.isEmpty();
        }
    }

    private static class DeferredCollector extends Visitor {

        private Set<DeferredUpdater> updaters;

        private static final Set<DeferredUpdater> EMPTY
                = Collections.emptySet();

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

        public Set<DeferredUpdater> getUpdaters() {
            return (updaters != null) ? updaters : EMPTY;
        }
    }

    private static class UnsavedCollector extends Visitor {

        private Set<IMObjectReference> refs;

        private static final Set<IMObjectReference> EMPTY
                = Collections.emptySet();

        public Set<IMObjectReference> getUnsaved() {
            return (refs != null) ? refs : EMPTY;
        }

        @Override
        public boolean visit(LoadState state) {
            LoadState parent = state.parent;
            if (parent != null && parent.getObject().isNew()) {
                addUnsaved(parent.getObject());
            }
            return super.visit(state);
        }

        protected boolean doVisit(LoadState state) {
            IMObject object = state.getObject();
            if (object.isNew()) {
                addUnsaved(object);
            }
            if (state.unsaved != null) {
                for (IMObjectReference ref : state.unsaved.keySet()) {
                    addUnsaved(ref);
                }
            }
            return true;
        }

        private void addUnsaved(IMObject object) {
            addUnsaved(object.getObjectReference());
        }

        private void addUnsaved(IMObjectReference ref) {
            if (refs == null) {
                refs = new HashSet<IMObjectReference>();
            }
            refs.add(ref);
        }
    }

    private static class UnsavedUpdaterVisitor extends Visitor {

        private final IMObjectReference ref;

        public UnsavedUpdaterVisitor(IMObjectReference ref) {
            this.ref = ref;
        }

        protected boolean doVisit(LoadState state) {
            Map<IMObjectReference, UnsavedRefUpdater> unsaved = state.unsaved;
            if (unsaved != null) {
                UnsavedRefUpdater updater = unsaved.get(ref);
                if (updater != null) {
                    updater.update(ref);
                }
            }
            return true;
        }
    }

}
