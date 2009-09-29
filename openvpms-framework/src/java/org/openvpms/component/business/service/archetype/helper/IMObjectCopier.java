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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper to copy {@link IMObject} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-06-27 04:04:11Z $
 */
public class IMObjectCopier {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Map of original -> copied objects, to avoid duplicate copying.
     */
    private Map<IMObjectReference, Copy> copies;

    /**
     * The copy handler.
     */
    private final IMObjectCopyHandler handler;


    /**
     * Construct a new <tt>IMObjectCopier</tt>.
     *
     * @param handler the copy handler
     */
    public IMObjectCopier(IMObjectCopyHandler handler) {
        this(handler, ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Construct a new <tt>IMObjectCopier</tt>.
     *
     * @param handler the copy handler
     * @param service the archetype service
     */
    public IMObjectCopier(IMObjectCopyHandler handler,
                          IArchetypeService service) {
        this.handler = handler;
        this.service = service;
    }

    /**
     * Copy an object, returning a list containing the copy, and any copied
     * child references.
     * <p/>
     * Any derived values will be populated on the returned objects.
     *
     * @param object the object to copy
     * @return a copy of <tt>object</tt>, and any copied child references.
     *         The copy of <tt>object</tt> is the first element in the returned
     *         list
     */
    public List<IMObject> apply(IMObject object) {
        List<IMObject> result = new ArrayList<IMObject>();
        copies = new HashMap<IMObjectReference, Copy>();
        IMObject target = apply(object, result, false);
        result.add(0, target);
        return result;
    }

    /**
     * Copy an object.
     * <p/>
     * Any derived values will be populated on the returned objects.
     *
     * @param object the object to copy.
     * @return a copy of <tt>object</tt>
     * @deprecated this saves child objects, as it processes. If the copy fails,
     *             child objects are not removed. Use {@link #apply} instead.
     */
    @Deprecated
    public IMObject copy(IMObject object) {
        copies = new HashMap<IMObjectReference, Copy>();
        List<IMObject> children = new ArrayList<IMObject>();
        return apply(object, children, true);
    }

    /**
     * Apply the copier to an object, copying it or returning it unchanged, as
     * determined by the {@link IMObjectCopyHandler}.
     *
     * @param source   the source object
     * @param children a list of child objects created during copying
     * @param save     determines if child objects should be saved
     * @return a copy of <tt>source</tt> if the handler indicates it should
     *         be copied; otherwise returns <tt>source</tt> unchanged
     */
    protected IMObject apply(IMObject source, List<IMObject> children,
                             boolean save) {
        IMObject target = handler.getObject(source, service);
        if (target != null) {
            // cache the references to avoid copying the same object twice
            Copy copy = new Copy(target);
            copies.put(source.getObjectReference(), copy);

            if (target != source) {
                doCopy(source, target, children, save);
            }
            copy.complete();
        }
        return target;
    }

    /**
     * Performs a copy of an object.
     *
     * @param source   the object to copy
     * @param target   the target to copy to
     * @param children a list of child objects created during copying
     * @param save     determines if child objects should be saved
     */
    protected void doCopy(IMObject source, IMObject target,
                          List<IMObject> children, boolean save) {
        ArchetypeDescriptor sourceType
                = DescriptorHelper.getArchetypeDescriptor(source, service);
        ArchetypeDescriptor targetType
                = DescriptorHelper.getArchetypeDescriptor(target, service);

        // copy the nodes
        for (NodeDescriptor sourceDesc : sourceType.getAllNodeDescriptors()) {
            NodeDescriptor targetDesc = handler.getNode(sourceType, sourceDesc,
                                                        targetType);
            if (targetDesc != null) {
                if (sourceDesc.isObjectReference()) {
                    IMObjectReference ref
                            = (IMObjectReference) sourceDesc.getValue(source);
                    if (ref != null) {
                        ref = copyReference(ref, source, children, save);
                        sourceDesc.setValue(target, ref);
                    }
                } else if (!sourceDesc.isCollection()) {
                    targetDesc.setValue(target, sourceDesc.getValue(source));
                } else {
                    copyChildren(source, target, children, save, sourceDesc, targetDesc);
                }
            }
        }
        // derive any values in the target
        service.deriveValues(target);
    }

    /**
     * Copies collections.
     *
     * @param source     the object to copy
     * @param target     the target to copy to
     * @param children   a list of child objects created during copying
     * @param save       determines if child objects should be saved
     * @param sourceDesc the source collection node descriptor
     * @param targetDesc the target collection node descriptor
     */
    private void copyChildren(IMObject source, IMObject target, List<IMObject> children, boolean save,
                              NodeDescriptor sourceDesc, NodeDescriptor targetDesc) {
        for (IMObject child : sourceDesc.getChildren(source)) {
            IMObject value;
            Copy copy = null;
            if (sourceDesc.isParentChild()) {
                copy = copies.get(child.getObjectReference());
                if (copy == null) {
                    value = apply(child, children, save);
                } else {
                    // referencing an object already present in another collection
                    value = copy.getObject();
                }
            } else {
                value = child;
            }
            if (value != null) {
                if (copy == null || copy.isComplete()) {
                    targetDesc.addChildToCollection(target, value);
                } else {
                    // can't safely add an incomplete object to the collection, so queue it for later
                    copy.queue(target, targetDesc);
                }
            }
        }
    }

    /**
     * Helper to copy the object referred to by a reference, and return the new
     * reference.
     *
     * @param reference the reference
     * @param parent    the parent object
     * @param children  a list of child objects created during copying
     * @param save      determines if child objects should be saved
     * @return a new reference, or one from <tt>references</tt> if the
     *         reference has already been copied
     */
    private IMObjectReference copyReference(IMObjectReference reference, IMObject parent, List<IMObject> children,
                                            boolean save) {
        Copy copy = copies.get(reference);
        IMObject object;
        if (copy != null) {
            object = copy.getObject();
        } else {
            IMObject original = service.get(reference);
            if (original == null) {
                throw new IMObjectCopierException(IMObjectCopierException.ErrorCode.ObjectNotFound, reference,
                                                  parent.getObjectReference());
            }
            object = apply(original, children, save);
            if (object != original && object != null) {
                // child was copied
                children.add(object);
                if (save) {
                    service.save(object);
                }
            }
        }
        return (object != null) ? object.getObjectReference() : null;
    }

    /**
     * Manages the state of a copied object.
     */
    private static class Copy {

        /**
         * The copied object.
         */
        private final IMObject object;

        /**
         * Determines if the object is complete.
         */
        private boolean complete;

        /**
         * A queue of collection objects.
         */
        private List<CollectionAdder> queue;

        /**
         * Creates a new <tt>Copy</tt>.
         *
         * @param object the copied object
         */
        public Copy(IMObject object) {
            this.object = object;
        }

        /**
         * Returns the copied object.
         *
         * @return the copied object
         */
        public IMObject getObject() {
            return object;
        }

        /**
         * Determines if the object is complete.
         *
         * @return <tt>true</tt> if the object is complete, otherwise <tt>false</tt>
         */
        public boolean isComplete() {
            return complete;
        }

        /**
         * Marks the object as being complete, adding it to any queued collections.
         */
        public void complete() {
            this.complete = true;
            if (queue != null) {
                for (CollectionAdder adder : queue) {
                    adder.add();
                }
                queue.clear();
            }
        }

        /**
         * Queues the object for addition to a collection.
         * <p/>
         * This should be used to queue incomplete objects for addition until such time as they are complete.
         *
         * @param target     the target object owns the collection
         * @param descriptor the collection node descriptor
         */
        public void queue(IMObject target, NodeDescriptor descriptor) {
            if (queue == null) {
                queue = new ArrayList<CollectionAdder>();
            }
            queue.add(new CollectionAdder(target, descriptor, object));
        }
    }

    /**
     * Helper to add an object to a collection.
     */
    private static class CollectionAdder {

        /**
         * The parent object.
         */
        private final IMObject parent;

        /**
         * The collection node descriptor.
         */
        private final NodeDescriptor descriptor;

        /**
         * The object to add.
         */
        private final IMObject value;

        /**
         * Creates a new <tt>CollectionAdder</tt>.
         *
         * @param parent     the parent object
         * @param descriptor the collection node descriptor
         * @param value      the value to add
         */
        public CollectionAdder(IMObject parent, NodeDescriptor descriptor, IMObject value) {
            this.parent = parent;
            this.descriptor = descriptor;
            this.value = value;
        }

        /**
         * Adds the object to the collection.
         */
        public void add() {
            descriptor.addChildToCollection(parent, value);
        }
    }
}