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

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class IMObjectState {

    public static final String ID_PREFIX = "id:";

    private final List<IMObjectState> children = new ArrayList<IMObjectState>();

    private final IMObjectState parent;
    private final IMObject object;

    private final ArchetypeDescriptor descriptor;

    private Set<DeferredUpdater> deferred = new HashSet<DeferredUpdater>();

    private final Location location;

    public IMObjectState(IMObjectState parent,
                         IMObject object, ArchetypeDescriptor descriptor,
                         Location location) {
        this.parent = parent;
        this.object = object;
        this.descriptor = descriptor;
        this.location = location;
    }

    public IMObjectState getParent() {
        return parent;
    }

    public void setValue(String name, String value, LoadContext context) {
        NodeDescriptor node = getNode(name);

        try {
            if (value.startsWith(ID_PREFIX)) {
                setReference(node, value, context);
            } else if (node.isDate()) {
                node.setValue(object, getDate(value));
            } else {
                node.setValue(object, value);
            }
        } catch (Exception exception) {
            throw new ArchetypeDataLoaderException(
                    FailedToSetAtribute, exception,
                    name, value, location);
        }
    }

    public List<IMObjectState> getChildren() {
        return children;
    }

    public void addDeferred(String id, NodeDescriptor descriptor) {
        deferred.add(new DeferredUpdater(this, descriptor, id));
    }

    public Set<DeferredUpdater> getDeferred() {
        DeferredCollector collector = new DeferredCollector();
        collector.visit(this);
        return collector.getUpdaters();
    }

    public void removeDeferred(DeferredUpdater updater) {
        deferred.remove(updater);
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

    private void setReference(NodeDescriptor descriptor,
                              String id, LoadContext context) {
        IMObjectReference ref = context.getReference(id);
        if (ref == null) {
            addDeferred(id, descriptor);
        } else {
            setReference(descriptor, ref, context);
        }
    }

    public void setReference(NodeDescriptor descriptor,
                             IMObjectReference ref,
                             LoadContext context) {
        Object child;
        if (descriptor.isObjectReference()) {
            child = ref;
        } else {
            IArchetypeService service = context.getService();
            if (context.validateOnly()) {
                child = service.create(ref.getArchetypeId());
            } else {
                child = service.get(ref);
                if (child == null) {
                    throw new ArchetypeDataLoaderException(ReferenceNotFound,
                                                           ref);
                }
            }
        }

        if (descriptor.isCollection()) {
            descriptor.addChildToCollection(object, child);
        } else {
            descriptor.setValue(object, child);
        }
    }

    private NodeDescriptor getNode(String name) {
        NodeDescriptor ndesc = descriptor.getNodeDescriptor(name);
        if (ndesc == null) {
            throw new ArchetypeDataLoaderException(
                    InvalidAttribute, location.getLineNumber(),
                    location.getColumnNumber(), name, descriptor.getType());
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

    public void addChild(String collection, IMObjectState child) {
        NodeDescriptor node = getNode(collection);
        if (!node.isCollection()) {
            throw new ArchetypeDataLoaderException(
                    ParentNotACollection, location);
        }
        node.addChildToCollection(object, child.getObject());
        children.add(child);
    }

    public void addChild(String collection, String id, LoadContext context) {
        NodeDescriptor node = getNode(collection);
        if (!node.isCollection()) {
            throw new ArchetypeDataLoaderException(
                    ParentNotACollection, location);
        }

        IMObjectReference ref = context.getReference(id);
        IMObject child = null;
        if (ref != null) {
            IArchetypeService service = context.getService();
            if (context.validateOnly()) {
                child = service.create(ref.getArchetypeId());
            } else {
                child = service.get(ref);
            }
        }
        if (child != null) {
            node.addChildToCollection(object, child);
        } else {
            addDeferred(id, node);
        }
    }

    private static abstract class Visitor {

        private Set<IMObjectState> visited = new HashSet<IMObjectState>();

        public boolean visit(IMObjectState state) {
            addVisited(state);
            boolean result = doVisit(state);
            if (result) {
                result = visitChildren(state);
            }
            return result;
        }

        protected boolean visitChildren(IMObjectState state) {
            boolean result = true;
            for (IMObjectState child : state.children) {
                if (!visited.contains(child)) {
                    if (!visit(child)) {
                        result = false;
                        break;
                    }
                }
            }
            return result;
        }

        protected void addVisited(IMObjectState state) {
            visited.add(state);
        }

        protected abstract boolean doVisit(IMObjectState state);

    }


    private static class CompleteVistor extends Visitor {

        public boolean doVisit(IMObjectState state) {
            return state.deferred.isEmpty();
        }
    }

    private static class DeferredCollector extends Visitor {

        private Set<DeferredUpdater> updaters;

        private static final Set<DeferredUpdater> EMPTY
                = Collections.emptySet();

        protected boolean doVisit(IMObjectState state) {
            Set<DeferredUpdater> deferred = state.deferred;
            if (deferred != null && !state.deferred.isEmpty()) {
                if (updaters == null) {
                    updaters = new LinkedHashSet<DeferredUpdater>();
                }
                updaters.addAll(deferred);
            }
            return true;
        }

        public Set<DeferredUpdater> getUpdaters() {
            return (updaters != null) ? updaters : EMPTY;
        }
    }

}
