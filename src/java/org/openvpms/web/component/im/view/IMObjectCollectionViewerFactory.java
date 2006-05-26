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

package org.openvpms.web.component.im.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.util.ArchetypeHandlers;
import org.openvpms.web.component.im.util.DescriptorHelper;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectCollectionViewerFactory {

    /**
     * Viewer implementations.
     */
    private static ArchetypeHandlers _viewers;

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(IMObjectCollectionViewerFactory.class);


    /**
     * Prevent construction.
     */
    private IMObjectCollectionViewerFactory() {
    }

    /**
     * Creates a new viewer.
     *
     * @param collection the collection to view
     * @param object     the parent of the collection
     * @return a viewer for the collection
     */
    public static IMObjectCollectionViewer create(CollectionProperty collection,
                                                  IMObject object) {
        IMObjectCollectionViewer result = null;

        NodeDescriptor descriptor = collection.getDescriptor();
        String[] shortNames = DescriptorHelper.getShortNames(descriptor);
        Set<Class> matches = new HashSet<Class>();
        for (String shortName : shortNames) {
            Class clazz = getViewers().getHandler(shortName);
            if (clazz != null) {
                matches.add(clazz);
            }
        }
        if (matches.size() == 1) {
            Class clazz = matches.toArray(new Class[0])[0];
            Constructor ctor = getConstructor(clazz, collection, object);
            if (ctor != null) {
                try {
                    result = (IMObjectCollectionViewer) ctor.newInstance(
                            collection, object);
                } catch (Throwable throwable) {
                    _log.error(throwable, throwable);
                }
            } else {
                _log.error("No valid constructor found for class: "
                        + clazz.getName());
            }
        }
        if (result == null) {
            result = new DefaultIMObjectCollectionViewer(collection, object);
        }
        return result;
    }

    /**
     * Returns the Viewers.
     *
     * @return the Viewers
     */
    private static synchronized ArchetypeHandlers getViewers() {
        if (_viewers == null) {
            _viewers = new ArchetypeHandlers(
                    "IMObjectCollectionViewerFactory.properties",
                    IMObjectCollectionViewer.class);
        }
        return _viewers;
    }

    /**
     * Returns a constructor to construct a new viewer.
     *
     * @param type       the Viewer type
     * @param collection the collection property
     * @param object     the parent of the collection
     * @return a constructor to construct the viewer, or <code>null</code> if
     *         none can be found
     */
    private static Constructor getConstructor(Class type,
                                              CollectionProperty collection,
                                              IMObject object) {
        Constructor[] ctors = type.getConstructors();

        for (Constructor ctor : ctors) {
            // check parameters
            Class[] ctorTypes = ctor.getParameterTypes();
            if (ctorTypes.length == 2) {
                Class ctorCollection = ctorTypes[0];
                Class ctorObj = ctorTypes[1];

                if (ctorCollection.isAssignableFrom(collection.getClass())
                        && ctorObj.isAssignableFrom(object.getClass())) {
                    return ctor;
                }
            }
        }
        return null;
    }

}
