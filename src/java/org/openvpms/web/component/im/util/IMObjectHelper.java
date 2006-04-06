package org.openvpms.web.component.im.util;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
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
                    result = ArchetypeQueryHelper.getByObjectReference(
                            service, reference);
                } catch (ArchetypeServiceException error) {
                    _log.error(error, error);
                }
            }
        }
        return result;
    }

    /**
     * Determines if a object is an instance of a particular archetype.
     *
     * @param object    the object. May be <code>null</code>
     * @param shortName the archetype short name
     * @return <code>true</code> if object is an instance of
     *         <code>shortName</code>
     */
    public static boolean isA(IMObject object, String shortName) {
        if (object != null) {
            return object.getArchetypeId().getShortName().equals(shortName);
        }
        return false;
    }

    /**
     * Determines if an object reference refers to an instance of a particular
     * archetype.
     *
     * @param reference the object. May be <code>null</code>
     * @param shortName the archetype short name
     * @return <code>true</code> if the reference refers to an instance of
     *         <code>shortName</code>
     */
    public static boolean isA(IMObjectReference reference, String shortName) {
        if (reference != null) {
            return reference.getArchetypeId().getShortName().equals(shortName);
        }
        return false;
    }

    /**
     * Returns a value from an object, given the value's node descriptor name.
     *
     * @param object the object
     * @param node   the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static Object getValue(IMObject object, String node) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        return getValue(object, archetype, node);
    }

    /**
     * Returns a value from an object, given the value's node descriptor name.
     *
     * @param object    the object
     * @param archetype the archetype descriptor
     * @param node      the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static Object getValue(IMObject object,
                                  ArchetypeDescriptor archetype, String node) {
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        if (descriptor != null) {
            return descriptor.getValue(object);
        }
        return null;
    }

    /**
     * Returns a collection from an object, given the collection's node
     * descriptor name.
     *
     * @param object the object
     * @param node   the node name
     * @return the collection corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static Collection getValues(IMObject object, String node) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        if (descriptor != null) {
            return descriptor.getChildren(object);
        }
        return null;
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
    public static <T extends IMObject> T
            getObject(String shortName, Collection<T> objects) {
        T result = null;
        for (T object : objects) {
            if (isA(object, shortName)) {
                result = object;
                break;
            }
        }
        return result;
    }

}

