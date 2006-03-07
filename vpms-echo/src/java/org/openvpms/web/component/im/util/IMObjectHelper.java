package org.openvpms.web.component.im.util;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.spring.ServiceHelper;


/**
 * {@link IMObject} helper methods.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(IMObjectHelper.class);


    /**
     * Returns an object given its reference.
     *
     * @param reference the object reference May be <code>null</code>
     * @return the object corresponding to <code>reference</code> or
     *         <code>null</code> if none exists
     */
    public static IMObject getObject(IMObjectReference reference) {
        IMObject result = null;
        if (reference != null) {
            result = Context.getInstance().getObject(reference);
            if (result == null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                try {
                    result = service.get(reference);
                } catch (ArchetypeServiceException error) {
                    _log.error(error, error);
                }
            }
        }
        return result;
    }

    /**
     * Performs a deep copy of an object.
     *
     * @param object the object to copy
     * @return a copy of <code>object</code>
     */
    public static IMObject copy(IMObject object) {
        return copy(object, ServiceHelper.getArchetypeService());
    }

    /**
     * Performs a deep copy of an object.
     *
     * @param object  the object to copy
     * @param service the archetype service
     * @return a copy of <code>object</code>
     */
    public static IMObject copy(IMObject object, IArchetypeService service) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object, service);
        IMObject result = service.create(object.getArchetypeId());
        for (NodeDescriptor descriptor : archetype.getAllNodeDescriptors()) {
            if (!descriptor.isReadOnly() && !descriptor.isHidden()) {
                if (!descriptor.isCollection()) {
                    descriptor.setValue(result, descriptor.getValue(object));
                } else {
                    for (IMObject child : descriptor.getChildren(object)) {
                        IMObject value;
                        if (descriptor.isParentChild()) {
                            value = copy(child, service);
                        } else {
                            value = child;
                        }
                        descriptor.addChildToCollection(result, value);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the first object instance from a collection with matching short
     * name.
     *
     * @param shortName the short name
     * @param objects   the objects to search
     * @return the first object from the collection with matching short name, or
     *         <code>null</code> if none exists.
     */
    public static final <T extends IMObject> T
            getObject(String shortName, Collection<T> objects) {
        T result = null;
        for (T object : objects) {
            if (object.getArchetypeId().getShortName().equals(shortName)) {
                result = object;
                break;
            }
        }
        return result;
    }
}

