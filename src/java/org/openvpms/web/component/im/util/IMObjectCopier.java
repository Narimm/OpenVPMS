package org.openvpms.web.component.im.util;

import java.util.HashMap;
import java.util.Map;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Helper to copy {@link IMObject} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectCopier {

    /**
     * The archetype service.
     */
    private IArchetypeService _service;

    /**
     * Map of original -> copied references, to avoid duplicate copying.
     */
    private Map<IMObjectReference, IMObjectReference> _references;

    /**
     * The copy handler.
     */
    private final IMObjectCopyHandler _handler;


    /**
     * Construct a new <code>IMObjectCopier</code>.
     *
     * @param handler the copy handler
     */
    public IMObjectCopier(IMObjectCopyHandler handler) {
        _handler = handler;
        _service = ServiceHelper.getArchetypeService();
    }

    /**
     * Copy an object.
     *
     * @param object the object to copy.
     * @return a copy of <code>object</code>
     */
    public IMObject copy(IMObject object) {
        _references = new HashMap<IMObjectReference, IMObjectReference>();
        return apply(object);
    }

    /**
     * Apply the copier to an object, copying it or returning it unchanged, as
     * determined by the {@link IMObjectCopyHandler}.
     *
     * @param source the source object
     * @return a copy of <code>source</code> if the handler indicates it should
     *         be copied; otherwise returns <code>source</code> unchanged
     */
    protected IMObject apply(IMObject source) {
        IMObject target = _handler.getObject(source, _service);
        if (target != null) {
            // cache the references to avoid copying the same object twice
            _references.put(source.getObjectReference(),
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
                = DescriptorHelper.getArchetypeDescriptor(source, _service);
        ArchetypeDescriptor targetType
                = DescriptorHelper.getArchetypeDescriptor(target, _service);

        // copy the nodes
        for (NodeDescriptor sourceDesc : sourceType.getAllNodeDescriptors()) {
            NodeDescriptor targetDesc = _handler.getNode(sourceDesc, targetType);
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
                        targetDesc.addChildToCollection(target, value);
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
     * @return a new reference, or one from <code>references</code> if the
     *         reference has already been copied
     */
    private IMObjectReference copyReference(IMObjectReference reference) {
        IMObjectReference result = _references.get(reference);
        if (result == null) {
            IMObject original = _service.get(reference);
            IMObject object = apply(original);
            if (object != original && object != null) {
                // copied, so save it
                _service.save(object);
            }
            result = object.getObjectReference();
        }
        return result;
    }


}
