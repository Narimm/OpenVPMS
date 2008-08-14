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
     * Map of original -> copied references, to avoid duplicate copying.
     */
    private Map<IMObjectReference, IMObjectReference> references;

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
        references = new HashMap<IMObjectReference, IMObjectReference>();
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
        references = new HashMap<IMObjectReference, IMObjectReference>();
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
            references.put(source.getObjectReference(),
                           target.getObjectReference());

            if (target != source) {
                doCopy(source, target, children, save);
            }
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
                        ref = copyReference(ref, children, save);
                        sourceDesc.setValue(target, ref);
                    }
                } else if (!sourceDesc.isCollection()) {
                    targetDesc.setValue(target, sourceDesc.getValue(source));
                } else {
                    for (IMObject child : sourceDesc.getChildren(source)) {
                        IMObject value;
                        if (sourceDesc.isParentChild()) {
                            value = apply(child, children, save);
                        } else {
                            value = child;
                        }
                        if (value != null) {
                            targetDesc.addChildToCollection(target, value);
                        }
                    }
                }
            }
        }
        // derive any values in the target
        service.deriveValues(target);
    }

    /**
     * Helper to copy the object referred to by a reference, and return the new
     * reference.
     *
     * @param reference the reference
     * @param children  a list of child objects created during copying
     * @param save      determines if child objects should be saved
     * @return a new reference, or one from <tt>references</tt> if the
     *         reference has already been copied
     */
    private IMObjectReference copyReference(IMObjectReference reference,
                                            List<IMObject> children,
                                            boolean save) {
        IMObjectReference result = references.get(reference);
        if (result == null) {
            IMObject original = service.get(reference);
            IMObject object = apply(original, children, save);
            if (object != original && object != null) {
                // child was copied
                children.add(object);
                if (save) {
                    service.save(object);
                }
            }
            if (object != null) {
                result = object.getObjectReference();
            }
        }
        return result;
    }

}