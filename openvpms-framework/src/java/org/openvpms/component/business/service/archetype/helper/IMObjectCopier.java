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

import java.util.HashMap;
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
     * Copy an object.
     *
     * @param object the object to copy.
     * @return a copy of <tt>object</tt>
     */
    public IMObject copy(IMObject object) {
        references = new HashMap<IMObjectReference, IMObjectReference>();
        return apply(object);
    }

    /**
     * Apply the copier to an object, copying it or returning it unchanged, as
     * determined by the {@link IMObjectCopyHandler}.
     *
     * @param source the source object
     * @return a copy of <tt>source</tt> if the handler indicates it should
     *         be copied; otherwise returns <tt>source</tt> unchanged
     */
    protected IMObject apply(IMObject source) {
        IMObject target = handler.getObject(source, service);
        if (target != null) {
            // cache the references to avoid copying the same object twice
            references.put(source.getObjectReference(),
                           target.getObjectReference());

            if (target != source) {
                doCopy(source, target);
            }
        }
        return target;
    }

    /**
     * Performs a copy of an object.
     *
     * @param source the object to copy
     * @param target the target to copy to
     */
    protected void doCopy(IMObject source, IMObject target) {
        ArchetypeDescriptor sourceType
                = DescriptorHelper.getArchetypeDescriptor(source, service);
        ArchetypeDescriptor targetType
                = DescriptorHelper.getArchetypeDescriptor(target, service);

        // copy the nodes
        for (NodeDescriptor sourceDesc : sourceType.getAllNodeDescriptors()) {
            NodeDescriptor targetDesc = handler.getNode(sourceDesc,
                                                        targetType);
            if (targetDesc != null) {
                if (sourceDesc.isObjectReference()) {
                    IMObjectReference ref
                            = (IMObjectReference) sourceDesc.getValue(source);
                    if (ref != null) {
                        ref = copyReference(ref);
                        sourceDesc.setValue(target, ref);
                    }
                } else if (!sourceDesc.isCollection()) {
                    targetDesc.setValue(target, sourceDesc.getValue(source));
                } else {
                    for (IMObject child : sourceDesc.getChildren(source)) {
                        IMObject value;
                        if (sourceDesc.isParentChild()) {
                            value = apply(child);
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
    }

    /**
     * Helper to copy the object referred to by a reference, and return the new
     * reference.
     *
     * @param reference the reference
     * @return a new reference, or one from <tt>references</tt> if the
     *         reference has already been copied
     */
    private IMObjectReference copyReference(IMObjectReference reference) {
        IMObjectReference result = references.get(reference);
        if (result == null) {
            IMObject original = ArchetypeQueryHelper.getByObjectReference(
                    service, reference);
            IMObject object = apply(original);
            if (object != original && object != null) {
                // copied, so save it
                service.save(object);
            }
            if (object != null) {
                result = object.getObjectReference();
            }
        }
        return result;
    }

}